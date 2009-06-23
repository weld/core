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
package org.jboss.webbeans;

import java.util.Map.Entry;

import org.jboss.deployers.client.spi.IncompleteDeploymentException;
import org.jboss.testharness.api.DeploymentException;
import org.jboss.testharness.api.DeploymentExceptionTransformer;

/**
 * An implementation which can transform deployment exceptions from JBoss AS
 * reported via the Profile Service
 * 
 * @author Pete Muir
 *
 */
public class WebBeansProfileServiceDeploymentExceptionTransformer implements DeploymentExceptionTransformer
{

   public DeploymentException transform(DeploymentException exception)
   {
      Throwable failure = exception.getCause();
      if (failure.getCause() instanceof IncompleteDeploymentException)
      {
         IncompleteDeploymentException incompleteDeploymentException = (IncompleteDeploymentException) failure.getCause();
         for (Entry<String, Throwable> entry : incompleteDeploymentException.getIncompleteDeployments().getContextsInError().entrySet())
         {
            if (entry.getKey().endsWith(exception.getName() + "/_WebBeansBootstrapBean"))
            {
               return new DeploymentException(exception, entry.getValue());
            }
         }
      }
      return exception;
   }

}
