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

package org.jboss.weld.bean.proxy;

import java.lang.constant.ClassDesc;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.attributes.WeldInjectionPointAttributes;
import org.jboss.weld.logging.BeanLogger;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

/**
 * This special proxy factory is mostly used for abstract decorators. When a
 * delegate field is injected, the abstract methods directly invoke the
 * corresponding method on the delegate. All other cases forward the calls to
 * the {@link BeanInstance} for further processing.
 *
 * @author David Allen
 * @author Stuart Douglas
 */
public class DecoratorProxyFactory<T> extends ProxyFactory<T> {

    public static final String PROXY_SUFFIX = "DecoratorProxy";
    private final WeldInjectionPointAttributes<?, ?> delegateInjectionPoint;
    private final Field delegateField;

    public DecoratorProxyFactory(String contextId, Class<T> proxyType,
            WeldInjectionPointAttributes<?, ?> delegateInjectionPoint, Bean<?> bean) {
        super(contextId, proxyType, Collections.<Type> emptySet(), bean);
        this.delegateInjectionPoint = delegateInjectionPoint;
        if (delegateInjectionPoint instanceof FieldInjectionPoint<?, ?>) {
            delegateField = (Field) ((FieldInjectionPoint<?, ?>) delegateInjectionPoint).getMember();
        } else {
            delegateField = null;
        }
    }

    @Override
    protected void addAdditionalInterfaces(Set<Class<?>> interfaces) {
        interfaces.add(DecoratorProxy.class);
    }

    @Override
    protected void addStaticInitializer(ClassCreator cc, List<MethodInfo> methodsToProxy,
            Map<MethodInfo, String> methodFieldNames) {
        // If delegate field is private, add a static field to hold the accessible Field object
        if (delegateField != null && Modifier.isPrivate(delegateField.getModifiers())) {
            cc.staticField("weld$$$delegateField$$$accessor", f -> {
                f.setType(Field.class);
                f.private_();
            });
        }

        // Create static initializer
        cc.staticMethod("<clinit>", m -> {
            m.returning(void.class);

            m.body(b -> {
                // First, initialize Method fields (from parent logic)
                for (MethodInfo methodInfo : methodsToProxy) {
                    String fieldName = methodFieldNames.get(methodInfo);
                    Method method = methodInfo.method;

                    Expr classExpr = Const.of(method.getDeclaringClass());
                    Expr methodNameExpr = Const.of(method.getName());

                    Class<?>[] paramTypes = method.getParameterTypes();
                    Expr paramTypesArray;
                    if (paramTypes.length == 0) {
                        paramTypesArray = b.newEmptyArray(Class.class, 0);
                    } else {
                        Expr arrayExpr = b.newEmptyArray(Class.class, paramTypes.length);
                        var paramTypesVar = b.localVar("paramTypes_" + fieldName, arrayExpr);

                        for (int i = 0; i < paramTypes.length; i++) {
                            Expr paramClassExpr = Const.of(paramTypes[i]);
                            b.set(paramTypesVar.elem(i), paramClassExpr);
                        }
                        paramTypesArray = paramTypesVar;
                    }

                    MethodDesc getDeclaredMethodDesc = MethodDesc.of(Class.class, "getDeclaredMethod",
                            Method.class, String.class, Class[].class);
                    Expr methodExpr = b.invokeVirtual(getDeclaredMethodDesc, classExpr, methodNameExpr,
                            paramTypesArray);

                    FieldDesc fieldDesc = FieldDesc.of(cc.type(), fieldName, Method.class);
                    b.setStaticField(fieldDesc, methodExpr);
                }

                // If delegate field is private, initialize the accessor field
                if (delegateField != null && Modifier.isPrivate(delegateField.getModifiers())) {
                    // Get the Field object: Class.getDeclaredField("fieldName")
                    MethodDesc getDeclaredFieldDesc = MethodDesc.of(Class.class, "getDeclaredField",
                            Field.class, String.class);
                    Expr declaringClassConst = Const.of(delegateField.getDeclaringClass());
                    Expr fieldNameConst = Const.of(delegateField.getName());
                    Expr fieldObjExpr = b.invokeVirtual(getDeclaredFieldDesc, declaringClassConst, fieldNameConst);

                    // Store in LocalVar for cross-scope usage
                    var fieldObj = b.localVar("delegateFieldAccessor", fieldObjExpr);

                    // Call setAccessible(true) on the Field
                    MethodDesc setAccessibleDesc = MethodDesc.of(Field.class, "setAccessible", void.class,
                            boolean.class);
                    b.invokeVirtual(setAccessibleDesc, fieldObj, Const.of(true));

                    // Store in static field
                    FieldDesc accessorFieldDesc = FieldDesc.of(cc.type(), "weld$$$delegateField$$$accessor",
                            Field.class);
                    b.setStaticField(accessorFieldDesc, fieldObj);
                }

                b.return_();
            });
        });
    }

