package org.jboss.webbeans.xml;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.inject.DefinitionException;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.resources.spi.ResourceLoadingException;

public class ParseXmlHelper
{
   private static List<AnnotatedItemReceiver> receivers;

   static
   {
      receivers = initializeReceivers();
   }

   public static Set<AnnotatedItem<?, ?>> getBeanItems(List<Element> beans)
   {
      Set<AnnotatedItem<?, ?>> result = new HashSet<AnnotatedItem<?, ?>>();

      for (Element bean : beans)
         result.add(receiveBeanItem(bean));

      return result;
   }

   private static AnnotatedItem<?, ?> receiveBeanItem(Element element)
   {
      for (AnnotatedItemReceiver receiver : receivers)
      {
         if (receiver.accept(element))
         {
            return receiver.receiveAnnotatedItem(element);
         }
      }

      throw new DefinitionException("definition of a bean " + element.getName() + " is incorrect");
   }

   private static List<AnnotatedItemReceiver> initializeReceivers()
   {
      List<AnnotatedItemReceiver> receivers = new ArrayList<AnnotatedItemReceiver>();

      AnnotatedItemReceiver jmsResourceReceiver = new AnnotatedItemReceiver()
      {
         public boolean accept(Element element)
         {
            return isJMSResource(element);
         }

         public AnnotatedItem<?, ?> receiveAnnotatedItem(Element element)
         {
            return receiveJMSResourceItem(element);
         }

      };
      AnnotatedItemReceiver resourceReceiver = new AnnotatedItemReceiver()
      {
         public boolean accept(Element element)
         {
            return isResource(element);
         }

         public AnnotatedItem<?, ?> receiveAnnotatedItem(Element element)
         {
            return receiveResourceItem(element);
         }

      };
      AnnotatedItemReceiver sessionBeanReceiver = new AnnotatedItemReceiver()
      {
         public boolean accept(Element element)
         {
            return isSessionBean(element);
         }

         public AnnotatedItem<?, ?> receiveAnnotatedItem(Element element)
         {
            return receiveSessionBeanItem(element);
         }

      };
      AnnotatedItemReceiver simpleBeanReceiver = new AnnotatedItemReceiver()
      {
         public boolean accept(Element element)
         {
            return isSimpleBean(element);
         }

         public AnnotatedItem<?, ?> receiveAnnotatedItem(Element element)
         {
            return receiveSimpleBeanItem(element);
         }

      };
      
      //order of elements is important
      receivers.add(jmsResourceReceiver);
      receivers.add(resourceReceiver);
      receivers.add(sessionBeanReceiver);
      receivers.add(simpleBeanReceiver);

      return receivers;
   }
   
   private static boolean isJMSResource(Element element)
   {
      if (isJavaEeNamespace(element) && 
            (element.getName().equalsIgnoreCase(XmlConstants.TOPIC) || 
                  element.getName().equalsIgnoreCase(XmlConstants.QUEUE)))
         return true;
      return false;
   }

   private static AnnotatedItem<?, ?> receiveJMSResourceItem(Element element)
   {
      final Element jmsElement = element;
      
      if(jmsElement.getName().equalsIgnoreCase(XmlConstants.QUEUE))
      {
         Queue queue = new Queue()
         {
            public String getQueueName() throws JMSException
            {
               return getJmsResourceName(jmsElement);
            }
         };
         
         return AnnotatedClassImpl.of(queue.getClass());
      }
                  
      Topic topic = new Topic()
      {
         public String getTopicName() throws JMSException
         {
            return getJmsResourceName(jmsElement);
         }         
      };
      
      return AnnotatedClassImpl.of(topic.getClass());
   }
   
   private static boolean isResource(Element element)
   {
      Iterator<?> elIterator = element.elementIterator();
      while (elIterator.hasNext())
      {
         Element child = (Element) elIterator.next();
         if (isJavaEeNamespace(child) && 
               (child.getName().equalsIgnoreCase(XmlConstants.RESOURCE) || 
                     child.getName().equalsIgnoreCase(XmlConstants.PERSISTENCE_CONTEXT) || 
                     child.getName().equalsIgnoreCase(XmlConstants.PERSISTENCE_UNIT) || 
                     child.getName().equalsIgnoreCase(XmlConstants.EJB) || 
                     child.getName().equalsIgnoreCase(XmlConstants.WEB_SERVICE_REF)))
            return true;
      }
      return false;
   }

