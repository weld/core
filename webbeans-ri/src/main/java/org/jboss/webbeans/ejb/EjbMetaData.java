package org.jboss.webbeans.ejb;

import static org.jboss.webbeans.ejb.EJB.MESSAGE_DRIVEN_ANNOTATION;
import static org.jboss.webbeans.ejb.EJB.REMOVE_ANNOTATION;
import static org.jboss.webbeans.ejb.EJB.SINGLETON_ANNOTATION;
import static org.jboss.webbeans.ejb.EJB.STATEFUL_ANNOTATION;
import static org.jboss.webbeans.ejb.EJB.STATELESS_ANNOTATION;
import static org.jboss.webbeans.ejb.EjbMetaData.EjbType.MESSAGE_DRIVEN;
import static org.jboss.webbeans.ejb.EjbMetaData.EjbType.SINGLETON;
import static org.jboss.webbeans.ejb.EjbMetaData.EjbType.STATEFUL;
import static org.jboss.webbeans.ejb.EjbMetaData.EjbType.STATELESS;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.webbeans.DefinitionException;
import javax.webbeans.Destructor;

import org.jboss.webbeans.util.Reflections;

public class EjbMetaData<T>
{

   public enum EjbType
   {
      STATELESS,
      STATEFUL,
      SINGLETON,
      MESSAGE_DRIVEN;
   }
   
   private EjbType ejbType;
   private List<Method> removeMethods = new ArrayList<Method>();
   private List<Method> destructorMethods = new ArrayList<Method>();
   private List<Method> noArgsRemoveMethods = new ArrayList<Method>();
   
   // TODO Populate this from web.xml
   private String ejbLinkJndiName;
   
   // TODO Initialize this based on the EJB 3.1 spec
   private String defaultJndiName;
   
   // TODO Initialize the ejb name
   private String ejbName;
   
   private Class<? extends T> type;
   

   public EjbMetaData(Class<? extends T> type)
   {
      // TODO Merge in ejb-jar.xml
      this.type = type;
      if (type.isAnnotationPresent(STATELESS_ANNOTATION))
      {
         this.ejbType = STATELESS;
         // TODO Has to be done here? If they are not parsed, they can't be detected later on (EnterpriseBean remove method init)
         if (!Reflections.getMethods(type, Destructor.class).isEmpty()) {
            throw new DefinitionException("Stateless enterprise beans cannot have @Destructor methods");
         }
      }
      else if (type.isAnnotationPresent(STATEFUL_ANNOTATION))
      {
         this.ejbType = STATEFUL;
         for (Method removeMethod : Reflections.getMethods(type, REMOVE_ANNOTATION))
         {
            removeMethods.add(removeMethod);
            if (removeMethod.getParameterTypes().length == 0) {
               noArgsRemoveMethods.add(removeMethod);
            }
         }
         for (Method destructorMethod : Reflections.getMethods(type, Destructor.class))
         {
            destructorMethods.add(destructorMethod);
         }
      }
      else if (type.isAnnotationPresent(MESSAGE_DRIVEN_ANNOTATION))
      {
         this.ejbType = MESSAGE_DRIVEN;
      }
      else if (type.isAnnotationPresent(SINGLETON_ANNOTATION))
      {
         this.ejbType = SINGLETON;
      }
   }

   public boolean isStateless()
   {
      return STATELESS.equals(ejbType);
   }

   public boolean isStateful()
   {
      return STATEFUL.equals(ejbType);
   }

   public boolean isMessageDriven()
   {
      return MESSAGE_DRIVEN.equals(ejbType);
   }

   public boolean isSingleton()
   {
      return SINGLETON.equals(ejbType);
   }
   
   public List<Method> getRemoveMethods()
   {
      return removeMethods;
   }
   
   public String getEjbLinkJndiName()
   {
      return ejbLinkJndiName;
   }

   public String getDefaultJndiName()
   {
      return defaultJndiName;
   }
   
   public String getEjbName()
   {
      return ejbName;
   }
   
   public Class<? extends T> getType()
   {
      return type;
   }

   public List<Method> getDestructorMethods()
   {
      return destructorMethods;
   }

   public Method getNoArgsRemoveMethod()
   {
      return noArgsRemoveMethods.size() == 1 ? noArgsRemoveMethods.iterator().next() : null;
   }

}