    @Override
    protected String getProxyNameSuffix() {
        return PROXY_SUFFIX;
    }

    @Override
    protected boolean isUsingProxyInstantiator() {
        return false;
    }

    @Override
    protected void addProxyMethod(ClassCreator cc, MethodInfo methodInfo, String methodFieldName) {
        // For decorator proxies, non-abstract methods should just call super directly
        // without going through the method handler
        Method method = methodInfo.method;

        // Create method descriptor
        MethodDesc methodDesc = MethodDesc.of(method);

        cc.method(methodDesc, m -> {
            // Set modifiers
            int modifiers = method.getModifiers();
            if (Modifier.isPublic(modifiers)) {
                m.public_();
            } else if (Modifier.isProtected(modifiers)) {
                m.protected_();
            }

            // Set varargs flag
            if (method.isVarArgs()) {
                m.varargs();
            }

            // Add parameters
            ParamVar[] params = new ParamVar[method.getParameterCount()];
            for (int i = 0; i < method.getParameterCount(); i++) {
                params[i] = m.parameter("arg" + i, method.getParameterTypes()[i]);
            }

            // Add exceptions
            for (Class<?> exceptionType : method.getExceptionTypes()) {
                @SuppressWarnings("unchecked")
                Class<? extends Throwable> throwableClass = (Class<? extends Throwable>) exceptionType;
                m.throws_(throwableClass);
            }

            m.body(b -> {
                // For non-abstract methods in decorators, call the superclass implementation
                // We need to check if the method is actually implemented in the decorator class
                // If it's from an interface with no implementation, we can't use invokespecial

                // Check if the method is actually declared in the decorator class (not just inherited from interface)
                Method implementedMethod = null;
                try {
                    // Try to find the method in the decorator class hierarchy (not interfaces)
                    Class<?> currentClass = getBeanType();
                    while (currentClass != null && currentClass != Object.class) {
                        try {
                            implementedMethod = currentClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
                            // Found it in the class hierarchy
                            break;
                        } catch (NoSuchMethodException e) {
                            // Not in this class, try parent
                            currentClass = currentClass.getSuperclass();
                        }
                    }
                } catch (Exception e) {
                    // Ignore
                }

                if (implementedMethod != null && !Modifier.isAbstract(implementedMethod.getModifiers())) {
                    // Method has an implementation in the decorator class hierarchy - call it with invokespecial
                    MethodDesc superMethodDesc = MethodDesc.of(implementedMethod);

                    Expr result;
                    if (params.length == 0) {
                        result = b.invokeSpecial(superMethodDesc, m.this_());
                    } else if (params.length == 1) {
                        result = b.invokeSpecial(superMethodDesc, m.this_(), params[0]);
                    } else if (params.length == 2) {
                        result = b.invokeSpecial(superMethodDesc, m.this_(), params[0], params[1]);
                    } else {
                        result = b.invokeSpecial(superMethodDesc, m.this_(), (Expr[]) params);
                    }

                    // Return the result
                    if (method.getReturnType() == void.class) {
                        b.return_();
                    } else {
                        b.return_(result);
                    }
                } else {
                    // Method is not implemented in decorator - must delegate to the field
                    // This shouldn't happen if routing logic is correct, but handle it gracefully
                    if (delegateField != null) {
                        // Read delegate field and invoke on it
                        // Use reflection if private, direct access otherwise
                        Expr delegateInstance = getDelegateFieldValue(b, m.this_(), cc);

                        // Create method descriptor explicitly using delegate field's type as owner
                        // This ensures we use the correct invoke instruction (interface vs virtual)
                        boolean delegateIsInterface = delegateField.getType().isInterface();

                        MethodDesc delegateMethodDesc = MethodDesc.of(
                                delegateField.getType(),
                                method.getName(),
                                method.getReturnType(),
                                method.getParameterTypes());

                        Expr result;
                        if (delegateIsInterface) {
                            if (params.length == 0) {
                                result = b.invokeInterface(delegateMethodDesc, delegateInstance);
                            } else if (params.length == 1) {
                                result = b.invokeInterface(delegateMethodDesc, delegateInstance, params[0]);
                            } else if (params.length == 2) {
                                result = b.invokeInterface(delegateMethodDesc, delegateInstance, params[0], params[1]);
                            } else {
                                result = b.invokeInterface(delegateMethodDesc, delegateInstance,
                                        (Expr[]) params);
                            }
                        } else {
                            if (params.length == 0) {
                                result = b.invokeVirtual(delegateMethodDesc, delegateInstance);
                            } else if (params.length == 1) {
                                result = b.invokeVirtual(delegateMethodDesc, delegateInstance, params[0]);
                            } else if (params.length == 2) {
                                result = b.invokeVirtual(delegateMethodDesc, delegateInstance, params[0], params[1]);
                            } else {
                                result = b.invokeVirtual(delegateMethodDesc, delegateInstance,
                                        (Expr[]) params);
                            }
                        }

                        if (method.getReturnType() == void.class) {
                            b.return_();
                        } else {
                            b.return_(result);
                        }
                    } else {
                        // No delegate field and no implementation - this is an error
                        // Just return a default value to avoid bytecode errors
                        if (method.getReturnType() == void.class) {
                            b.return_();
                        } else if (method.getReturnType().isPrimitive()) {
                            b.return_(Const.of(0));
                        } else {
                            b.return_(Const.ofNull(method.getReturnType()));
                        }
                    }
                }
            });
        });

        BeanLogger.LOG.addingMethodToProxy(method);
    }

