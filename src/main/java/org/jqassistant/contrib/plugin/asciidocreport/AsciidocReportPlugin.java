package org.jqassistant.contrib.plugin.asciidocreport;

import static java.util.Collections.singletonList;
import static org.asciidoctor.AttributesBuilder.attributes;
import static org.asciidoctor.OptionsBuilder.options;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.report.api.AbstractReportPlugin;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.api.ReportPlugin.Default;
import com.buschmais.jqassistant.core.shared.asciidoc.AsciidoctorFactory;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FilePatternMatcher;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Default
public class AsciidocReportPlugin extends AbstractReportPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsciidocReportPlugin.class);

    private static final String PROPERTY_DIRECTORY = "asciidoc.report.directory";
    private static final String PROPERTY_RULE_DIRECTORY = "asciidoc.report.rule.directory";
    private static final String PROPERTY_FILE_INCLUDE = "asciidoc.report.file.include";
    private static final String PROPERTY_FILE_EXCLUDE = "asciidoc.report.file.exclude";

    private static final String DEFAULT_DIRECTORY = "html";
    private static final String DEFAULT_RULE_DIRECTORY = "jqassistant/rules";

    private static final String BACKEND_HTML5 = "html5";
    private static final String CODERAY = "coderay";

    private ReportContext reportContext;

    private File reportDirectory;

    private File ruleDirectory;

    private String fileInclude;

    private String fileExclude;

    private Map<String, RuleResult> conceptResults;
    private Map<String, RuleResult> constraintResults;

    @Override
    public void configure(ReportContext reportContext, Map<String, Object> properties) {
        this.reportContext = reportContext;
        File defaultReportDirectory = reportContext.getReportDirectory(DEFAULT_DIRECTORY);
        this.reportDirectory = getFile(PROPERTY_DIRECTORY, defaultReportDirectory, properties);
        this.ruleDirectory = getFile(PROPERTY_RULE_DIRECTORY, new File(DEFAULT_RULE_DIRECTORY), properties);
        if (this.reportDirectory.mkdirs()) {
            LOGGER.info("Created directory '" + this.reportDirectory.getAbsolutePath() + "'.");
        }
        this.fileInclude = (String) properties.get(PROPERTY_FILE_INCLUDE);
        this.fileExclude = (String) properties.get(PROPERTY_FILE_EXCLUDE);
    }

    private File getFile(String property, File defaultValue, Map<String, Object> properties) {
        String directoryName = (String) properties.get(property);
        return directoryName != null ? new File(directoryName) : defaultValue;
    }

    @Override
    public void begin() {
        conceptResults = new HashMap<>();
        constraintResults = new HashMap<>();
    }

    @Override
    public void end() {
        if (ruleDirectory.exists()) {
            File[] files = getAsciidocFiles();
            if (files.length > 0) {
                LOGGER.info("Calling for the Asciidoctor...");
                Asciidoctor asciidoctor = AsciidoctorFactory.getAsciidoctor();
                OptionsBuilder optionsBuilder = options().mkDirs(true).baseDir(ruleDirectory).toDir(reportDirectory).backend(BACKEND_HTML5)
                        .safe(SafeMode.UNSAFE).attributes(attributes().experimental(true).sourceHighlighter(CODERAY));
                LOGGER.info("Writing to report directory " + reportDirectory.getAbsolutePath());
                JavaExtensionRegistry extensionRegistry = asciidoctor.javaExtensionRegistry();
                for (File file : files) {
                    LOGGER.info("-> " + file.getPath());
                    Document document = asciidoctor.loadFile(file, optionsBuilder.asMap());
                    extensionRegistry.includeProcessor(new IncludeProcessor(document, conceptResults, constraintResults));
                    extensionRegistry.inlineMacro(new InlineMacroProcessor());
                    extensionRegistry.treeprocessor(new TreePreprocessor(conceptResults, constraintResults, reportContext));
                    asciidoctor.convertFile(file, optionsBuilder);
                }
                LOGGER.info("The Asciidoctor finished his work successfully.");
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
                .columnNames(columnNames != null ? columnNames : singletonList("Empty Result"));
        for (Map<String, Object> row : result.getRows()) {
            Map<String, List<String>> resultRow = new LinkedHashMap<>();
            for (Map.Entry<String, Object> rowEntry : row.entrySet()) {
                Object value = rowEntry.getValue();
                List<String> values = new ArrayList<>();
                if (value instanceof Iterable<?>) {
                    for (Object o : ((Iterable) value)) {
                        values.add(ReportHelper.getLabel(o));
                    }
                } else {
                    values.add(ReportHelper.getLabel(value));
                }
                resultRow.put(rowEntry.getKey(), values);
            }
            ruleResultBuilder.row(resultRow);
        }
        return ruleResultBuilder.build();
    }
}
