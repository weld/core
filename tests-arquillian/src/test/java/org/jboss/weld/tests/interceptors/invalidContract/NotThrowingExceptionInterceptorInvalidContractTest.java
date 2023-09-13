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
package org.jboss.weld.tests.interceptors.invalidContract;

import jakarta.enterprise.inject.spi.DefinitionException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test to verify that a DefinitionException is thrown when the defined Interceptor
 * does not follow the defined contract.
 * <p/>
 * "Around-invoke methods have the following signature:
 * Object <METHOD>(InvocationContext) throws Exception"
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
@Ignore("WELD-1401")
public class NotThrowingExceptionInterceptorInvalidContractTest {

    @ShouldThrowException(DefinitionException.class)
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap
                .create(BeanArchive.class,
                        Utils.getDeploymentNameAsHash(NotThrowingExceptionInterceptorInvalidContractTest.class))
                .intercept(NotThrowingExceptionInterceptor.class)
                .addClasses(Intercept.class, Service.class, ServiceImpl.class, NotThrowingExceptionInterceptor.class);
    }

    //WELD-580
    @Test
    public void shouldHaveThrownDefinitionException() throws Exception {
        // should throw deployment exception
        // currently does not works, see WELD-1401
    }
}
