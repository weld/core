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

import javax.enterprise.inject.UnproxyableResolutionException;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.jsr299.tck.spi.Managers;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.test.BeanManagerLocator;

public class ManagersImpl implements Managers
{
   
   public BeanManager getManager()
   {
      return BeanManagerLocator.INSTANCE.locate();
   }

   public boolean isDefinitionError(org.jboss.testharness.api.DeploymentException deploymentException)
   {
      return isDefinitionException(deploymentException.getCause());
   }

   private boolean isDefinitionException(Throwable t)
   {
      if (t == null)
      {
         return false;
      }
      else if (DefinitionException.class.isAssignableFrom(t.getClass()))
      {
         return true;
      }
      else
      {
         return isDefinitionException(t.getCause());
      }
   }

   public boolean isDeploymentProblem(org.jboss.testharness.api.DeploymentException deploymentException)
   {
      return isDeploymentException(deploymentException.getCause());
   }

   public boolean isDeploymentException(Throwable t)
   {
      if (t == null)
      {
         return false;
      }
      else if (DeploymentException.class.isAssignableFrom(t.getClass()))
      {
         return true;
      }
      else if (UnproxyableResolutionException.class.isAssignableFrom(t.getClass()))
      {
         return true;
      }
      else
      {
         return isDeploymentException(t.getCause());
      }
   }
}
