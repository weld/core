package org.jboss.weld.invokable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public final class ConfiguredLookup {
    public final int position; // -1 for instance lookup, >= 0 for argument lookups
    public final Type type;
    public final Annotation[] qualifiers;

    ConfiguredLookup(int position, Type type, Annotation[] qualifiers) {
        this.position = position;
        this.type = type;
        this.qualifiers = qualifiers;
    }

    public boolean isInstanceLookup() {
        return position < 0;
    }
}