   private static AnnotatedItem<?, ?> receiveResourceItem(Element element)
   {
      // TODO:
      return null;
   }
   
   private static boolean isSessionBean(Element element)
   {
      ManagerImpl manager = CurrentManager.rootManager();
      if (manager.getEjbDescriptorCache().containsKey(element.getName()) ||
            element.attribute(XmlConstants.EJB_NAME) != null)
         return true;
      return false;
   }

   private static AnnotatedItem<?, ?> receiveSessionBeanItem(Element element)
   {
      // TODO:
      return null;
   }
   
   private static boolean isSimpleBean(Element element)
   {
      //TODO
      String urn = element.getNamespace().getURI();
      Class<?> beanClass = null;//loadElementClass(urn, element.getName());

      if (!Modifier.isAbstract(beanClass.getModifiers()) && 
            beanClass.getTypeParameters().length == 0)
         return true;

      return false;
   }

   private static AnnotatedItem<?, ?> receiveSimpleBeanItem(Element element)
   {
      //TODO
      String urn = element.getNamespace().getURI();
      Class<?> beanClass = null;//loadElementClass(urn, element.getName());

      if (!Modifier.isStatic(beanClass.getModifiers()) && 
            beanClass.isMemberClass())
         throw new DefinitionException("class " + beanClass + " is a non-static inner class");

      // if (beanClass.getTypeParameters().length > 0)
      // throw new DefinitionException("class " + beanClass +
      // " is a parameterized type");

      // TODO:
      // boolean isDecorator = false;
      // if (Modifier.isAbstract(beanClass.getModifiers()) && !isDecorator)
      // throw new DefinitionException("class " + beanClass +
      // " is an abstract and not Decorator");

      return AnnotatedClassImpl.of(beanClass);
   }
   
   private static String getJmsResourceName(Element element)
   {
      Iterator<?> elIterator = element.elementIterator();
      while (elIterator.hasNext())
      {
         Element child = (Element) elIterator.next();
         if (isJavaEeNamespace(child) && 
               child.getName().equalsIgnoreCase(XmlConstants.RESOURCE))
         {
            Iterator<?> chIterator = child.elementIterator();
            while(chIterator.hasNext())
            {
               Element chChild = (Element) chIterator.next();
               if (isJavaEeNamespace(chChild) && 
                     (chChild.getName().equalsIgnoreCase(XmlConstants.NAME) || 
                           chChild.getName().equalsIgnoreCase(XmlConstants.MAPPED_NAME)))
               {
                  return chChild.getName();
               }
            }
         }         
      }
      throw new DefinitionException("Incorrect JMSResource declaration for " + element.getName());
   }
   
   public static boolean isJavaEeNamespace(Element element)
   {
      return element.getNamespace().getURI().equalsIgnoreCase(XmlConstants.JAVA_EE_NAMESPACE);
   }
   
   public static <T> Class<? extends T> loadElementClass(Element element, Class<T> expectedType, XmlEnvironment environment, Map<String, Set<String>> packagesMap)
   {
      List<Class<? extends T>> classesList = new ArrayList<Class<? extends T>>();
      String className = element.getName();
      String prefix = element.getNamespacePrefix();
      
      for(Map.Entry<String, Set<String>> packagesEntry : packagesMap.entrySet())
      {
         if(prefix.equalsIgnoreCase(packagesEntry.getKey()))
         {
            Set<String> packages = packagesEntry.getValue();
            for(String packageName : packages)
            {
               String classPath = packageName + "." + element.getName();
               try
               {
                  Class<? extends T> classType = environment.loadClass(classPath, expectedType).getRawType();
                  classesList.add(classType);
               }
               catch(ResourceLoadingException e){}
            }
         }
      }
      
      if(classesList.size() == 0)
         throw new DefinitionException("Could not find '" + className + "'");
      
      if(classesList.size() == 1)
         return classesList.get(0);
      
      throw new DefinitionException("There are multiple packages containing a Java type with the same name '" + className + "'");
   }
   
