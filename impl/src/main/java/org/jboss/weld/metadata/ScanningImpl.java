/**
 *
 */
package org.jboss.weld.metadata;

import java.util.Collection;

import org.jboss.weld.bootstrap.spi.Filter;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.Scanning;

public class ScanningImpl implements Scanning {

    private final Collection<Metadata<Filter>> includes;
    private final Collection<Metadata<Filter>> excludes;

    public ScanningImpl(Collection<Metadata<Filter>> includes, Collection<Metadata<Filter>> excludes) {
        this.includes = includes;
        this.excludes = excludes;
    }

    public Collection<Metadata<Filter>> getExcludes() {
        return excludes;
    }

    public Collection<Metadata<Filter>> getIncludes() {
        return includes;
    }

    @Override
    public String toString() {
        return "Includes: " + includes + "; Excludes: " + excludes;
    }

}
