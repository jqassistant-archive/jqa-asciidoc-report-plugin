package org.jqassistant.contrib.plugin.asciidocreport.plantuml;

public enum RenderMode {
    GRAPHVIZ(""),
    JDOT("!pragma graphviz_dot jdot\n");

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
     * @throws IllegalArgumentException if rendermode is not valid.
     */
    public static RenderMode fromString(String rendermode){
        for (RenderMode mode : RenderMode.values()) {
            if(mode.name().equalsIgnoreCase(rendermode)){
                return mode;
            }
        }

        throw new IllegalArgumentException(rendermode + " is not a valid RenderMode");
    }
}
