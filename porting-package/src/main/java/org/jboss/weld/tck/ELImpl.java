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
package org.jboss.weld.tck;

import org.jboss.jsr299.tck.api.JSR299Configuration;
import org.jboss.testharness.api.Configurable;
import org.jboss.testharness.api.Configuration;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.el.EL;

import javax.el.ELContext;

public class ELImpl implements org.jboss.jsr299.tck.spi.EL, Configurable {

    private JSR299Configuration configuration;


    @SuppressWarnings("unchecked")
    public <T> T evaluateValueExpression(String expression, Class<T> expectedType) {
        ELContext elContext = createELContext();
        return (T) EL.EXPRESSION_FACTORY.createValueExpression(elContext, expression, expectedType).getValue(elContext);
    }

    @SuppressWarnings("unchecked")
    public <T> T evaluateMethodExpression(String expression, Class<T> expectedType, Class<?>[] expectedParamTypes, Object[] expectedParams) {
        ELContext elContext = createELContext();
        return (T) EL.EXPRESSION_FACTORY.createMethodExpression(elContext, expression, expectedType, expectedParamTypes).invoke(elContext, expectedParams);
    }

    public ELContext createELContext() {
        if (configuration.getManagers().getManager() instanceof BeanManagerImpl) {
            return EL.createELContext((BeanManagerImpl) configuration.getManagers().getManager());
        } else {
            throw new IllegalStateException("Wrong manager");
        }
    }

    public void setConfiguration(Configuration configuration) {
        if (configuration instanceof JSR299Configuration) {
            this.configuration = (JSR299Configuration) configuration;
        } else {
            throw new IllegalArgumentException("Can only use ELImpl in the CDI TCK");
        }
    }

}
