/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.weld.environment.servlet.inject;

import java.util.Map;
import java.util.WeakHashMap;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.manager.api.WeldManager;

/**
 * Provides support for Weld injection into servlets, servlet filters etc.
 *
 * @author Pete Muir
 * @author <a href="mailto:matija.mazi@gmail.com">Matija Mazi</a>
 * @author <a href="mailto:dan.j.allen@gmail.com">Dan Allen</a>
 */
public abstract class AbstractInjector
{
   private final WeldManager manager;
   private final Map<Class<?>, InjectionTarget<?>> cache = new WeakHashMap<Class<?>, InjectionTarget<?>>();
   private final Map<Object, CreationalContext<Object>> contexts = new WeakHashMap<Object, CreationalContext<Object>>();

   protected AbstractInjector(WeldManager manager)
   {
      if (manager == null)
         throw new IllegalArgumentException("Null manager");
      this.manager = manager;
   }

   public void inject(Object instance)
   {
      // not data-race safe, however doesn't matter, as the injection target created for class A is interchangable for another injection target created for class A
      // TODO Make this a concurrent cache when we switch to google collections
      Class<?> clazz = instance.getClass();
      if (!cache.containsKey(clazz))
      {
         cache.put(clazz, manager.createInjectionTarget(manager.createAnnotatedType(clazz)));
      }
      CreationalContext<Object> cc = manager.createCreationalContext(null);
      InjectionTarget<Object> it = (InjectionTarget<Object>) cache.get(clazz);
      it.inject(instance, cc);
      // QUESTION is holding an instance as a key a bad idea?
      contexts.put(instance, cc);
   }
   
   public void release(Object instance)
   {
      if (contexts.containsKey(instance))
      {
         contexts.get(instance).release();
         contexts.remove(instance);
      }
   }
   
   public void releaseAll()
   {
      for (CreationalContext<Object> cc : contexts.values())
      {
         cc.release();
      }
      contexts.clear();
   }
}
