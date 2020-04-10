package org.jboss.weld.examples.pastecode.jsf;

import org.jboss.weld.examples.pastecode.model.Language;

import jakarta.faces.convert.EnumConverter;
import jakarta.faces.convert.FacesConverter;

/**
 * JSF converter responsible for converting the {@link Language} enum to and from Strings
 *
 * @author Pete Muir
 */
@FacesConverter(forClass = Language.class)
public class LanguageConverter extends EnumConverter {
    public LanguageConverter() {
        super(Language.class);
    }
}
