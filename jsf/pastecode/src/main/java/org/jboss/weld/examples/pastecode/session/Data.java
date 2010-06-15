/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
public class Data implements Serializable
{
   private static final long serialVersionUID = 991300443278089016L;

   private int LANG_EXTENSION = 0; // e.g. txt
   private int LANG_NAME = 1; // e.g. Plain text
   private int LANG_BRUSH = 2; // e.g. text
   private String languages[][] = { { "txt", "Plain text", "text" }, { "as3", "AS3", "as3" }, { "sh", "Bash", "bash" }, { "cs", "C#", "csharp" }, { "cf", "Cold Fusion", "coldfusion" }, { "cpp", "C++", "cpp" }, { "css", "CSS", "css" }, { "pas", "Delphi", "pas" }, { "diff", "Diff", "diff" }, { "erl", "Erlang", "erl" }, { "groovy", "Groovy", "groovy" }, { "js", "JavaScript", "js" }, { "java", "Java", "java" }, { "fx", "JavaFX", "javafx" }, { "perl", "Perl", "perl" }, { "php", "PHP", "php" }, { "ps1", "Power Shell", "powershell" }, { "py", "Python", "py" }, { "rb", "Ruby", "rb" }, { "scl", "Scala", "scala" }, { "sql", "Sql", "sql" }, { "vb", "Visual Basic", "vb" }, { "xml", "XML", "xml" } };
   private String themes[] = { "Default Theme", "Django Theme", "Eclipse Theme", "Emacs Theme", "Midnight Theme", "Dark Theme" };

   public Data()
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
