package org.jboss.weld.examples.pastecode.jsf;

import javax.faces.convert.EnumConverter;
import javax.faces.convert.FacesConverter;

import org.jboss.weld.examples.pastecode.model.Language;

@FacesConverter(forClass=Language.class)
public class LanguageConverter extends EnumConverter
{
   public LanguageConverter()
   {
      super(Language.class);
   }
}
