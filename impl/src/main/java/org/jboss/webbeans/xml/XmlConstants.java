package org.jboss.webbeans.xml;

enum JavaEePackage
{
   JAVA_LANG("java.lang"),
   JAVA_UTIL("java.util"),
   JAVAX_ANNOTATION("javax.annotation"),
   JAVAX_INJECT("javax.inject"),
   JAVAX_CONTEXT("javax.context"),
   JAVAX_INTERCEPTOR("javax.interceptor"),
   JAVAX_DECORATOR("javax.decorator"),
   JAVAX_EVENT("javax.event"),
   JAVAX_EJB("javax.ejb"),
   JAVAX_PERSISTENCE("javax.persistence"),
   JAVAX_XML_WS("javax.xml.ws"),
   JAVAX_JMS("javax.jms"),
   JAVAX_SQL("javax.sql");
   
   private String packageName;

   JavaEePackage(String name) {
      packageName = name;
   }

   @Override
   public String toString() {
       return packageName;
   }
}

public class XmlConstants
{
   public static final String JAVA_EE_NAMESPACE = "urn:java:ee";
   
   public static final String DEPLOY = "Deploy";
   
   public static final String INTERCEPTORS = "Interceptors";
   
   public static final String DECORATORS = "Decorators";
   
   public static final String BINDING_TYPE = "BindingType";
   
   public static final String INTERCEPTOR_BINDING_TYPE = "InterceptorBindingType";
   
   public static final String STEREOTYPE = "Stereotype";
   
   public static final String RESOURCE = "Resource";
   
   public static final String PERSISTENCE_CONTEXT = "PersistenceContext";
   
   public static final String PERSISTENCE_UNIT = "PersistenceUnit";
   
   public static final String EJB = "EJB";
   
   public static final String EJB_NAME = "ejbName";
   
   public static final String WEB_SERVICE_REF = "WebServiceRef";
   
   public static final String TOPIC = "Topic";
   
   public static final String QUEUE = "Queue";
   
   public static final String URN_PREFIX = "urn:java:";
   
   public static final String NAME = "name";
   
   public static final String MAPPED_NAME = "mappedName";
   
   public static final String STANDARD = "Standard";
}
