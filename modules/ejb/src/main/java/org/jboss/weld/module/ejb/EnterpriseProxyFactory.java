/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;

import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.proxy.CommonProxiedMethodFilters;
import org.jboss.weld.bean.proxy.Marker;
import org.jboss.weld.bean.proxy.MethodHandler;
import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.util.collections.ImmutableSet;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

/**
 * This factory produces client proxies specific for enterprise beans, in
 * particular session beans. It adds the interface
 * {@link EnterpriseBeanInstance} on the proxy.
 *
 * @author David Allen
 */
class EnterpriseProxyFactory<T> extends ProxyFactory<T> {

    private static final String SUFFIX = "$EnterpriseProxy$";

    /**
     * Produces a factory for a specific bean implementation.
     *
     * @param proxiedBeanType the actual enterprise bean
     */
    EnterpriseProxyFactory(Class<T> proxiedBeanType, SessionBeanImpl<T> bean) {
        super(bean.getBeanManager().getContextId(), proxiedBeanType, ImmutableSet.<Type> builder().addAll(bean.getTypes())
                .addAll(bean.getEjbDescriptor().getRemoteBusinessInterfacesAsClasses()).build(), bean);
    }

    @Override
    protected void addAdditionalInterfaces(Set<Class<?>> interfaces) {
        super.addAdditionalInterfaces(interfaces);
        // Add the EnterpriseBeanInstance interface
        interfaces.add(EnterpriseBeanInstance.class);
    }

    @Override
    protected void addSpecialMethods(ClassCreator cc) {
        super.addSpecialMethods(cc);
        // Add the destroy() method from EnterpriseBeanInstance interface
        generateDestroyMethod(cc);
    }

    /**
     * Generates the destroy() method from EnterpriseBeanInstance interface.
     * This method delegates to the method handler which will handle the actual destruction.
     */
    private void generateDestroyMethod(ClassCreator cc) {
        try {
            Method destroyMethod = EnterpriseBeanInstance.class.getMethod("destroy",
                    Marker.class, SessionBean.class, CreationalContext.class);
            BeanLogger.LOG.addingMethodToEnterpriseProxy(destroyMethod);

            cc.method(destroyMethod.getName(), m -> {
                m.public_();
                m.returning(void.class);
                var markerParam = m.parameter("marker", Marker.class);
                var sessionBeanParam = m.parameter("enterpriseBean", SessionBean.class);
                var contextParam = m.parameter("creationalContext", CreationalContext.class);

                m.body(b -> {
                    // Get the method handler field
                    FieldDesc methodHandlerField = FieldDesc.of(
                            cc.type(),
                            METHOD_HANDLER_FIELD_NAME,
                            getMethodHandlerType());
                    Expr handler = b.get(m.this_().field(methodHandlerField));

                    // Get the Method object for destroy()
                    // EnterpriseBeanInstance.class.getMethod("destroy", ...)
                    Expr enterpriseBeanInstanceClass = Const.of(EnterpriseBeanInstance.class);
                    Expr methodName = Const.of("destroy");

                    // Create parameter types array
                    Expr paramTypesArray = b.newEmptyArray(Class.class, 3);
                    var paramTypesVar = b.localVar("paramTypes", paramTypesArray);
                    b.set(paramTypesVar.elem(0), Const.of(Marker.class));
                    b.set(paramTypesVar.elem(1), Const.of(SessionBean.class));
                    b.set(paramTypesVar.elem(2), Const.of(CreationalContext.class));

                    MethodDesc getMethodDesc = MethodDesc.of(
                            Class.class, "getMethod", Method.class, String.class, Class[].class);
                    Expr methodObj = b.invokeVirtual(getMethodDesc, enterpriseBeanInstanceClass,
                            methodName, paramTypesVar);

                    // Create null proceed Method parameter
                    Expr nullMethod = Const.ofNull(Method.class);

                    // Create args array with the three parameters
                    Expr argsArray = b.newEmptyArray(Object.class, 3);
                    var argsVar = b.localVar("args", argsArray);
                    b.set(argsVar.elem(0), markerParam);
                    b.set(argsVar.elem(1), sessionBeanParam);
                    b.set(argsVar.elem(2), contextParam);

                    // Call methodHandler.invoke(this, methodObj, null, args)
                    MethodDesc invokeDesc = MethodDesc.of(
                            MethodHandler.class,
                            INVOKE_METHOD_NAME,
                            Object.class,
                            Object.class, Method.class, Method.class, Object[].class);

                    b.invokeInterface(invokeDesc, handler, m.this_(), methodObj, nullMethod, argsVar);

                    // Return (void method)
                    b.return_();
                });
            });
        } catch (Exception e) {
            throw new WeldException(e);
        }
    }

    @Override
    protected String getProxyNameSuffix() {
        return SUFFIX;
    }

    @Override
    protected boolean isMethodAccepted(Method method, Class<?> proxySuperclass) {
        return super.isMethodAccepted(method, proxySuperclass)
                && CommonProxiedMethodFilters.NON_PRIVATE.accept(method, proxySuperclass);
    }

    @Override
    protected boolean isUsingProxyInstantiator() {
        return false;
    }
}
