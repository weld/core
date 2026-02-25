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
package org.jboss.weld.util.bytecode;

import java.util.List;

import org.jboss.weld.bean.proxy.ProxyFactory;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;

/**
 * Utility class for working with constructors in Gizmo bytecode generation.
 *
 * @author Stuart Douglas
 */
public class ConstructorUtils {

    private ConstructorUtils() {
    }

    /**
     * Adds a default constructor that calls super().
     *
     * @param classCreator the class creator
     * @param superClass the superclass
     * @param initialValueBytecode deferred bytecode for field initialization
     * @param useUnsafeInstantiators whether unsafe instantiators are used
     */
    public static void addDefaultConstructor(ClassCreator classCreator, Class<?> superClass,
            List<DeferredBytecode> initialValueBytecode,
            final boolean useUnsafeInstantiators) {
        addConstructor(classCreator, superClass, new Class<?>[0], new Class<?>[0], initialValueBytecode,
                useUnsafeInstantiators);
    }

    /**
     * Adds a constructor that delegates to a super constructor with the same
     * parameter types. The bytecode in initialValueBytecode will be executed at the
     * start of the constructor and can be used to initialize fields to a default
     * value.
     *
     * @param classCreator the class creator
     * @param superClass the superclass
     * @param parameterTypes the constructor parameter types
     * @param exceptionTypes any exceptions that are thrown
     * @param initialValueBytecode deferred bytecode for field initialization
     * @param useUnsafeInstantiators whether unsafe instantiators are used
     */
    public static void addConstructor(ClassCreator classCreator, Class<?> superClass, Class<?>[] parameterTypes,
            Class<?>[] exceptionTypes,
            List<DeferredBytecode> initialValueBytecode, final boolean useUnsafeInstantiators) {

        classCreator.constructor(ctor -> {
            ctor.public_();

            // Add parameters
            ParamVar[] params = new ParamVar[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                params[i] = ctor.parameter("param" + i, parameterTypes[i]);
            }

            // Add checked exceptions
            for (Class<?> exceptionType : exceptionTypes) {
                @SuppressWarnings("unchecked")
                Class<? extends Throwable> throwableClass = (Class<? extends Throwable>) exceptionType;
                ctor.throws_(throwableClass);
            }

            ctor.body(b -> {
                // Apply deferred bytecode (field initialization)
                for (final DeferredBytecode iv : initialValueBytecode) {
                    iv.apply(b);
                }

                // Call super constructor
                ConstructorDesc superConstructor = ConstructorDesc.of(superClass, parameterTypes);
                Expr[] paramExprs = new Expr[params.length];
                for (int i = 0; i < params.length; i++) {
                    paramExprs[i] = params[i];
                }

                if (paramExprs.length == 0) {
                    b.invokeSpecial(superConstructor, ctor.this_());
                } else if (paramExprs.length == 1) {
                    b.invokeSpecial(superConstructor, ctor.this_(), paramExprs[0]);
                } else if (paramExprs.length == 2) {
                    b.invokeSpecial(superConstructor, ctor.this_(), paramExprs[0], paramExprs[1]);
                } else {
                    // For 3+ parameters, use the varargs version
                    b.invokeSpecial(superConstructor, ctor.this_(), paramExprs);
                }

                // If not using unsafe instantiators, set the constructed flag to true
                if (!useUnsafeInstantiators) {
                    FieldDesc constructedField = FieldDesc.of(
                            classCreator.type(),
                            ProxyFactory.CONSTRUCTED_FLAG_NAME,
                            boolean.class);
                    b.set(ctor.this_().field(constructedField), Const.of(true));
                }

                b.return_();
            });
        });
    }
}
