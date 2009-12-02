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
package org.jboss.weld.environment.se;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import org.jboss.weld.environment.se.beans.InstanceManager;
import org.jboss.weld.environment.se.beans.ParametersFactory;

/**
 * Explicitly registers all of the 'built-in' Java SE related beans.
 * @author Peter Royle
 */
public class WeldSEBeanRegistrant implements Extension
{

   public void registerWeldSEBeans(@Observes BeforeBeanDiscovery event, BeanManager beanManager)
   {
      event.addAnnotatedType(beanManager.createAnnotatedType(ShutdownManager.class));
      event.addAnnotatedType(beanManager.createAnnotatedType(ParametersFactory.class));
      event.addAnnotatedType(beanManager.createAnnotatedType(InstanceManager.class));
   }

}
