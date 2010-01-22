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
package org.jboss.weld.test.harness;

import java.net.URL;
import java.util.Collection;

import org.jboss.testharness.api.DeploymentException;
import org.jboss.testharness.spi.StandaloneContainers;
import org.jboss.weld.mock.MockServletLifecycle;
import org.jboss.weld.mock.TestContainer;

public abstract class AbstractStandaloneContainersImpl implements StandaloneContainers
{

   private DeploymentException deploymentException;
   
   private TestContainer testContainer;

   public boolean deploy(Collection<Class<?>> classes, Collection<URL> beansXml)
   {
      this.testContainer = new TestContainer(newLifecycle(), classes, beansXml);
      
      try
      {
         testContainer.startContainer();
      }
      catch (Exception e) 
      {
         this.deploymentException = new DeploymentException("Error deploying beans", e);
         return false;
      }
      testContainer.getLifecycle().beginSession();
      testContainer.getLifecycle().beginRequest();
      return true;
   }

   protected abstract MockServletLifecycle newLifecycle();

   public void deploy(Collection<Class<?>> classes) throws DeploymentException
   {
      deploy(classes, null);
   }

   public void cleanup()
   {
      // Np-op

   }

   public void setup()
   {
      // No-op
   }

   public DeploymentException getDeploymentException()
   {
      return deploymentException;
   }

   public void undeploy()
   {
      testContainer.stopContainer();
      testContainer = null;
      deploymentException = null;
   }

}