    @Override
    protected void addMethodsFromClass(ClassCreator cc,
            List<MethodInfo> methodsToProxy,
            Map<MethodInfo, String> methodFieldNames) {

        // Collect all methods from the decorator class hierarchy
        Set<Method> allDecoratorMethods = new HashSet<>();
        decoratorMethods(getBeanType(), allDecoratorMethods);

        for (MethodInfo methodInfo : methodsToProxy) {
            Method method = methodInfo.method;
            String methodFieldName = methodFieldNames.get(methodInfo);

            // Check if this method is abstract in the decorator
            boolean isAbstractInDecorator = isAbstractInDecorator(method, allDecoratorMethods);

            if (isAbstractInDecorator && delegateField != null) {
                // For abstract methods, generate code that delegates to the injected field
                addAbstractDelegateMethod(cc, methodInfo, methodFieldName);
            } else {
                // For non-abstract methods, call our overridden version that calls super directly
                addProxyMethod(cc, methodInfo, methodFieldName);
            }
        }
    }

    /**
     * Checks if a method is abstract in the decorator class hierarchy.
     * A method is considered abstract for delegation if:
     * 1. It's declared as abstract in the decorator class, OR
     * 2. It's not implemented anywhere in the decorator hierarchy (inherited from interface)
     */
    private boolean isAbstractInDecorator(Method method, Set<Method> allDecoratorMethods) {
        // Check if the method is explicitly declared in the decorator hierarchy
        for (Method decoratorMethod : allDecoratorMethods) {
            if (isEqual(method, decoratorMethod)) {
                // If found in decorator, return whether it's abstract there
                return Modifier.isAbstract(decoratorMethod.getModifiers());
            }
        }

        // Method not found in decorator hierarchy - it's inherited from an interface
        // Check if decorator class or any superclass provides an implementation
        try {
            Method declaredMethod = getBeanType().getMethod(method.getName(), method.getParameterTypes());
            // If we found it and it's not abstract, it's implemented
            return Modifier.isAbstract(declaredMethod.getModifiers());
        } catch (NoSuchMethodException e) {
            // Method not found - shouldn't happen but treat as needing delegation
            return true;
        }
    }

