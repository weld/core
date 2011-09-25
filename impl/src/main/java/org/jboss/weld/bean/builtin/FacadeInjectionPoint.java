package org.jboss.weld.bean.builtin;

import org.jboss.weld.injection.ForwardingInjectionPoint;
import org.jboss.weld.util.reflection.ParameterizedTypeImpl;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.InjectionPoint;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import static org.jboss.weld.util.Beans.mergeInQualifiers;

public class FacadeInjectionPoint extends ForwardingInjectionPoint implements Serializable {

    private static final long serialVersionUID = -4102173765226078459L;

    private final InjectionPoint injectionPoint;
    private final Type type;
    private final Set<Annotation> qualifiers;

    public FacadeInjectionPoint(String contextId, InjectionPoint injectionPoint, Type subtype, Set<Annotation> existingQualifiers, Annotation[] newQualifiers) {
        this.injectionPoint = injectionPoint;
        this.type = new ParameterizedTypeImpl(Instance.class, new Type[]{subtype}, null);
        this.qualifiers = mergeInQualifiers(contextId, existingQualifiers, newQualifiers);
    }

    @Override
    protected InjectionPoint delegate() {
        return injectionPoint;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

}