   public static void checkRootAttributes(Element root, Map<String, Set<String>> packagesMap)
   {
      Iterator<?> rootAttrIterator = root.attributeIterator();
      while(rootAttrIterator.hasNext())
      {
         Set<String> packagesSet = new HashSet<String>();
         Attribute attribute = (Attribute)rootAttrIterator.next();
         String attrPrefix = attribute.getNamespacePrefix();         
         String attrData = attribute.getStringValue();
         
         String urn = "";
         for(String attrVal : attrData.split(" "))
         {
            if(attrVal.startsWith(XmlConstants.URN_PREFIX))
            {
               urn = attrVal;
               URL namespaceFile = loadFile(urn, XmlConstants.NAMESPACE_FILE_NAME);
               if(namespaceFile == null)
                  throw new DefinitionException("Could not find '" + XmlConstants.NAMESPACE_FILE_NAME + "' file according to specified URN '" + urn + "'");
               packagesSet.addAll(parseNamespaceFile(namespaceFile));
            }
            if(attribute.getName().equalsIgnoreCase(XmlConstants.SCHEMA_LOCATION) && 
                  attrVal.startsWith(XmlConstants.HTTP_PREFIX) && urn.trim().length() > 0)
            {
               URL schemaFile = loadFile(urn, XmlConstants.SCHEMA_FILE_NAME);
               if(schemaFile == null)
                  throw new DefinitionException("Could not find '" + XmlConstants.SCHEMA_FILE_NAME + "' file according to specified URN '" + urn + "'");
            }
         }
         
         addElementToPackagesMap(packagesMap, attrPrefix, packagesSet);
      }
   }
   
   public static void checkRootDeclaredNamespaces(Element root, Map<String, Set<String>> packagesMap)
   {
      Iterator<?> namespacesIterator = root.declaredNamespaces().iterator();
      while(namespacesIterator.hasNext())
      {
         Namespace namespace = (Namespace)namespacesIterator.next();
         String prefix = namespace.getPrefix();
         String uri = namespace.getURI();
         if(uri.startsWith(XmlConstants.URN_PREFIX))
         {
            Set<String> packagesSet = new HashSet<String>();
            
            URL namespaceFile = loadFile(uri, XmlConstants.NAMESPACE_FILE_NAME);
            if(namespaceFile != null)
            {
               packagesSet.addAll(parseNamespaceFile(namespaceFile));
            }
            else
            {
               String packageName = uri.replaceFirst(XmlConstants.URN_PREFIX, "");
               packagesSet.add(packageName);
            }            
            
            addElementToPackagesMap(packagesMap, prefix, packagesSet);
         }
      }
   }
   
   private static URL loadFile(String urn, String fileName)
   {
      char separator = '/';
      String packageName = urn.replaceFirst(XmlConstants.URN_PREFIX, "");
      String path = packageName.replace('.', separator);
      String filePath = separator + path + separator + fileName;
      URL namespaceFile = ParseXmlHelper.class.getResource(filePath);      
      return namespaceFile;
   }
   
   private static Set<String> parseNamespaceFile(URL namespaceFile)
   {
      Set<String> packages = new HashSet<String>();
      Scanner fileScanner;
      try
      {
         fileScanner = new Scanner(namespaceFile.openStream());
         while (fileScanner.hasNextLine() )
         {
            String line = fileScanner.nextLine();
            Scanner lineScanner = new Scanner(line);
            lineScanner.useDelimiter(XmlConstants.NAMESPACE_FILE_DELIMETER);
            while(lineScanner.hasNext())
            {
               packages.add(lineScanner.next());
            }
            lineScanner.close();
         }
         fileScanner.close();
         return packages;
      }
      catch (IOException e)
      {
         throw new RuntimeException("Error opening " + namespaceFile.toString());
      }         
   }
   
   private static void addElementToPackagesMap(Map<String, Set<String>> packagesMap, String prefix, Set<String> packagesSet)
   {
      if(packagesMap.containsKey(prefix))
      {
         Set<String> packages = packagesMap.get(prefix);
         packages.addAll(packagesSet);
         packagesMap.put(prefix, packages);
      }
      else
      {
         packagesMap.put(prefix, packagesSet);
      }
   }
}
