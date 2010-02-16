package org.jboss.weld.examples.pastecode.session;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.Serializable;
import javax.faces.model.SelectItem;
import javax.enterprise.inject.Produces;
import java.util.List;
import java.util.ArrayList;

@ApplicationScoped
@Named("dataBean")
public class DataBean implements Serializable
{
   private static final long serialVersionUID = 991300443278089016L;

   private int LANG_EXTENSION = 0; // e.g. txt
   private int LANG_NAME = 1; // e.g. Plain text
   private int LANG_BRUSH = 2; // e.g. text
   private String languages[][] = { { "txt", "Plain text", "text" }, { "as3", "AS3", "as3" }, { "sh", "Bash", "bash" }, { "cs", "C#", "csharp" }, { "cf", "Cold Fusion", "coldfusion" }, { "cpp", "C++", "cpp" }, { "css", "CSS", "css" }, { "pas", "Delphi", "pas" }, { "diff", "Diff", "diff" }, { "erl", "Erlang", "erl" }, { "groovy", "Groovy", "groovy" }, { "js", "JavaScript", "js" }, { "java", "Java", "java" }, { "fx", "JavaFX", "javafx" }, { "perl", "Perl", "perl" }, { "php", "PHP", "php" }, { "ps1", "Power Shell", "powershell" }, { "py", "Python", "py" }, { "rb", "Ruby", "rb" }, { "scl", "Scala", "scala" }, { "sql", "Sql", "sql" }, { "vb", "Visual Basic", "vb" }, { "xml", "XML", "xml" } };
   private String themes[] = { "Default Theme", "Django Theme", "Eclipse Theme", "Emacs Theme", "Midnight Theme", "Dark Theme" };

   public DataBean()
   {
   }

   @Produces
   @Named("languageItems")
   public List<SelectItem> getLanguageItems()
   {
      List<SelectItem> items = new ArrayList<SelectItem>();
      for (int i = 0; i != languages.length; i++)
      {
         items.add(new SelectItem(languages[i][LANG_EXTENSION], languages[i][LANG_NAME]));
      }
      return items;
   }

   @Produces
   @Named("themeItems")
   public List<SelectItem> getThemeItems()
   {
      List<SelectItem> items = new ArrayList<SelectItem>();
      for (int i = 0; i != themes.length; i++)
      {
         items.add(new SelectItem(themes[i]));
      }
      return items;
   }

   public String getBrush(String language)
   {
      for (int i = 0; i != languages.length; i++)
      {
         if (languages[i][LANG_EXTENSION].equals(language))
            return languages[i][LANG_BRUSH];
      }
      return languages[0][LANG_BRUSH];
   }
}
