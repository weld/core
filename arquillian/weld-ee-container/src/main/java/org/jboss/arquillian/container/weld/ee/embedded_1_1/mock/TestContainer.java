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
package org.jboss.arquillian.container.weld.ee.embedded_1_1.mock;

import static java.util.Arrays.asList;

import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.manager.api.WeldManager;

/**
 * <p>
 * Wrapper over Weld, exposing operations commonly required for executing tests
 * with Weld. {@link TestContainer} exposes a "Mock Java EE 6" view of Weld,
 * allowing Weld to discover EE components, but not to invoke them.
 * </p>
 * 
 * <p>
 * In general, we recommend using Arquillian to test CDI/Weld applications,
 * however sometimes it useful to have greater control over the test, and in
 * that case you may wish to use {@link TestContainer}.
 * </p>
 * 
 * <p>
 * If you require more control over the container bootstrap lifecycle than that
 * offered by {@link TestContainer} you should use the {@link #getLifecycle()}
 * method. For example:
 * <p>
 * 
 * <pre>
 * TestContainer container = new TestContainer(...);
 * container.getLifecycle().initialize();
 * container.getLifecycle().getBootstrap().startInitialization(container.getDeployment());
 * container.getLifecycle().getBootstrap().deployBeans();
 * container.getLifecycle().getBootstrap().validateBeans();
 * container.getLifecycle().getBootstrap().endInitialization();
 * container.getLifecycle().stopContainer();
 * </pre>
 * 
 * <p>
 * Note that we can easily mix fine-grained calls to bootstrap, and coarse
 * grained calls to {@link TestContainer}.
 * </p>
 * 
 * @author Pete Muir
 * 
 */
public class TestContainer
{

   /**
    * A further wrapper over TestContainer, allowing a test to be run.
    * 
    * @author Pete Muir
    * 
    */
   public static class Runner
   {
      
      public static interface Runnable
      {
         
         public abstract void run(WeldManager beanManager);
         
      }

      private static Runnable NO_OP = new Runnable()
      {

         public void run(WeldManager beanManager)
         {
         }

      };

      private final List<URL> beansXml;

      private final List<Class<?>> classes;

      public Runner(List<URL> beansXml, List<Class<?>> classes)
      {
         this.beansXml = beansXml;
         this.classes = classes;
      }

      /**
       * Bootstrap and shutdown the container.
       * 
       */
      public void run() throws Exception
      {
         run(NO_OP);
      }

      /**
       * Bootstrap and shutdown the container.
       * 
       * @param runnable a {@link Runnable} to be called whilst the container is
       *           active
       * 
       */
      public void run(Runnable runnable) throws Exception
      {
         TestContainer container = null;
         try
         {
            container = new TestContainer(beansXml, classes);
            container.startContainer().ensureRequestActive();
            runnable.run(container.getBeanManager(container.getDeployment().getBeanDeploymentArchives().iterator().next()));
         }
         finally
         {
            if (container != null)
            {
               container.stopContainer();
            }
         }
      }

      /**
       * Bootstrap and shutdown the container If the expected exception must be
       * thrown (including message).
       * 
       * @throws AssertionError if the exception that was expected is not
       *            thrown.
       */
      public void runAndExpect(Exception expected)
      {
         runAndExpect(NO_OP, expected);
      }

      /**
       * Bootstrap and shutdown the container. If the expected exception must be
       * thrown (including message).
       * 
       *@param runnable a {@link Runnable} to be called whilst the container is
       *           active
       * @throws AssertionError if the exception that was expected is not
       *            thrown.
       */
      public void runAndExpect(Runnable runnable, Exception expected)
      {
         try
         {
            run();
         }
         catch (Exception e)
         {
            if (!expected.getClass().isAssignableFrom(e.getClass()))
            {
               Error t = new AssertionError("Expected exception " + expected + " but got " + e);
               t.initCause(e);
               throw t;
               
            }
            if (expected.getMessage() == null)
            {
               return;
            }
            String errorCode = expected.getMessage().substring(0, 11);
            if (e.getMessage().startsWith(errorCode))
            {
               return;
            }
         }
         throw new AssertionError("Expected exception " + expected + " but none was thrown");
      }

   }

   private final MockLifecycle lifecycle;
   private final Deployment deployment;

   /**
    * Create a container, specifying the classes and beans.xml to deploy
    * 
    * @param lifecycle
    * @param classes
    * @param beansXml
    */
   public TestContainer(BeansXml beansXml, Collection<Class<?>> classes)
   {
      this(new FlatDeployment(new BeanDeploymentArchiveImpl(beansXml, classes)));
   }

   public TestContainer(Collection<URL> beansXml, Collection<Class<?>> classes)
   {
      this.lifecycle = new MockLifecycle();
      this.deployment = new FlatDeployment(new BeanDeploymentArchiveImpl(lifecycle.getBootstrap().parse(beansXml), classes));
   }
   
   public TestContainer(String beanArchiveId, Collection<URL> beansXml, Collection<Class<?>> classes)
   {
      this.lifecycle = new MockLifecycle();
      this.deployment = new FlatDeployment(new BeanDeploymentArchiveImpl(beanArchiveId, lifecycle.getBootstrap().parse(beansXml), classes));
   }

   /**
    * Create a container, specifying the classes and beans.xml to deploy
    * 
    * @param lifecycle
    * @param classes
    * @param beansXml
    */
   public TestContainer(BeansXml beansXml, Class<?>... classes)
   {
      this(new FlatDeployment(new BeanDeploymentArchiveImpl(beansXml, asList(classes))));
   }

   public TestContainer(Class<?>... classes)
   {
      this(new FlatDeployment(new BeanDeploymentArchiveImpl(asList(classes))));
   }

   public TestContainer(Deployment deployment)
   {
      this.deployment = deployment;
      this.lifecycle = new MockLifecycle();
   }

   public TestContainer ensureRequestActive()
   {
      if (!getLifecycle().isSessionActive())
      {
         getLifecycle().beginSession();
      }
      if (!getLifecycle().isConversationActive())
      {
         ((ConversationContext) getLifecycle().getConversationContext()).setActive(true);
      }
      if (!getLifecycle().isRequestActive())
      {
         getLifecycle().beginRequest();
      }
      return this;
   }

   /**
    * Starts the container and begins the application
    */
   public TestContainer startContainer()
   {
      getLifecycle().initialize(deployment);
      getLifecycle().beginApplication();
      return this;
   }
   
   /**
    * Get the context lifecycle, allowing fine control over the contexts' state
    * 
    * @return
    */
   public MockLifecycle getLifecycle()
   {
      return lifecycle;
   }

   public WeldManager getBeanManager(BeanDeploymentArchive beanDeploymentArchive)
   {
      return getLifecycle().getBootstrap().getManager(beanDeploymentArchive);
   }
   
   public Deployment getDeployment()
   {
      return deployment;
   }
   
   /**
    * Clean up the container, ending any active contexts
    * 
    */
   public TestContainer stopContainer()
   {
      if (getLifecycle().isRequestActive())
      {
         getLifecycle().endRequest();
      }
      if (getLifecycle().isConversationActive())
      {
         ((ConversationContext) getLifecycle().getConversationContext()).setActive(false);
      }
      if (getLifecycle().isSessionActive())
      {
         getLifecycle().endSession();
      }
      if (getLifecycle().isApplicationActive())
      {
         getLifecycle().endApplication();
      }
      return this;
   }

}