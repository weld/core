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
package org.jboss.weld.tck.jbossas;

import java.util.Map.Entry;

import org.jboss.deployers.client.spi.IncompleteDeploymentException;
import org.jboss.testharness.api.DeploymentException;
import org.jboss.testharness.api.DeploymentExceptionTransformer;

/**
 * An implementation which can transform deployment exceptions from JBoss AS
 * reported via the Profile Service.
 * 
 * Please note that if the deployment fails for a secondary reason, such as a NullPointerException in the init() method
 * of a filter, to provide an example, then this translator will not be made aware of the bootstrap exception.
 * 
 * @author Pete Muir
 */
public class WeldProfileServiceDeploymentExceptionTransformer implements DeploymentExceptionTransformer
{

   public DeploymentException transform(DeploymentException exception)
   {
      Throwable failure = exception.getCause().getCause();
      if (failure instanceof IncompleteDeploymentException)
      {
         IncompleteDeploymentException incompleteDeploymentException = (IncompleteDeploymentException) failure;
         for (Entry<String, Throwable> entry : incompleteDeploymentException.getIncompleteDeployments().getContextsInError().entrySet())
         {
            if (entry.getKey().endsWith(exception.getName() + "/_WeldBootstrapBean"))
            {
               return new DeploymentException(exception, entry.getValue());
            }
         }
      }
      return exception;
   }

}
