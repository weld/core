/**
 *
 */
package org.jboss.weld.examples.pastecode.model;


public enum Language {

    TEXT("txt", "Plain text", "text"),
    AS3("as3", "AS3", "as3"),
    BASH("sh", "Bash", "bash"),
    CSHARP("cs", "C#", "csharp"),
    COLD_FUSION("cf", "Cold Fusion", "coldfusion"), CPLUSPLUS("cpp", "C++", "cpp"),
    CSS("css", "CSS", "css"),
    DELPHI("pas", "Delphi", "pas"),
    DIFF("diff", "Diff", "diff"), ERLANG("erl", "Erlang", "erl"),
    GROOVY("groovy", "Groovy", "groovy"),
    JAVASCRIPT("js", "JavaScript", "js"),
    JAVA("java", "Java", "java"),
    JAVAFX("fx", "JavaFX", "javafx"),
    PERL("perl", "Perl", "perl"),
    PHP("php", "PHP", "php"),
    POWER_SHELL("ps1", "Power Shell", "powershell"),
    PYTHON("py", "Python", "py"),
    RUBY("rb", "Ruby", "rb"),
    SCALA("scl", "Scala", "scala"),
    SQL("sql", "Sql", "sql"),
    VISUAL_BASIC("vb", "Visual Basic", "vb"),
    XML("xml", "XML", "xml");

    private final String extension;
    private final String name;
    private final String brush;

    Language(String extension, String name, String brush) {
        this.extension = extension;
        this.name = name;
        this.brush = brush;
    }

    public String getBrush() {
        return brush;
    }

    public String getExtension() {
        return extension;
    }

    public String getName() {
        return name;
    }
}
