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

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Classes;
import org.jboss.testharness.impl.packaging.ExpectedDeploymentException;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

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
@ExpectedDeploymentException(DefinitionException.class)
@Artifact
@Classes({Intercept.class, Service.class, ServiceImpl.class, VoidInterceptor.class})
public class VoidInterceptorInvalidContractTest extends AbstractWeldTest {
    @Test(groups = "broken", description = "WELD-580")
    public void shouldHaveThrownDefinitionException() throws Exception {
    }
}
