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
package org.jboss.weld.bean;

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.CIRCULAR_CALL;

import java.lang.reflect.Member;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Alternative;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.context.WeldCreationalContext;
import org.jboss.weld.introspector.WeldMember;
import org.slf4j.cal10n.LocLogger;

/**
 * @author pmuir
 *
 */
public abstract class AbstractReceiverBean<X, T, S extends Member> extends AbstractBean<T, S>
{

   private static final LocLogger log = loggerFactory().getLogger(BEAN);
   
   private AbstractClassBean<X> declaringBean;
   private boolean policy;

   public AbstractReceiverBean(String idSuffix, AbstractClassBean<X> declaringBean, BeanManagerImpl manager)
   {
      super(idSuffix, manager);
      this.declaringBean = declaringBean;
   }
   
   @Override
   public void initialize(BeanDeployerEnvironment environment)
   {
      super.initialize(environment);
   }

   /**
    * Gets the receiver of the product
    * 
    * @return The receiver
    */
   protected Object getReceiver(CreationalContext<?> creationalContext)
   {
      // This is a bit dangerous, as it means that producer methods can end of
      // executing on partially constructed instances. Also, it's not required
      // by the spec...
      if (getAnnotatedItem().isStatic())
      {
         return null;
      }
      else
      {
         if (creationalContext instanceof WeldCreationalContext<?>)
         {
            WeldCreationalContext<?> creationalContextImpl = (WeldCreationalContext<?>) creationalContext;
            if (creationalContextImpl.containsIncompleteInstance(getDeclaringBean()))
            {
               log.warn(CIRCULAR_CALL, getAnnotatedItem(), getDeclaringBean());
               return creationalContextImpl.getIncompleteInstance(getDeclaringBean());
            }
         }
         return manager.getReference(getDeclaringBean(), Object.class, creationalContext);
      }
   }
   

   /**
    * Returns the declaring bean
    * 
    * @return The bean representation
    */
   public AbstractClassBean<X> getDeclaringBean()
   {
      return declaringBean;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.weld.bean.AbstractBean#isPolicy()
    */
   @Override
   public boolean isAlternative()
   {
      return policy;
   }
   
   @Override
   protected void initAlternative()
   {
      if (getDeclaringBean().isAlternative())
      {
         this.policy = true;
      }
      else if (getAnnotatedItem().isAnnotationPresent(Alternative.class))
      {
         this.policy = true;
      }
      else if (getMergedStereotypes().isAlternative())
      {
         this.policy = true;
      }
   }
   
   @Override
   public abstract WeldMember<T, ?, S> getAnnotatedItem();

}
