package org.jboss.weld.metadata;

import com.google.common.base.Predicate;

import java.util.Collection;

public class ScanningPredicate<T> implements Predicate<T> {

    private final Collection<Predicate<T>> includes;
    private final Collection<Predicate<T>> excludes;

    public ScanningPredicate(Collection<Predicate<T>> includes, Collection<Predicate<T>> excludes) {
        this.includes = includes;
        this.excludes = excludes;
    }

    public boolean apply(T input) {
        // Initial state - all classes are included if no includes are specified
        boolean apply = includes.isEmpty();

        for (Predicate<T> include : includes) {
            // If any include matches, we should include the class
            if (include.apply(input)) {
                apply = true;
            }
        }
        for (Predicate<T> exclude : excludes) {
            // If any exclude matches, we exclude the class - we can then short-circuit
            if (exclude.apply(input)) {
                return false;
            }
        }
        return apply;
    }

}
