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

package org.jboss.webbeans.bean;

import javax.webbeans.ApplicationScoped;
import javax.webbeans.Decorator;
import javax.webbeans.DefinitionException;
import javax.webbeans.Dependent;
import javax.webbeans.Destructor;
import javax.webbeans.Disposes;
import javax.webbeans.Initializer;
import javax.webbeans.Interceptor;
import javax.webbeans.Observes;
import javax.webbeans.Produces;
import javax.webbeans.Specializes;
import javax.webbeans.manager.EnterpriseBeanLookup;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.ejb.EJB;
import org.jboss.webbeans.ejb.EjbMetaData;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;

/**
 * An enterprise bean representation
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public class EnterpriseBean<T> extends AbstractClassBean<T>
{

   private String location;

   private EjbMetaData<T> ejbMetaData;

   /**
    * Constructor
    * 
    * @param type The type of the bean
    * @param manager The Web Beans manager
    */
   public EnterpriseBean(Class<T> type, ManagerImpl manager)
   {
      super(type, manager);
      init();
   }

   /**
    * Initializes the bean and its metadata
    */
   @Override
   protected void init()
   {
      super.init();
      ejbMetaData = ManagerImpl.instance().getMetaDataCache().getEjbMetaData(getType());
      initRemoveMethod();
      initInjectionPoints();
      checkEnterpriseBeanTypeAllowed();
      checkEnterpriseScopeAllowed();
      checkConflictingRoles();
      checkSpecialization();
      checkRemoveMethod();
   }

   /**
    * Initializes the injection points
    */
   @Override
   protected void initInjectionPoints()
   {
      super.initInjectionPoints();
      if (removeMethod != null)
      {
         for (AnnotatedParameter<?> injectable : removeMethod.getParameters())
         {
            injectionPoints.add(injectable);
         }
      }
   }

   /**
    * Validates for non-conflicting roles
    */
   protected void checkConflictingRoles()
   {
      if (getType().isAnnotationPresent(Interceptor.class))
      {
         throw new DefinitionException("Enterprise beans cannot be interceptors");
      }
      if (getType().isAnnotationPresent(Decorator.class))
      {
         throw new DefinitionException("Enterprise beans cannot be decorators");
      }
   }

   /**
    * Check that the scope type is allowed by the stereotypes on the bean and
    * the bean type
    */
   protected void checkEnterpriseScopeAllowed()
   {
      if (getEjbMetaData().isStateless() && !getScopeType().equals(Dependent.class))
      {
         throw new DefinitionException("Scope " + getScopeType() + " is not allowed on stateless enterpise beans for " + getType() + ". Only @Dependent is allowed on stateless enterprise beans");
      }
      if (getEjbMetaData().isSingleton() && (!(getScopeType().equals(Dependent.class) || getScopeType().equals(ApplicationScoped.class))))
      {
         throw new DefinitionException("Scope " + getScopeType() + " is not allowed on singleton enterpise beans for " + getType() + ". Only @Dependent or @ApplicationScoped is allowed on singleton enterprise beans");
      }
   }

   /**
    * Validates specialization
    */
   private void checkSpecialization()
   {
      if (!getType().isAnnotationPresent(Specializes.class))
      {
         return;
      }
      if (!isDefinedInXml())
      {
         if (!getManager().getMetaDataCache().getEjbMetaData(getAnnotatedItem().getSuperclass().getType()).isEjb())
         {
            throw new DefinitionException("Annotation defined specializing EJB must have EJB superclass");
         }
      }
      else
      {
         if (getManager().getMetaDataCache().getEjbMetaData(getAnnotatedItem().getSuperclass().getType()).isEjb())
         {
            throw new DefinitionException("XML defined specializing EJB must have annotation defined EJB implementation");
         }
      }
   }

   /**
    * Initializes the remvoe method
    */
   protected void initRemoveMethod()
   {
      if (!getEjbMetaData().isStateful())
      {
         // Nothing to do for stateless enterprise beans;
         return;
      }

      // >1 @Destructor
      if (getEjbMetaData().getDestructorMethods().size() > 1)
      {
         throw new DefinitionException("Multiple @Destructor methods not allowed on " + getAnnotatedItem());
      }

      // <1 (0) @Destructors
      if (getEjbMetaData().getNoArgsRemoveMethods().size() == 1)
      {
         super.removeMethod = getEjbMetaData().getNoArgsRemoveMethods().get(0);
         return;
      }

      if (!getScopeType().equals(Dependent.class))
      {
         throw new DefinitionException("Only @Dependent scoped enterprise beans can be without remove methods");
      }

   }

   /**
    * Validates the remove method
    */
   private void checkRemoveMethod()
   {
      if (removeMethod == null)
      {
         return;
      }

      if (removeMethod.isAnnotationPresent(Destructor.class) && !removeMethod.isAnnotationPresent(EJB.REMOVE_ANNOTATION))
      {
         throw new DefinitionException("Methods marked @Destructor must also be marked @Remove on " + removeMethod.getName());
      }
      else if (removeMethod.isAnnotationPresent(Initializer.class))
      {
         throw new DefinitionException("Remove methods cannot be initializers on " + removeMethod.getName());
      }
      else if (removeMethod.isAnnotationPresent(Produces.class))
      {
         throw new DefinitionException("Remove methods cannot be producers on " + removeMethod.getName());
      }
      else if (removeMethod.getAnnotatedParameters(Disposes.class).size() > 0)
      {
         throw new DefinitionException("Remove method can't have @Disposes annotated parameters on " + removeMethod.getName());
      }
      else if (removeMethod.getAnnotatedParameters(Observes.class).size() > 0)
      {
         throw new DefinitionException("Remove method can't have @Observes annotated parameters on " + removeMethod.getName());
      }
   }

   /**
    * Creates an instance of the bean
    * 
    * @return The instance
    */
   @SuppressWarnings("unchecked")
   @Override
   public T create()
   {
      T instance = (T) getManager().getInstanceByType(EnterpriseBeanLookup.class).lookup(ejbMetaData.getEjbName());
      bindDecorators();
      bindInterceptors();
      injectEjbAndCommonFields();
      injectBoundFields(instance);
      callInitializers(instance);
      return instance;
   }

   /**
    * Destroys an instance of a bean
    * 
    * @param instance The instance
    */
   @Override
   public void destroy(T instance)
   {
      super.destroy(instance);
   }

   /**
    * Calls all initializers of the bean
    * 
    * @param instance The bean instance
    */
   protected void callInitializers(T instance)
   {
      for (AnnotatedMethod<Object> initializer : getInitializerMethods())
      {
         initializer.invoke(getManager(), instance);
      }
   }

   /**
    * Injects EJBs and common fields
    */
   protected void injectEjbAndCommonFields()
   {
      // TODO
   }

   /**
    * Injects bound fields
    * 
    * @param instance The bean instance
    */
   protected void injectBoundFields(T instance)
   {
      for (AnnotatedField<?> field : getInjectableFields())
      {
         field.inject(instance, getManager());
      }
   }

   /**
    * Gets the debugging location info
    * 
    * @return The location string
    */
   public String getLocation()
   {
      if (location == null)
      {
         location = "type: Enterprise Bean; declaring class: " + getType() + ";";
      }
      return location;
   }

   /**
    * Returns the specializes type of the bean
    * 
    * @return The specialized type
    */
   @SuppressWarnings("unchecked")
   @Override
   protected AbstractBean<? extends T, Class<T>> getSpecializedType()
   {
      // TODO: lots of validation!
      Class<?> superclass = getAnnotatedItem().getType().getSuperclass();
      if (superclass != null)
      {
         // TODO look up this bean and do this via init
         return new EnterpriseBean(superclass, getManager());
      }
      else
      {
         throw new RuntimeException();
      }

   }

   /**
    * Validates the bean type
    */
   private void checkEnterpriseBeanTypeAllowed()
   {
      if (getEjbMetaData().isMessageDriven())
      {
         throw new DefinitionException("Message Driven Beans can't be Web Beans");
      }
   }

   /**
    * Returns the EJB metadata
    * 
    * @return The metadata
    */
   protected EjbMetaData<T> getEjbMetaData()
   {
      return ejbMetaData;
   }

   @Override
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      String ejbType = "";
      if (getEjbMetaData().isMessageDriven())
      {
         ejbType = "message driven";
      }
      else if (getEjbMetaData().isSingleton())
      {
         ejbType = "singleton";
      }
      else if (getEjbMetaData().isStateful())
      {
         ejbType = "stateful";
      }
      else if (getEjbMetaData().isStateless())
      {
         ejbType = "stateless";
      }
      else
      {
         ejbType = "unknown";
      }
      buffer.append("Annotated " + getScopeType().getSimpleName().toLowerCase() + " " + ejbType + " enterprise bean '" + getName() + "' " + "[" + getType().getName() + "]\n");
      buffer.append("   EJB name: " + getEjbMetaData().getEjbName() + ", default JNDI name: " + getEjbMetaData().getDefaultJndiName() + ", EJB link JNDI name: " + getEjbMetaData().getEjbLinkJndiName() + "\n");
      buffer.append("   API types " + getTypes() + ", binding types " + getBindingTypes() + "\n");
      return buffer.toString();
   }

   public String toDetailedString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("EnterpriseBean:\n");
      buffer.append(super.toString() + "\n");
      buffer.append(ejbMetaData.toString() + "\n");
      return buffer.toString();
   }

}
