package org.jboss.webbeans.util.xml;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.DefinitionException;

import org.dom4j.Element;

public class ParseXmlHelper
{
   private static List<ClassReceiver> receivers;

   static
   {
      receivers = initializeReceivers();
   }

   public static Set<AnnotatedElement> getBeanClasses(List<Element> beans)
   {
      Set<AnnotatedElement> result = new HashSet<AnnotatedElement>();

      for (Element bean : beans)
         result.add(reciveBeanClass(bean));

      return result;
   }

   private static AnnotatedElement reciveBeanClass(Element element)
   {
      for (ClassReceiver receiver : receivers)
      {
         if (receiver.accept(element))
         {
            return receiver.reciveClass(element);
         }
      }

      throw new DefinitionException("definition of a bean " + element.getName() + " is incorrect");
   }

   private static List<ClassReceiver> initializeReceivers()
   {
      List<ClassReceiver> receivers = new ArrayList<ClassReceiver>();

      ClassReceiver jmsResourceClassReceiver = new ClassReceiver()
      {
         public boolean accept(Element element)
         {
            return isJMSResource(element);
         }

         public AnnotatedElement reciveClass(Element element)
         {
            return reciveJMSResourceClass(element);
         }

      };
      ClassReceiver resourceClassReceiver = new ClassReceiver()
      {
         public boolean accept(Element element)
         {
            return isResource(element);
         }

         public AnnotatedElement reciveClass(Element element)
         {
            return reciveResourceClass(element);
         }

      };
      ClassReceiver sessionBeanClassReceiver = new ClassReceiver()
      {
         public boolean accept(Element element)
         {
            return isSessionBean(element);
         }

         public AnnotatedElement reciveClass(Element element)
         {
            return reciveSessionBeanClass(element);
         }

      };
      ClassReceiver simpleBeanClassReceiver = new ClassReceiver()
      {
         public boolean accept(Element element)
         {
            return isSimpleBean(element);
         }

         public AnnotatedElement reciveClass(Element element)
         {
            return reciveSimpleBeanClass(element);
         }

      };

      receivers.add(jmsResourceClassReceiver);
      receivers.add(resourceClassReceiver);
      receivers.add(sessionBeanClassReceiver);
      receivers.add(simpleBeanClassReceiver);

      return receivers;
   }

   private static boolean isResource(Element element)
   {
      Iterator<?> elIterator = element.elementIterator();
      while (elIterator.hasNext())
      {
         Element child = (Element) elIterator.next();
         if (child.getNamespace().getURI().equalsIgnoreCase(XmlConstants.JAVA_EE_NAMESPACE) && 
               (child.getName().equalsIgnoreCase(XmlConstants.RESOURCE) || 
                     child.getName().equalsIgnoreCase(XmlConstants.PERSISTENCE_CONTEXT) || 
                     child.getName().equalsIgnoreCase(XmlConstants.PERSISTENCE_UNIT) || 
                     child.getName().equalsIgnoreCase(XmlConstants.EJB) || 
                     child.getName().equalsIgnoreCase(XmlConstants.WEB_SERVICE_REF)))
            return true;
      }
      return false;
   }

   private static AnnotatedElement reciveResourceClass(Element element)
   {
      // TODO:
      return null;
   }

   private static boolean isJMSResource(Element element)
   {
      if (element.getNamespace().getURI().equalsIgnoreCase(XmlConstants.JAVA_EE_NAMESPACE) && 
            (element.getName().equalsIgnoreCase(XmlConstants.TOPIC) || 
                  element.getName().equalsIgnoreCase(XmlConstants.QUEUE)))
         return true;
      return false;
   }

   private static AnnotatedElement reciveJMSResourceClass(Element element)
   {
      // TODO:
      return null;
   }

   private static boolean isSimpleBean(Element element)
   {
      Class<?> beanClass = loadClass(element);

      if (!Modifier.isAbstract(beanClass.getModifiers()) && 
            beanClass.getTypeParameters().length == 0)
         return true;

      return false;
   }

   private static AnnotatedElement reciveSimpleBeanClass(Element element)
   {
      Class<?> beanClass = loadClass(element);

      if (beanClass.isMemberClass())
         throw new DefinitionException("class " + beanClass + " is a non-static inner class");

      // if (beanClass.getTypeParameters().length > 0)
      // throw new DefinitionException("class " + beanClass +
      // " is a parameterized type");

      // TODO:
      // boolean isDecorator = false;
      // if (Modifier.isAbstract(beanClass.getModifiers()) && !isDecorator)
      // throw new DefinitionException("class " + beanClass +
      // " is an abstract and not Decorator");

      return beanClass;
   }

   private static boolean isSessionBean(Element element)
   {
      // TODO:
      return false;
   }

   private static AnnotatedElement reciveSessionBeanClass(Element element)
   {
      // TODO:
      return null;
   }

   private static Class<?> loadClass(Element element)
   {
      String beanUri = element.getNamespace().getURI();
      String packageName = beanUri.replaceFirst(XmlConstants.URN_PREFIX, "");
      String classPath = packageName + "." + element.getName();

      try
      {
         return Class.forName(classPath);
      }
      catch (ClassNotFoundException e)
      {
         throw new DefinitionException("class " + classPath + " not found");
      }

   }
}
