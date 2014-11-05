package org.jboss.weld.metadata;

import java.util.Collection;
import java.util.function.Predicate;

public class ScanningPredicate<T> implements Predicate<T> {

    private final Collection<Predicate<T>> includes;
    private final Collection<Predicate<T>> excludes;

    public ScanningPredicate(Collection<Predicate<T>> includes, Collection<Predicate<T>> excludes) {
        this.includes = includes;
        this.excludes = excludes;
    }

    public boolean test(T input) {
        // Initial state - all classes are included if no includes are specified
        boolean apply = includes.isEmpty();

        for (Predicate<T> include : includes) {
            // If any include matches, we should include the class
            if (include.test(input)) {
                apply = true;
            }
        }
        for (Predicate<T> exclude : excludes) {
            // If any exclude matches, we exclude the class - we can then short-circuit
            if (exclude.test(input)) {
                return false;
            }
        }
        return apply;
    }

}
