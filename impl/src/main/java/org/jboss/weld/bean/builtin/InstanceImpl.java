/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.bean.builtin;

import org.jboss.weld.Container;
import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.injection.ForwardingInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resolution.Resolvable;
import org.jboss.weld.resolution.ResolvableBuilder;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.TypeLiteral;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import static org.jboss.weld.logging.messages.BeanMessage.PROXY_REQUIRED;

/**
 * Helper implementation for Instance for getting instances
 *
 * @param <T>
 * @author Gavin King
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_NO_SUITABLE_CONSTRUCTOR", justification = "Uses SerializationProxy")
public class InstanceImpl<T> extends AbstractFacade<T, Instance<T>> implements Instance<T>, Serializable {

    private static class InstanceInjectionPoint extends ForwardingInjectionPoint implements Serializable {

        private static final long serialVersionUID = -4102173765226078459L;

        private final InjectionPoint injectionPoint;
        private final Type type;
        private final Set<Annotation> qualifiers;

        public InstanceInjectionPoint(InjectionPoint injectionPoint, Type type, Set<Annotation> qualifiers) {
            this.injectionPoint = injectionPoint;
            this.type = type;
            this.qualifiers = qualifiers;
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

    private static final long serialVersionUID = -376721889693284887L;

    public static <I> Instance<I> of(InjectionPoint injectionPoint, CreationalContext<I> creationalContext, BeanManagerImpl beanManager) {
        return new InstanceImpl<I>(injectionPoint, creationalContext, beanManager);
    }

    private InstanceImpl(InjectionPoint injectionPoint, CreationalContext<? super T> creationalContext, BeanManagerImpl beanManager) {
        super(injectionPoint, creationalContext, beanManager);
    }

    public T get() {
        Resolvable resolvable = new ResolvableBuilder(getType(), getBeanManager())
            .addQualifiers(getQualifiers())
            .setDeclaringBean(getInjectionPoint().getBean())
            .create();
        Bean<?> bean = getBeanManager().getBean(resolvable);
        // Generate a correct injection point for the bean, we do this by taking the original injection point and adjusting the qualifiers and type
        InjectionPoint ip = new InstanceInjectionPoint(getInjectionPoint(), getType(), getQualifiers());
        CurrentInjectionPoint currentInjectionPoint = Container.instance(getBeanManager().getContextId()).services().get(CurrentInjectionPoint.class);
        try {
            currentInjectionPoint.push(ip);
            return Reflections.<T>cast(getBeanManager().getReference(bean, getType(), getCreationalContext()));
        } finally {
            currentInjectionPoint.pop();
        }
    }

    /**
     * Gets a string representation
     *
     * @return A string representation
     */
    @Override
    public String toString() {
        return Formats.formatAnnotations(getQualifiers()) + " Instance<" + Formats.formatType(getType()) + ">";
    }

    private Set<Bean<?>> getBeans() {
        return getBeanManager().getBeans(getType(), getQualifiers());
    }

    public Iterator<T> iterator() {
        Collection<T> instances = new ArrayList<T>();
        for (Bean<?> bean : getBeans()) {
            // Don't return the InjectionPoint bean, it's not a possible to inject an instance of that!
            if (!InjectionPoint.class.isAssignableFrom(bean.getBeanClass())) {
                Object object = getBeanManager().getReference(bean, getType(), getBeanManager().createCreationalContext(bean));
                instances.add(Reflections.<T>cast(object));
            }
        }
        return instances.iterator();
    }

    public boolean isAmbiguous() {
        return getBeans().size() > 1;
    }

    public boolean isUnsatisfied() {
        return getBeans().size() == 0;
    }

    public Instance<T> select(Annotation... qualifiers) {
        return selectInstance(this.getType(), qualifiers);
    }

    public <U extends T> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
        return selectInstance(subtype, qualifiers);
    }

    public <U extends T> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
        return selectInstance(subtype.getType(), qualifiers);
    }

    private <U extends T> Instance<U> selectInstance(Type subtype, Annotation[] newQualifiers) {
        InjectionPoint modifiedInjectionPoint = new FacadeInjectionPoint(getBeanManager().getContextId(), getInjectionPoint(), subtype, getQualifiers(), newQualifiers);
        return new InstanceImpl<U>(modifiedInjectionPoint, getCreationalContext(), getBeanManager());
    }

    // Serialization

    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<T>(this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException(PROXY_REQUIRED);
    }

    private static class SerializationProxy<T> extends AbstractFacadeSerializationProxy<T, Instance<T>> {

        private static final long serialVersionUID = 9181171328831559650L;

        public SerializationProxy(InstanceImpl<T> instance) {
            super(instance);
        }

        private Object readResolve() {
            return InstanceImpl.of(getInjectionPoint(), getCreationalContext(), getBeanManager());
        }

    }

}
