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
package org.jboss.weld.module.ejb;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jakarta.ejb.EJBException;
import jakarta.ejb.NoSuchEJBException;

import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.jlr.MethodSignatureImpl;
import org.jboss.weld.bean.proxy.Marker;
import org.jboss.weld.bean.proxy.MethodHandler;
import org.jboss.weld.ejb.api.SessionObjectReference;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.logging.SerializationLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.util.collections.ImmutableMap;
import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Method handler for enterprise bean client proxies
 *
 * @author Nicklas Karlsson
 * @author Pete Muir
 * @author Marko Luksa
 */
class EnterpriseBeanProxyMethodHandler<T> implements MethodHandler, Serializable {

    private static final long serialVersionUID = 2107723373882153667L;

    private final BeanManagerImpl manager;
    private final BeanIdentifier beanId;
    private final SessionObjectReference reference;

    private final transient SessionBeanImpl<T> bean;

    private final transient Map<Class<?>, Class<?>> typeToBusinessInterfaceMap;

    /**
     * Constructor
     *
     * @param bean the session bean
     */
    EnterpriseBeanProxyMethodHandler(SessionBeanImpl<T> bean) {
        this(bean, null);
    }

    /**
     * Constructor used directly by {@link #readResolve()}.
     *
     * @param bean      the session bean
     * @param reference session object reference or {@code null} to create a new reference
     */
    private EnterpriseBeanProxyMethodHandler(SessionBeanImpl<T> bean, SessionObjectReference reference) {
        this.bean = bean;
        this.manager = bean.getBeanManager();
        this.beanId = bean.getIdentifier();

        Map<Class<?>, Class<?>> typeToBusinessInterfaceMap = new HashMap<Class<?>, Class<?>>();
        discoverBusinessInterfaces(typeToBusinessInterfaceMap, bean.getEjbDescriptor().getRemoteBusinessInterfacesAsClasses());
        discoverBusinessInterfaces(typeToBusinessInterfaceMap, bean.getEjbDescriptor().getLocalBusinessInterfacesAsClasses());
        this.typeToBusinessInterfaceMap = ImmutableMap.copyOf(typeToBusinessInterfaceMap);

        if (reference == null) {
            this.reference = bean.createReference();
            BeanLogger.LOG.createdSessionBeanProxy(bean);
        } else {
            this.reference = reference;
            BeanLogger.LOG.activatedSessionBeanProxy(bean);
        }
    }

    /**
     * Looks up the EJB in the container and executes the method on it
     *
     * @param self    the proxy instance.
     * @param method  the overridden method declared in the super class or
     *                interface.
     * @param proceed the forwarder method for invoking the overridden method. It
     *                is null if the overridden method is abstract or declared in the
     *                interface.
     * @param args    an array of objects containing the values of the arguments
     *                passed in the method invocation on the proxy instance. If a
     *                parameter type is a primitive type, the type of the array
     *                element is a wrapper class.
     * @return the resulting value of the method invocation.
     * @throws Throwable if the method invocation fails.
     */
    @Override
    public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable {
        if ("destroy".equals(method.getName()) && Marker.isMarker(0, method, args)) {
            if (bean.getEjbDescriptor().isStateful()) {
                if (!reference.isRemoved()) {
                    reference.remove();
                }
            }
            return null;
        }

        if (!bean.isClientCanCallRemoveMethods() && isRemoveMethod(method)) {
            throw BeanLogger.LOG.invalidRemoveMethodInvocation(method);
        }
        Class<?> businessInterface = getBusinessInterface(method);
        Object proxiedInstance = reference.getBusinessObject(businessInterface);

        if (!Modifier.isPublic(method.getModifiers())) {
            throw new EJBException("Not a business method " + method.toString() +". Do not call non-public methods on EJB's.");
        }
        try {
            Object returnValue = Reflections.invokeAndUnwrap(proxiedInstance, method, args);
            BeanLogger.LOG.callProxiedMethod(method, proxiedInstance, args, returnValue);
            return returnValue;
        } catch (NoSuchEJBException e) {
            if (isToStringMethod(method)) {
                return businessInterface.getName() + " [REMOVED]";
            }
            throw e;
        }
    }

    private boolean isRemoveMethod(Method method) {
        MethodSignature methodSignature = new MethodSignatureImpl(method);
        return bean.getEjbDescriptor().getRemoveMethodSignatures().contains(methodSignature);
    }

    private boolean isToStringMethod(Method method) {
        return "toString".equals(method.getName()) && method.getParameterCount() == 0;
    }

    private Class<?> getBusinessInterface(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        Class<?> businessInterface = null;

        if (declaringClass.equals(Object.class)) {
            businessInterface = bean.getEjbDescriptor().getObjectInterface();
        } else {
            // This will likely not work perfectly if there is a session bean with more than one views extending/implementing the declaringClass
            businessInterface = typeToBusinessInterfaceMap.get(declaringClass);
        }

        if (businessInterface == null) {
            throw new RuntimeException("Unable to locate a business interface declaring " + method);
        }
        return businessInterface;
    }

    @SuppressWarnings("unchecked")
    private Object readResolve() throws ObjectStreamException {
        try {
            return new EnterpriseBeanProxyMethodHandler<T>((SessionBeanImpl<T>) manager.getPassivationCapableBean(beanId), reference);
        } catch (Exception e) {
            throw SerializationLogger.LOG.unableToDeserialize(beanId, e);
        }
    }

    private void discoverBusinessInterfaces(Map<Class<?>, Class<?>> typeToBusinessInterfaceMap, Set<Class<?>> businessInterfaces) {
        for (Class<?> businessInterfaceClass : businessInterfaces) {
            for (Class<?> type : HierarchyDiscovery.forNormalizedType(businessInterfaceClass).getTypeMap().keySet()) {
                typeToBusinessInterfaceMap.put(type, businessInterfaceClass);
            }
        }
    }
}
