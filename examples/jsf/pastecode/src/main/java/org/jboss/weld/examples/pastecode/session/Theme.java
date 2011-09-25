package org.jboss.weld.examples.pastecode.session;

/**
 * Closed set of the visual themes available for displaying code fragments
 *
 * @author Pete Muir
 */
public enum Theme {

    DEFAULT("Default Theme"),
    DJANGO("Django Theme"),
    ECLIPSE("Eclipse Theme"),
    EMACS("Emacs Theme"),
    MIDNIGHT("Midnight Theme"),
    DARK("Dark Theme");

    private final String name;

    private Theme(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
