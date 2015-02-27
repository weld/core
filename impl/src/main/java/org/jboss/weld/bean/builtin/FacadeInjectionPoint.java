package org.jboss.weld.bean.builtin;

import static org.jboss.weld.util.Beans.mergeInQualifiers;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.injection.ForwardingInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.ParameterizedTypeImpl;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class FacadeInjectionPoint extends ForwardingInjectionPoint implements Serializable {

    private static final long serialVersionUID = -4102173765226078459L;

    @SuppressWarnings(value = "SE_BAD_FIELD", justification = "Depends on realization of injectionPoint")
    private final InjectionPoint injectionPoint;
    private final Type type;
    private final Set<Annotation> qualifiers;

    public FacadeInjectionPoint(BeanManagerImpl manager, InjectionPoint injectionPoint, Type subtype, Set<Annotation> existingQualifiers, Annotation[] newQualifiers) {
        this.injectionPoint = injectionPoint;
        this.type = new ParameterizedTypeImpl(Instance.class, new Type[]{subtype}, null);
        this.qualifiers = mergeInQualifiers(manager, existingQualifiers, newQualifiers);
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