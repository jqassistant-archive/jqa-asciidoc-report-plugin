package com.buschmais.jqassistant.plugin.asciidocreport.plantuml;

import com.buschmais.jqassistant.core.report.api.ReportException;

import static java.util.Arrays.asList;

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
     * @param renderMode
     *            The {@link RenderMode} as string.
     * @return The matching {@link RenderMode}
     * @throws ReportException
     *             If renderMode is not valid.
     */
    public static RenderMode fromString(String renderMode) throws ReportException {
        for (RenderMode mode : RenderMode.values()) {
            if (mode.name().equalsIgnoreCase(renderMode)) {
                return mode;
            }
        }
        throw new ReportException(renderMode + " is not a valid, supported modes are " + asList(RenderMode.values()));
    }
}
