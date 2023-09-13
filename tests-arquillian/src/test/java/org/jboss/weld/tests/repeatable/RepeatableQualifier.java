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
package org.jboss.weld.tests.repeatable;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;
import jakarta.interceptor.InterceptorBinding;

import org.jboss.weld.tests.repeatable.RepeatableQualifier.Container;

@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@InterceptorBinding
@Repeatable(Container.class)
public @interface RepeatableQualifier {

    String value();

    @Retention(RetentionPolicy.RUNTIME)
    @interface Container {
        RepeatableQualifier[] value();
    }

    @SuppressWarnings("all")
    public static class Literal extends AnnotationLiteral<RepeatableQualifier> implements RepeatableQualifier {

        private final String value;

        public Literal(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }
}
