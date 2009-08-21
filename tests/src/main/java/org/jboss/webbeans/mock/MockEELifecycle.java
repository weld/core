/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.webbeans.mock;

import org.jboss.webbeans.bootstrap.api.Environments;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.persistence.spi.JpaServices;
import org.jboss.webbeans.persistence.spi.helpers.JSFServices;
import org.jboss.webbeans.resources.spi.ResourceServices;
import org.jboss.webbeans.security.spi.SecurityServices;
import org.jboss.webbeans.transaction.spi.TransactionServices;
import org.jboss.webbeans.validation.spi.ValidationServices;

public class MockEELifecycle extends MockServletLifecycle
{
   
   private static final TransactionServices MOCK_TRANSACTION_SERVICES = new MockTransactionServices();

   public MockEELifecycle()
   {
      super();
      getBootstrap().getServices().add(TransactionServices.class, MOCK_TRANSACTION_SERVICES);
      getBootstrap().getServices().add(EjbServices.class, new MockEjBServices());
      getBootstrap().getServices().add(JpaServices.class, new MockJpaServices(getDeployment()));
      getBootstrap().getServices().add(ResourceServices.class, new MockResourceServices());
      getBootstrap().getServices().add(SecurityServices.class, new MockSecurityServices());
      getBootstrap().getServices().add(ValidationServices.class, new MockValidationServices());
      getBootstrap().getServices().add(JSFServices.class, new MockJSFServices());
      getBootstrap().setEnvironment(Environments.EE);
   }
   
  
   
}
