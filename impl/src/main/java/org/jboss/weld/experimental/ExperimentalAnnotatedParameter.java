/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.experimental;

import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Parameter;

import javax.enterprise.inject.spi.AnnotatedParameter;

/**
 * This API is experimental and will change! All the methods declared by this interface are supposed to be moved to {@link AnnotatedParameter}.
 *
 * All the methods declared by this interface should be moved to AnnotatedParameter.
 *
 * @author Jozef Hartinger
 * @see CDI-481
 *
 * @param <X>
 */
public interface ExperimentalAnnotatedParameter<X> extends AnnotatedParameter<X> {

    /**
     * Get the underlying {@link Parameter}.
     *
     * @return the {@link Parameter}
     */
    default Parameter getJavaParameter() {
        Member member = getDeclaringCallable().getJavaMember();
        if (!(member instanceof Executable)) {
            throw new IllegalStateException("Parameter does not belong to an executable " + member);
        }
        Executable executable = (Executable) member;
        return executable.getParameters()[getPosition()];
    }
}
