package org.jboss.weld.examples.pastecode.jsf;

import javax.faces.convert.EnumConverter;
import javax.faces.convert.FacesConverter;

import org.jboss.weld.examples.pastecode.model.Language;

/**
 * JSF converter responsible for converting the {@link Language} enum to and from Strings
 * 
 * @author Pete Muir
 *
 */
@FacesConverter(forClass=Language.class)
public class LanguageConverter extends EnumConverter
{
   public LanguageConverter()
   {
      super(Language.class);
   }
}
