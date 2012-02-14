/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.test.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.el.ELContext;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.bean.proxy.ProxyObject;
import org.jboss.weld.exceptions.UnsatisfiedResolutionException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.test.util.el.EL;
import org.jboss.weld.util.collections.EnumerationList;
import org.jboss.weld.util.reflection.Reflections;

import static org.jboss.weld.logging.messages.BeanManagerMessage.UNRESOLVABLE_TYPE;


public class Utils {

    /**
     * Checks if all annotations are in a given set of annotations
     *
     * @param annotations     The annotation set
     * @param annotationTypes The annotations to match
     * @return True if match, false otherwise
     */
    public static boolean annotationSetMatches(Set<? extends Annotation> annotations, Class<? extends Annotation>... annotationTypes) {
        List<Class<? extends Annotation>> annotationTypeList = new ArrayList<Class<? extends Annotation>>();
        annotationTypeList.addAll(Arrays.asList(annotationTypes));
        for (Annotation annotation : annotations) {
            if (annotationTypeList.contains(annotation.annotationType())) {
                annotationTypeList.remove(annotation.annotationType());
            } else {
                return false;
            }
        }
        return annotationTypeList.size() == 0;
    }

    public static boolean typeSetMatches(Set<Type> types, Type... requiredTypes) {
        List<Type> typeList = Arrays.asList(requiredTypes);
        return requiredTypes.length == types.size() && types.containsAll(typeList);
    }

    public static Iterable<URL> getResources(Class<?> clazz, String name) {
        if (name.startsWith("/")) {
            name = name.substring(1);
        } else {
            name = clazz.getPackage().getName().replace(".", "/") + "/" + name;
        }
        try {
            return new EnumerationList<URL>(clazz.getClassLoader().getResources(name));
        } catch (IOException e) {
            throw new RuntimeException("Error loading resource from classloader" + name, e);
        }
    }

    public static byte[] serialize(Object instance) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        out.writeObject(instance);
        return bytes.toByteArray();
    }

    public static <T> T deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes)) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                try {
                    return super.resolveClass(desc);
                } catch (ClassNotFoundException e) {
                    // if a weld-internal class is not available (JBoss AS 7) let's use the weld classloader to load them
                    return BeanManagerImpl.class.getClassLoader().loadClass(desc.getName());
                }
            }
        };
        return Reflections.<T>cast(in.readObject());
    }

    public static boolean isExceptionInHierarchy(Throwable exception, Class<? extends Throwable> expectedException) {
        while (exception != null) {
            if (exception.getClass().equals(expectedException)) {
                return true;
            }
            exception = exception.getCause();
        }
        return false;
    }

    public static <T> Bean<T> getBean(BeanManager beanManager, Type beanType, Annotation... bindings) {
        Set<Bean<?>> beans = beanManager.getBeans(beanType, bindings);
        Bean<?> bean = beanManager.resolve(beans);
        if (bean == null) {
            throw new UnsatisfiedResolutionException(UNRESOLVABLE_TYPE, beanType, Arrays.toString(bindings));
        }

        @SuppressWarnings("unchecked")
        Bean<T> typedBean = (Bean<T>) bean;

        return typedBean;
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<Bean<T>> getBeans(BeanManager beanManager, Class<T> type, Annotation... bindings) {
        return (Set) beanManager.getBeans(type, bindings);
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<Bean<T>> getBeans(BeanManager beanManager, TypeLiteral<T> type, Annotation... bindings) {
        return (Set) beanManager.getBeans(type.getType(), bindings);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getReference(BeanManager beanManager, Class<T> beanType, Annotation... bindings) {
        Bean<?> bean = getBean(beanManager, beanType, bindings);
        return (T) beanManager.getReference(bean, beanType, beanManager.createCreationalContext(bean));
    }

    @SuppressWarnings("unchecked")
    public static <T> T getReference(BeanManager beanManager, Bean<T> bean) {
        return (T) beanManager.getReference(bean, bean.getBeanClass(), beanManager.createCreationalContext(bean));
    }

    @SuppressWarnings("unchecked")
    public static <T> T evaluateValueExpression(BeanManagerImpl beanManager, String expression, Class<T> expectedType) {
        ELContext elContext = EL.createELContext(beanManager.getCurrent());
        return (T) EL.EXPRESSION_FACTORY.createValueExpression(elContext, expression, expectedType).getValue(elContext);
    }

    public static boolean isProxy(Object proxy) {
        return proxy instanceof ProxyObject;
    }


    public static <T extends Context> T getActiveContext(WeldManager beanManager, Class<T> type) {
        for (T context : beanManager.instance().select(type)) {
            if (context.isActive()) {
                return context;
            }
        }
        throw new ContextNotActiveException();
    }

}
