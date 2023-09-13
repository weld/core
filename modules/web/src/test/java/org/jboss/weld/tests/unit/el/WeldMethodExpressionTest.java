/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import jakarta.el.ELContext;
import jakarta.el.MethodExpression;
import jakarta.el.MethodInfo;

import org.jboss.weld.module.web.el.WeldMethodExpression;
import org.junit.Test;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class WeldMethodExpressionTest {

    @Test
    public void testEquals() {
        MethodExpression delegate = new MockMethodExpression("doSomething");

        MethodExpression wrapper1 = new WeldMethodExpression(delegate);
        MethodExpression wrapper2 = new WeldMethodExpression(delegate);

        assertTrue("should be equal", wrapper1.equals(wrapper2));
        assertTrue("should be equal", wrapper2.equals(wrapper1));

        // since there is no way of making delegate.equals(wrapper) return true, we must preserve the symmetry by
        // making wrapper.equals(delegate) also return false.
        assertFalse("should not be equal", delegate.equals(wrapper1));
        assertFalse("should not be equal", wrapper1.equals(delegate));
    }

    private static class MockMethodExpression extends MethodExpression {

        private String expressionString;

        private MockMethodExpression(String expressionString) {
            this.expressionString = expressionString;
        }

        @Override
        public MethodInfo getMethodInfo(ELContext context) {
            return null;
        }

        @Override
        public Object invoke(ELContext context, Object[] params) {
            return null;
        }

        @Override
        public String getExpressionString() {
            return expressionString;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            MockMethodExpression that = (MockMethodExpression) o;

            if (!expressionString.equals(that.expressionString))
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            return expressionString.hashCode();
        }

        @Override
        public boolean isLiteralText() {
            return true;
        }
    }
}
