package org.jboss.weld.examples.pastecode.jsf;

import javax.faces.convert.EnumConverter;

import org.jboss.weld.examples.pastecode.model.Language;

public class LanguageConverter extends EnumConverter
{
   public LanguageConverter()
   {
      super(Language.class);
   }
}
