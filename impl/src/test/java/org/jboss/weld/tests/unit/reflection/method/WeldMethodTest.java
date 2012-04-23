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
package org.jboss.weld.tests.unit.reflection.method;

import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.SharedObjectCache;
import org.junit.Assert;
import org.junit.Test;

public class WeldMethodTest {

    private final ClassTransformer transformer = new ClassTransformer(new TypeStore(), new SharedObjectCache());
    private final Class<Choice<?, ?>> CHOICE_LITERAL = new TypeLiteral<Choice<?, ?>>() {
        private static final long serialVersionUID = 1672009803068800735L;
    }.getRawType();

    /*
    * description = "WELD-221"
    */
    @Test
    public void testMethodReturnsGenericTypeOfClass() throws Exception {
        EnhancedAnnotatedType<Choice<?, ?>> clazz = transformer.getEnhancedAnnotatedType(CHOICE_LITERAL);
        EnhancedAnnotatedMethod<?, ?> method = clazz.getEnhancedMethod(Choice.class.getMethod("aMethod"));
        Assert.assertEquals(3, method.getTypeClosure().size());
    }

}
