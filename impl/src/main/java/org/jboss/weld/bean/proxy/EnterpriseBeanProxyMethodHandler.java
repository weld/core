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
package org.jboss.weld.bean.proxy;

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.CALL_PROXIED_METHOD;
import static org.jboss.weld.logging.messages.BeanMessage.CREATED_SESSION_BEAN_PROXY;
import static org.jboss.weld.logging.messages.BeanMessage.INVALID_REMOVE_METHOD_INVOCATION;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;

import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.jlr.MethodSignatureImpl;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.ejb.api.SessionObjectReference;
import org.jboss.weld.exceptions.UnsupportedOperationException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.util.reflection.Reflections;
import org.slf4j.cal10n.LocLogger;

/**
 * Method handler for enterprise bean client proxies
 *
 * @author Nicklas Karlsson
 * @author Pete Muir
 * @author Marko Luksa
 */
public class EnterpriseBeanProxyMethodHandler<T> implements MethodHandler, Serializable {

    private static final long serialVersionUID = 2107723373882153667L;

    // The log provider
    private static final LocLogger log = loggerFactory().getLogger(BEAN);

    private final BeanManagerImpl manager;
    private final BeanIdentifier beanId;
    private final SessionObjectReference reference;

    private final transient SessionBean<T> bean;

    /**
     * Constructor
     *
     * @param bean the session bean
     * @param creationalContext
     */
    public EnterpriseBeanProxyMethodHandler(SessionBean<T> bean) {
        this.bean = bean;
        this.manager = bean.getBeanManager();
        this.beanId = bean.getIdentifier();
        this.reference = bean.createReference();
        log.trace(CREATED_SESSION_BEAN_PROXY, bean);
    }

    /**
     * Lookups the EJB in the container and executes the method on it
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
                if(!reference.isRemoved()) {
                    reference.remove();
                }
            }
            return null;
        }

        if (!bean.isClientCanCallRemoveMethods() && isRemoveMethod(method)) {
            throw new UnsupportedOperationException(INVALID_REMOVE_METHOD_INVOCATION, method);
        }
        Class<?> businessInterface = getBusinessInterface(method);
        if (reference.isRemoved() && isToStringMethod(method)) {
            return businessInterface.getName() + " [REMOVED]";
        }
        Object proxiedInstance = reference.getBusinessObject(businessInterface);

        Object returnValue = Reflections.invokeAndUnwrap(proxiedInstance, method, args);
        log.trace(CALL_PROXIED_METHOD, method, proxiedInstance, args, returnValue);
        return returnValue;
    }

    private boolean isRemoveMethod(Method method) {
        // TODO we can certainly optimize this search algorithm!
        MethodSignature methodSignature = new MethodSignatureImpl(method);
        return bean.getEjbDescriptor().getRemoveMethodSignatures().contains(methodSignature);
    }

    private boolean isToStringMethod(Method method) {
        return "toString".equals(method.getName()) && method.getParameterTypes().length == 0;
    }

    private Class<?> getBusinessInterface(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass.equals(Object.class)) {
            return bean.getEjbDescriptor().getObjectInterface();
        }
        if (bean.getEjbDescriptor().getLocalBusinessInterfacesAsClasses().contains(declaringClass)) {
            return declaringClass;
        }
        // TODO we can certainly optimize this search algorithm!
        for (Class<?> view : bean.getEjbDescriptor().getLocalBusinessInterfacesAsClasses()) {
            for (Class<?> currentClass = view; currentClass != Object.class && currentClass != null; currentClass = currentClass.getSuperclass()) {
                if (currentClass.equals(view)) {
                    return view;
                }
            }
        }
        throw new RuntimeException("Unable to locate a business interface declaring " + method);
    }

    @SuppressWarnings("unchecked")
    private Object readResolve() throws ObjectStreamException {
        return new EnterpriseBeanProxyMethodHandler<T>((SessionBean<T>) manager.getPassivationCapableBean(beanId));
    }
}
