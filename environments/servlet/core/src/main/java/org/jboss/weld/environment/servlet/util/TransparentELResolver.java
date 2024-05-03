package org.jboss.weld.environment.servlet.util;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;

/**
 * An ELResolver that behaves as though it is invisible, meaning it's
 * idempotent to the chain and the next ELResolver in the line will be
 * consulted.
 *
 * @author Dan Allen
 */
public class TransparentELResolver extends ELResolver {
    @Override
    public Class<?> getCommonPropertyType(ELContext arg0, Object arg1) {
        return null;
    }

    @Override
    public Class<?> getType(ELContext arg0, Object arg1, Object arg2) {
        return null;
    }

    @Override
    public Object getValue(ELContext arg0, Object arg1, Object arg2) {
        return null;
    }

    @Override
    public boolean isReadOnly(ELContext arg0, Object arg1, Object arg2) {
        return false;
    }

    @Override
    public void setValue(ELContext arg0, Object arg1, Object arg2, Object arg3) {
    }

}
