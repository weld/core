package org.jboss.weld.metadata;

import com.google.common.base.Predicate;
import org.jboss.weld.bootstrap.spi.ClassAvailableActivation;
import org.jboss.weld.bootstrap.spi.Filter;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.SystemPropertyActivation;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A predicate which selects classes to process based on a filter.
 * <p/>
 * This filter will determine if the filter is active on instantiation, so
 * should only be instantiated when it is ready to be used.
 *
 * @author Pete Muir
 */
public class FilterPredicate implements Predicate<String> {

    private final boolean active;
    private final Pattern pattern;
    private final String name;

    public FilterPredicate(Metadata<Filter> filter, ResourceLoader resourceLoader) {
        boolean active = true;
        if (filter.getValue().getClassAvailableActivations() != null) {
            for (Metadata<ClassAvailableActivation> classAvailableActivation : filter.getValue().getClassAvailableActivations()) {
                if (classAvailableActivation.getValue() == null) {
                    throw new IllegalStateException("Class available activation metadata not available at " + classAvailableActivation);
                }
                String className = classAvailableActivation.getValue().getClassName();
                if (className == null) {
                    throw new IllegalStateException("Must specify class name at " + classAvailableActivation);
                }
                boolean inverted = isInverted(className);
                if (inverted) {
                    className = removeInversion(className);
                }
                active = active && isClassAvailable(className, resourceLoader, inverted);
            }
        }
        if (filter.getValue().getSystemPropertyActivations() != null) {
            for (Metadata<SystemPropertyActivation> systemPropertyActivation : filter.getValue().getSystemPropertyActivations()) {
                if (systemPropertyActivation.getValue() == null) {
                    throw new IllegalStateException("System property activation metadata not available at " + systemPropertyActivation);
                }
                String propertyName = systemPropertyActivation.getValue().getName();
                String requiredPropertyValue = systemPropertyActivation.getValue().getValue();
                if (propertyName == null) {
                    throw new IllegalStateException("Must specify system property name at " + systemPropertyActivation);
                }
                boolean propertyNameInverted = isInverted(propertyName);

                if (propertyNameInverted && requiredPropertyValue != null) {
                    throw new IllegalStateException("Cannot invert property name and specify property value at " + systemPropertyActivation);
                }

                if (propertyNameInverted) {
                    propertyName = removeInversion(propertyName);
                }

                if (requiredPropertyValue == null) {
                    active = active && isNotNull(System.getProperty(propertyName), propertyNameInverted);
                } else {
                    boolean requiredPropertyValueInverted = isInverted(requiredPropertyValue);
                    if (requiredPropertyValueInverted) {
                        requiredPropertyValue = removeInversion(requiredPropertyValue);
                    }
                    active = active && isEqual(requiredPropertyValue, System.getProperty(propertyName), requiredPropertyValueInverted);
                }
            }
        }
        this.active = active;
        if (filter.getValue().getPattern() != null) {
            this.name = null;
            try {
                this.pattern = Pattern.compile(filter.getValue().getPattern());
            } catch (PatternSyntaxException e) {
                throw new IllegalStateException("Error parsing pattern at " + filter, e);
            }
        } else if (filter.getValue().getName() != null) {
            this.name = filter.getValue().getName();
            this.pattern = null;
        } else if (filter.getValue().getPattern() != null && filter.getValue().getName() != null) {
            throw new IllegalStateException("Cannot specify both a pattern and a name at " + filter);
        } else {
            throw new IllegalStateException("Must specify one of a pattern and a name at " + filter);
        }
    }

    public boolean apply(String className) {
        if (active) {
            if (pattern != null) {
                return pattern.matcher(className).matches();
            } else {
                return Selectors.matchPath(this.name, className);
            }
        } else {
            return false;
        }
    }

    private static boolean isClassAvailable(String className, ResourceLoader resourceLoader, boolean invert) {
        if (invert) {
            return !isClassAvailable(className, resourceLoader);
        } else {
            return isClassAvailable(className, resourceLoader);
        }
    }

    private static boolean isClassAvailable(String className, ResourceLoader resourceLoader) {
        try {
            resourceLoader.classForName(className);
        } catch (ResourceLoadingException e) {
            return false;
        }
        return true;
    }

    private static boolean isNotNull(String string, boolean invert) {
        if (invert) {
            return string == null;
        } else {
            return string != null;
        }
    }

    private static boolean isEqual(String string1, String string2, boolean invert) {
        if (invert) {
            return !string1.equals(string2);
        } else {
            return string1.equals(string2);
        }
    }

    private static boolean isInverted(String string) {
        return string.startsWith("!");
    }

    private static String removeInversion(String string) {
        if (!string.startsWith("!")) {
            throw new IllegalStateException("Cannot remove inversion from non-inverted string [" + string + "]");
        }
        return string.substring(1);
    }

}