    /**
     * Generates a method that reads the delegate field and invokes the method on it.
     * This is used for abstract methods in the decorator.
     */
    private void addAbstractDelegateMethod(ClassCreator cc, MethodInfo methodInfo, String methodFieldName) {
        Method method = methodInfo.method;

        // Create method descriptor
        MethodDesc methodDesc = MethodDesc.of(method);

        cc.method(methodDesc, m -> {
            // Set modifiers
            int modifiers = method.getModifiers();
            if (Modifier.isPublic(modifiers)) {
                m.public_();
            } else if (Modifier.isProtected(modifiers)) {
                m.protected_();
            }

            // Set varargs flag
            if (method.isVarArgs()) {
                m.varargs();
            }

            // Add parameters
            ParamVar[] params = new ParamVar[method.getParameterCount()];
            for (int i = 0; i < method.getParameterCount(); i++) {
                params[i] = m.parameter("arg" + i, method.getParameterTypes()[i]);
            }

            // Add exceptions
            for (Class<?> exceptionType : method.getExceptionTypes()) {
                @SuppressWarnings("unchecked")
                Class<? extends Throwable> throwableClass = (Class<? extends Throwable>) exceptionType;
                m.throws_(throwableClass);
            }

            m.body(b -> {
                // Read the delegate field: this.delegateField
                // The delegate field is declared in the decorator superclass
                // Use reflection if private, direct access otherwise
                Expr delegateInstance = getDelegateFieldValue(b, m.this_(), cc);

                // Create method descriptor explicitly using delegate field's type as owner
                // This avoids VerifyError from using a method descriptor bound to the decorator class
                // and ensures we use the correct invoke instruction (interface vs virtual)
                boolean delegateIsInterface = delegateField.getType().isInterface();

                MethodDesc delegateMethodDesc = MethodDesc.of(
                        delegateField.getType(),
                        method.getName(),
                        method.getReturnType(),
                        method.getParameterTypes());

                Expr result;
                if (delegateIsInterface) {
                    // Use invokeInterface for interface methods
                    if (params.length == 0) {
                        result = b.invokeInterface(delegateMethodDesc, delegateInstance);
                    } else if (params.length == 1) {
                        result = b.invokeInterface(delegateMethodDesc, delegateInstance, params[0]);
                    } else if (params.length == 2) {
                        result = b.invokeInterface(delegateMethodDesc, delegateInstance, params[0], params[1]);
                    } else {
                        result = b.invokeInterface(delegateMethodDesc, delegateInstance, (Expr[]) params);
                    }
                } else {
                    // Use invokeVirtual for class methods
                    if (params.length == 0) {
                        result = b.invokeVirtual(delegateMethodDesc, delegateInstance);
                    } else if (params.length == 1) {
                        result = b.invokeVirtual(delegateMethodDesc, delegateInstance, params[0]);
                    } else if (params.length == 2) {
                        result = b.invokeVirtual(delegateMethodDesc, delegateInstance, params[0], params[1]);
                    } else {
                        result = b.invokeVirtual(delegateMethodDesc, delegateInstance, (Expr[]) params);
                    }
                }

                // Return the result
                if (method.getReturnType() == void.class) {
                    b.return_();
                } else {
                    b.return_(result);
                }
            });
        });

        BeanLogger.LOG.addingMethodToProxy(method);
    }

    /**
     * Helper to access delegate field, using reflection if private.
     */
    private Expr getDelegateFieldValue(BlockCreator b, Expr thisExpr, ClassCreator cc) {
        if (Modifier.isPrivate(delegateField.getModifiers())) {
            // Use reflection: accessor.get(this)
            FieldDesc accessorFieldDesc = FieldDesc.of(
                    cc.type(),
                    "weld$$$delegateField$$$accessor",
                    Field.class);
            // Read static field
            Expr accessor = Expr.staticField(accessorFieldDesc);

            MethodDesc getDesc = MethodDesc.of(
                    Field.class, "get", Object.class, Object.class);
            Expr value = b.invokeVirtual(getDesc, accessor, thisExpr);

            // Cast to the delegate field type
            return b.cast(value, delegateField.getType());
        } else {
            // Direct field access for non-private fields
            FieldDesc delegateFieldDesc = FieldDesc.of(
                    ClassDesc.of(delegateField.getDeclaringClass().getName()),
                    delegateField.getName(),
                    delegateField.getType());
            // Get the field value - thisExpr must support .field() method
            return b.get(thisExpr.field(delegateFieldDesc));
        }
    }

    /**
     * Collects all methods from the decorator class hierarchy.
     */
    private void decoratorMethods(Class<?> cls, Set<Method> all) {
        if (cls == null) {
            return;
        }
        all.addAll(Arrays.asList(cls.getDeclaredMethods()));

        decoratorMethods(cls.getSuperclass(), all);

        // by now we should have all declared methods, let's only add the missing ones
        for (Class<?> ifc : cls.getInterfaces()) {
            Method[] methods = ifc.getMethods();
            for (Method m : methods) {
                boolean isEqual = false;
                for (Method a : all) {
                    if (isEqual(m, a)) {
                        isEqual = true;
                        break;
                    }
                }
                if (!isEqual) {
                    all.add(m);
                }
            }
        }
    }

    /**
     * Checks if two methods are equal (same name, params, and compatible return types).
     * m is more generic than a.
     */
    private static boolean isEqual(Method m, Method a) {
        if (m.getName().equals(a.getName()) && m.getParameterCount() == a.getParameterCount()) {
            // Check parameters match exactly (or are compatible)
            for (int i = 0; i < m.getParameterCount(); i++) {
                if (!(m.getParameterTypes()[i].isAssignableFrom(a.getParameterTypes()[i]))) {
                    return false;
                }
            }
            // For return types, allow covariant returns in either direction
            // The decorator method (a) can have a more specific return type than the interface method (m)
            // OR the interface method (m) can have a more specific return type (WeldEvent vs Event)
            return m.getReturnType().isAssignableFrom(a.getReturnType())
                    || a.getReturnType().isAssignableFrom(m.getReturnType());
        }
        return false;
    }

}
