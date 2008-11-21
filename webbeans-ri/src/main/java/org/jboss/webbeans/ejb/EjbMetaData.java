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

import java.util.ArrayList;
import java.util.List;

import javax.webbeans.DefinitionException;
import javax.webbeans.Destructor;

import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;

public class EjbMetaData<T>
{

   public enum EjbType
   {
      STATELESS, STATEFUL, SINGLETON, MESSAGE_DRIVEN;
   }

   private EjbType ejbType;
   private List<AnnotatedMethod<Object>> removeMethods = new ArrayList<AnnotatedMethod<Object>>();
   private List<AnnotatedMethod<Object>> destructorMethods = new ArrayList<AnnotatedMethod<Object>>();
   private List<AnnotatedMethod<Object>> noArgsRemoveMethods = new ArrayList<AnnotatedMethod<Object>>();

   // TODO Populate this from web.xml
   private String ejbLinkJndiName;

   // TODO Initialize this based on the EJB 3.1 spec
   private String defaultJndiName;

   // TODO Initialize the ejb name
   private String ejbName;

   private AnnotatedClass<T> type;

   public EjbMetaData(Class<T> type)
   {
      this(new AnnotatedClassImpl<T>(type));
   }

   public EjbMetaData(AnnotatedClass<T> type)
   {
      // TODO Merge in ejb-jar.xml
      this.type = type;
      if (type.isAnnotationPresent(STATELESS_ANNOTATION))
      {
         this.ejbType = STATELESS;
         // TODO Has to be done here? If they are not parsed, they can't be
         // detected later on (EnterpriseBean remove method init)
         if (type.getAnnotatedMethods(Destructor.class).size() > 0)
         {
            throw new DefinitionException("Stateless enterprise beans cannot have @Destructor methods");
         }
      }
      else if (type.isAnnotationPresent(STATEFUL_ANNOTATION))
      {
         this.ejbType = STATEFUL;
         for (AnnotatedMethod<Object> removeMethod : type.getAnnotatedMethods(REMOVE_ANNOTATION))
         {
            removeMethods.add(removeMethod);
            if (removeMethod.getParameters().size() == 0)
            {
               noArgsRemoveMethods.add(removeMethod);
            }
         }
         for (AnnotatedMethod<Object> destructorMethod : type.getAnnotatedMethods(Destructor.class))
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

   public boolean isEjb()
   {
      return ejbType != null;
   }

   public List<AnnotatedMethod<Object>> getRemoveMethods()
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

   public Class<T> getType()
   {
      return type.getType();
   }

   public List<AnnotatedMethod<Object>> getDestructorMethods()
   {
      return destructorMethods;
   }

   public List<AnnotatedMethod<Object>> getNoArgsRemoveMethods()
   {
      return noArgsRemoveMethods;
   }

   @Override
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("EJB metadata model\n");
      buffer.append("EJB name: " + ejbName + "\n");
      buffer.append("EJB type: " + ejbType + "\n");
      buffer.append("EJB link JNDI name " + ejbLinkJndiName + "\n");
      buffer.append("Default JNDI name: " + defaultJndiName + "\n");
      buffer.append("Type: " + type.toString() + "\n");
      buffer.append("Desctructor methods: " + destructorMethods.size() + "\n");
      int i = 0;
      for (AnnotatedMethod<?> method : destructorMethods)
      {
         buffer.append(++i + " - " + method.toString());
      }
      i = 0;
      buffer.append("Remove methods: " + removeMethods.size() + "\n");
      for (AnnotatedMethod<?> method : removeMethods)
      {
         buffer.append(++i + " - " + method.toString());
      }
      i = 0;
      buffer.append("No-args remove methods: " + noArgsRemoveMethods.size() + "\n");
      for (AnnotatedMethod<?> method : noArgsRemoveMethods)
      {
         buffer.append(++i + " - " + method.toString());
      }
      return buffer.toString();
   }

}
