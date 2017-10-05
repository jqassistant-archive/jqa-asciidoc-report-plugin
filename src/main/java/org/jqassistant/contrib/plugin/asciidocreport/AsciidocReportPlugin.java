package org.jqassistant.contrib.plugin.asciidocreport;

import static java.util.Collections.singletonList;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FilePatternMatcher;

public class AsciidocReportPlugin implements ReportPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsciidocReportPlugin.class);

    private static final String PROPERTY_DIRECTORY = "asciidoc.report.directory";
    private static final String PROPERTY_RULE_DIRECTORY = "asciidoc.report.rule.directory";
    private static final String PROPERTY_FILE_INCLUDE = "asciidoc.report.file.include";
    private static final String PROPERTY_FILE_EXCLUDE = "asciidoc.report.file.exclude";

    private static final String DEFAULT_DIRECTORY = "jqassistant/report/asciidoc";
    private static final String DEFAULT_RULE_DIRECTORY = "jqassistant/report/asciidoc";
    public static final String BACKEND_HTML5 = "html5";
    public static final String CODERAY = "coderay";
    public static final String ASCIIDOCTOR_DIAGRAM = "asciidoctor-diagram";

    private File reportDirectory;

    private File ruleDirectory;

    private String fileInclude;

    private String fileExclude;

    private Map<String, RuleResult> conceptResults;
    private Map<String, RuleResult> constraintResults;

    @Override
    public void initialize() throws ReportException {
    }

    @Override
    public void configure(Map<String, Object> properties) throws ReportException {
        this.reportDirectory = getFile(PROPERTY_DIRECTORY, DEFAULT_DIRECTORY, properties);
        this.ruleDirectory = getFile(PROPERTY_RULE_DIRECTORY, DEFAULT_RULE_DIRECTORY, properties);
        if (this.reportDirectory.mkdirs()) {
            LOGGER.info("Created directory '" + this.reportDirectory.getAbsolutePath() + "'.");
        }
        this.fileInclude = (String) properties.get(PROPERTY_FILE_INCLUDE);
        this.fileExclude = (String) properties.get(PROPERTY_FILE_EXCLUDE);
    }

    private File getFile(String property, String defaultValue, Map<String, Object> properties) {
        String directoryName = (String) properties.get(property);
        return directoryName != null ? new File(directoryName) : new File(defaultValue);
    }

    @Override
    public void begin() throws ReportException {
        conceptResults = new HashMap<>();
        constraintResults = new HashMap<>();
    }

    @Override
    public void end() throws ReportException {
        if (ruleDirectory.exists()) {
            File[] files = getAsciidocFiles();
            if (files.length > 0) {
                LOGGER.info("Initializing Asciidoctor...");
                Asciidoctor asciidoctor = Asciidoctor.Factory.create();
                asciidoctor.requireLibrary(ASCIIDOCTOR_DIAGRAM);
                JavaExtensionRegistry extensionRegistry = asciidoctor.javaExtensionRegistry();
                OptionsBuilder optionsBuilder = OptionsBuilder.options().mkDirs(true).baseDir(ruleDirectory).toDir(reportDirectory).backend(BACKEND_HTML5)
                        .safe(SafeMode.UNSAFE).attributes(AttributesBuilder.attributes().experimental(true).sourceHighlighter(CODERAY));
                LOGGER.info("Using report directory " + reportDirectory.getAbsolutePath());
                for (File file : files) {
                    LOGGER.info("  " + file.getPath());
                    Document document = asciidoctor.loadFile(file, optionsBuilder.asMap());
                    extensionRegistry.blockMacro(new MacroProcessor(document, conceptResults, constraintResults));
                    extensionRegistry.treeprocessor(new ResultTreePreprocessor(conceptResults, constraintResults));
                    asciidoctor.convertFile(file, optionsBuilder);
                    asciidoctor.unregisterAllExtensions();
                }
                LOGGER.info("Finished rendering.");
            }
        }
    }

    private File[] getAsciidocFiles() {
        final FilePatternMatcher filePatternMatcher = FilePatternMatcher.Builder.newInstance().include(this.fileInclude).exclude(this.fileExclude).build();
        return ruleDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return filePatternMatcher.accepts(name);
            }
        });
    }

    @Override
    public void beginConcept(Concept concept) throws ReportException {
    }

    @Override
    public void endConcept() throws ReportException {
    }

    @Override
    public void beginGroup(Group group) throws ReportException {
    }

    @Override
    public void endGroup() throws ReportException {
    }

    @Override
    public void beginConstraint(Constraint constraint) throws ReportException {
    }

    @Override
    public void endConstraint() throws ReportException {
    }

    @Override
    public void setResult(Result<? extends ExecutableRule> result) throws ReportException {
        ExecutableRule rule = result.getRule();
        if (rule instanceof Concept) {
            this.conceptResults.put(rule.getId(), getRuleResult(result));
        } else if (rule instanceof Constraint) {
            this.constraintResults.put(rule.getId(), getRuleResult(result));
        }
    }

    private RuleResult getRuleResult(Result<? extends ExecutableRule> result) {
        RuleResult.RuleResultBuilder ruleResultBuilder = RuleResult.builder();
        List<String> columnNames = result.getColumnNames();
        ruleResultBuilder.rule(result.getRule()).effectiveSeverity(result.getSeverity()).status(result.getStatus())
                .columnNames(columnNames != null ? columnNames : singletonList("No Result"));
        for (Map<String, Object> row : result.getRows()) {
            Map<String, String> resultRow = new LinkedHashMap<>();
            for (Map.Entry<String, Object> rowEntry : row.entrySet()) {
                resultRow.put(rowEntry.getKey(), ReportHelper.getLabel(rowEntry.getValue()));
            }
            ruleResultBuilder.row(resultRow);
        }
        return ruleResultBuilder.build();
    }
}
