package org.jboss.weld.metadata;

import java.security.AccessController;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.jboss.weld.bootstrap.spi.ClassAvailableActivation;
import org.jboss.weld.bootstrap.spi.Filter;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.SystemPropertyActivation;
import org.jboss.weld.bootstrap.spi.WeldFilter;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.security.GetSystemPropertyAction;

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
    private final Matcher matcher;

    public FilterPredicate(Metadata<Filter> filter, ResourceLoader resourceLoader) {
        boolean active = true;
        if (filter.getValue().getClassAvailableActivations() != null) {
            for (Metadata<ClassAvailableActivation> classAvailableActivation : filter.getValue()
                    .getClassAvailableActivations()) {
                if (classAvailableActivation.getValue() == null) {
                    throw new IllegalStateException(
                            "Class available activation metadata not available at " + classAvailableActivation);
                }
                String className = classAvailableActivation.getValue().getClassName();
                if (className == null) {
                    throw new IllegalStateException("Must specify class name at " + classAvailableActivation);
                }
                boolean inverted = isInverted(className) || classAvailableActivation.getValue().isInverted();
                if (inverted) {
                    className = removeInversion(className);
                }
                active = active && isClassAvailable(className, resourceLoader, inverted);
            }
        }
        if (filter.getValue().getSystemPropertyActivations() != null) {
            for (Metadata<SystemPropertyActivation> systemPropertyActivation : filter.getValue()
                    .getSystemPropertyActivations()) {
                if (systemPropertyActivation.getValue() == null) {
                    throw new IllegalStateException(
                            "System property activation metadata not available at " + systemPropertyActivation);
                }
                String propertyName = systemPropertyActivation.getValue().getName();
                String requiredPropertyValue = systemPropertyActivation.getValue().getValue();
                if (propertyName == null) {
                    throw new IllegalStateException("Must specify system property name at " + systemPropertyActivation);
                }
                boolean propertyNameInverted = isInverted(propertyName);

                if (propertyNameInverted && requiredPropertyValue != null) {
                    throw new IllegalStateException(
                            "Cannot invert property name and specify property value at " + systemPropertyActivation);
                }

                if (propertyNameInverted) {
                    propertyName = removeInversion(propertyName);
                }

                String actualPropertyValue = AccessController.doPrivileged(new GetSystemPropertyAction(propertyName));
                if (requiredPropertyValue == null) {
                    active = active && isNotNull(actualPropertyValue, propertyNameInverted);
                } else {
                    boolean requiredPropertyValueInverted = isInverted(requiredPropertyValue);
                    if (requiredPropertyValueInverted) {
                        requiredPropertyValue = removeInversion(requiredPropertyValue);
                    }
                    active = active && isEqual(requiredPropertyValue, actualPropertyValue, requiredPropertyValueInverted);
                }
            }
        }
        this.active = active;
        if (filter.getValue() instanceof WeldFilter) {
            WeldFilter weldFilter = (WeldFilter) filter.getValue();
            if ((weldFilter.getName() != null && weldFilter.getPattern() != null)
                    || (weldFilter.getName() == null && weldFilter.getPattern() == null)) {
                throw new IllegalStateException("Cannot specify both a pattern and a name at " + filter);
            }
            if (weldFilter.getPattern() != null) {
                this.matcher = new PatternMatcher(filter, weldFilter.getPattern());
            } else {
                this.matcher = new AntSelectorMatcher(weldFilter.getName());
            }
        } else {
            if (filter.getValue().getName() == null) {
                throw new IllegalStateException("Name must be specified at " + filter);
            }
            String name = filter.getValue().getName();
            String suffixDotDoubleStar = ".**";
            String suffixDotStar = ".*";
            if (name.endsWith(suffixDotDoubleStar)) {
                this.matcher = new PrefixMatcher(name.substring(0, name.length() - suffixDotDoubleStar.length()), filter);
            } else if (name.endsWith(suffixDotStar)) {
                this.matcher = new PackageMatcher(name.substring(0, name.length() - suffixDotStar.length()), filter);
            } else {
                this.matcher = new FullyQualifierClassNameMatcher(name, filter);
            }
        }
    }

    public boolean test(String className) {
        if (active) {
            return matcher.matches(className);
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
            return string;
        }
        return string.substring(1);
    }

    private interface Matcher {
        boolean matches(String input);
    }

    private static class PatternMatcher implements Matcher {
        private final Pattern pattern;

        private PatternMatcher(Metadata<Filter> filter, String pattern) {
            try {
                this.pattern = Pattern.compile(pattern);
            } catch (PatternSyntaxException e) {
                throw new IllegalStateException("Error parsing pattern at " + filter, e);
            }
        }

        @Override
        public boolean matches(String input) {
            return pattern.matcher(input).matches();
        }
    }

    private static class AntSelectorMatcher implements Matcher {
        private final String name;

        private AntSelectorMatcher(String name) {
            this.name = name;
        }

        @Override
        public boolean matches(String input) {
            return Selectors.matchPath(this.name, input);
        }
    }

    private abstract static class CDI11Matcher implements Matcher {
        private static final Pattern CDI11_EXCLUDE_PATTERN = Pattern
                .compile("([\\p{L}_$][\\p{L}\\p{N}_$]*\\.)*[\\p{L}_$][\\p{L}\\p{N}_$]*");
        protected final String expression;

        private CDI11Matcher(String expression, Metadata<Filter> filter) {
            this.expression = expression;
            if (!CDI11_EXCLUDE_PATTERN.matcher(expression).matches()) {
                throw new IllegalArgumentException("Invalid expression " + filter);
            }
        }
    }

    private static class FullyQualifierClassNameMatcher extends CDI11Matcher {

        private FullyQualifierClassNameMatcher(String fqcn, Metadata<Filter> filter) {
            super(fqcn, filter);
        }

        @Override
        public boolean matches(String input) {
            return expression.equals(input);
        }
    }

    private static class PrefixMatcher extends CDI11Matcher {

        private PrefixMatcher(String prefix, Metadata<Filter> filter) {
            super(prefix, filter);
        }

        @Override
        public boolean matches(String input) {
            return input != null && input.startsWith(expression);
        }
    }

    private static class PackageMatcher extends CDI11Matcher {

        private PackageMatcher(String pkg, Metadata<Filter> filter) {
            super(pkg, filter);
        }

        @Override
        public boolean matches(String input) {
            if (input == null) {
                return false;
            }
            int lastDot = input.lastIndexOf('.');
            if (lastDot == -1) {
                return false;
            }
            return expression.equals(input.substring(0, lastDot));
        }
    }
}
