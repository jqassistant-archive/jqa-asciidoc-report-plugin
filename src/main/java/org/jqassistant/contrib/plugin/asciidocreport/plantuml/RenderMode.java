package org.jqassistant.contrib.plugin.asciidocreport.plantuml;

import static java.util.Arrays.asList;

import com.buschmais.jqassistant.core.report.api.ReportException;

public enum RenderMode {
    GRAPHVIZ(""), JDOT("!pragma graphviz_dot jdot\n");

    RenderMode(String pragma) {
        this.pragma = pragma;
    }

    private String pragma;

    public String getPragma() {
        return pragma;
    }

    /**
     * Returns the {@link RenderMode} for the given string
     *
     * @param rendermode
     *            The {@link RenderMode} as string.
     * @return The matching {@link RenderMode}
     * @throws IllegalArgumentException
     *             if rendermode is not valid.
     */
    public static RenderMode fromString(String rendermode) throws ReportException {
        for (RenderMode mode : RenderMode.values()) {
            if (mode.name().equalsIgnoreCase(rendermode)) {
                return mode;
            }
        }

        throw new ReportException(rendermode + " is not a valid, supported modes are " + asList(RenderMode.values()));
    }
}
