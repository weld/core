/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.interceptors.signature;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.spi.DefinitionException;
import jakarta.interceptor.Interceptor;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class PostConstructInterceptorWithInvalidParameterTypeTest {

    @Deployment
    @ShouldThrowException(DefinitionException.class)
    public static Archive<?> deploy() {
        return ShrinkWrap
                .create(BeanArchive.class,
                        Utils.getDeploymentNameAsHash(PostConstructInterceptorWithInvalidParameterTypeTest.class))
                .intercept(MyInterceptor.class)
                .addClass(PostConstructInterceptorWithInvalidParameterTypeTest.class)
                .addClasses(Lifecycle.class, LifecycleInterceptedBean.class);
    }

    @Test
    public void testDeploymentInterceptorWithInvalidParamType() {
        // should throw exception, either IllegalArg or Definition
    }

    @Lifecycle
    @Interceptor
    public static class MyInterceptor {
        @PostConstruct
        public Object postConstruct(String notInvocationContext) throws Exception {
            return null;
        }
    }
}
