/*
 * JBoss, Home of Professional Open Source
 * Copyright 2022, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.unit.el;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;

import org.jboss.weld.module.web.el.WeldExpressionFactory;
import org.jboss.weld.module.web.util.el.ForwardingExpressionFactory;
import org.junit.Test;

/**
 * @author bstansberry
 */
public class WeldExpressionFactoryTest {

    @Test
    public void testEquals() {
        ExpressionFactory elOne = new MockExpressionFactory();
        ExpressionFactory elTwo = new MockExpressionFactory();

        // Sanity check
        assertNotEquals("Expression factories should be unequal", elOne, elTwo);

        ExpressionFactory wefOne = new WeldExpressionFactory(elOne);
        ExpressionFactory wefTwo = new WeldExpressionFactory(elTwo);
        ExpressionFactory wefThree = new WeldExpressionFactory(elOne);
        ExpressionFactory forward = new ForwardingExpressionFactory() {

            @Override
            protected ExpressionFactory delegate() {
                return elOne;
            }
        };

        assertEquals(wefOne, wefOne);
        assertEquals(wefOne, elOne);
        assertEquals(wefOne, wefThree);
        assertEquals(wefThree, wefOne);
        assertNotEquals(elOne, wefOne); // Assumption check
        assertEquals(wefTwo, elTwo);
        assertNotEquals(wefOne, elTwo);
        assertNotEquals(wefOne, wefTwo);
        assertNotEquals(wefOne, forward);
        assertNotEquals(forward, wefOne);
        assertEquals(forward, forward);
        assertEquals(forward, elOne);
        assertNotEquals(elOne, forward);
    }

    private static class MockExpressionFactory extends ExpressionFactory {

        @Override
        public ValueExpression createValueExpression(ELContext elContext, String s, Class<?> aClass) {
            return null;
        }

        @Override
        public ValueExpression createValueExpression(Object o, Class<?> aClass) {
            return null;
        }

        @Override
        public MethodExpression createMethodExpression(ELContext elContext, String s, Class<?> aClass, Class<?>[] classes) {
            return null;
        }

        @Override
        public <T> T coerceToType(Object o, Class<T> aClass) {
            return null;
        }
    }
}
