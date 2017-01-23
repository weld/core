/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.probe;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Vetoed;
import javax.management.DynamicMBean;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

import org.jboss.weld.resources.WeldClassLoaderResourceLoader;

/**
 * This is a wrapper {@link DynamicMBean} which allows to supply some description metadata for MXBean components.
 *
 * @author Martin Kouba
 */
@Vetoed
class ProbeDynamicMBean extends StandardMBean {

    private static final Map<String, Class<?>> PRIMITIVES_MAP = new HashMap<>();

    private final Class<?> mbeanInterface;

    {
        PRIMITIVES_MAP.put(byte.class.getName(), byte.class);
        PRIMITIVES_MAP.put(short.class.getName(), short.class);
        PRIMITIVES_MAP.put(int.class.getName(), int.class);
        PRIMITIVES_MAP.put(long.class.getName(), long.class);
        PRIMITIVES_MAP.put(float.class.getName(), float.class);
        PRIMITIVES_MAP.put(double.class.getName(), double.class);
        PRIMITIVES_MAP.put(char.class.getName(), char.class);
        PRIMITIVES_MAP.put(boolean.class.getName(), boolean.class);
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD, ElementType.PARAMETER })
    @interface Description {
        String value();
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.PARAMETER })
    @interface ParamName {
        String value();
    }

    <T> ProbeDynamicMBean(T implementation, Class<T> mbeanInterface) throws NotCompliantMBeanException {
        super(implementation, mbeanInterface, true);
        this.mbeanInterface = mbeanInterface;
    }

    @Override
    protected String getDescription(MBeanOperationInfo info) {
        Method found = findMethod(info);
        if (found != null) {
            Description description = found.getAnnotation(Description.class);
            if (description != null) {
                return description.value();
            }
        }
        return super.getDescription(info);
    }

    @Override
    protected String getParameterName(MBeanOperationInfo op, MBeanParameterInfo param, int sequence) {
        Method found = findMethod(op);
        if (found != null) {
            Annotation[] annotations = found.getParameterAnnotations()[sequence];
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(ParamName.class)) {
                    return ((ParamName) annotation).value();
                }
            }
        }
        return super.getParameterName(op, param, sequence);
    }

    @Override
    protected String getDescription(MBeanOperationInfo op, MBeanParameterInfo param, int sequence) {
        Method found = findMethod(op);
        if (found != null) {
            Annotation[] annotations = found.getParameterAnnotations()[sequence];
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(Description.class)) {
                    return ((Description) annotation).value();
                }
            }
        }
        return super.getDescription(op, param, sequence);
    }

    private Method findMethod(MBeanOperationInfo operationInfo) {
        try {
            return mbeanInterface.getMethod(operationInfo.getName(), toParamTypes(operationInfo.getSignature()));
        } catch (NoSuchMethodException | SecurityException e) {
            return null;
        }
    }

    private Class<?>[] toParamTypes(MBeanParameterInfo[] paramsInfo) {
        Class<?>[] paramTypes = new Class<?>[paramsInfo.length];
        for (int i = 0; i < paramsInfo.length; i++) {
            paramTypes[i] = classForName(paramsInfo[i].getType());
        }
        return paramTypes;
    }

    private Class<?> classForName(String name) {
        Class<?> clazz = PRIMITIVES_MAP.get(name);
        if (clazz == null) {
            clazz = WeldClassLoaderResourceLoader.INSTANCE.classForName(name);
        }
        return clazz;
    }

}
