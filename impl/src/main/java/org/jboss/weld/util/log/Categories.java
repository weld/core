package org.jboss.weld.util.log;

public enum Categories
{

   BOOTSTRAP("WeldBootstrap"),
   UTIL("WeldUtilities"),
   BEAN("Bean"), 
   SERVLET("Servlet"),
   REFLECTION("Reflection"),
   JSF("JSF"),
   EVENT("Event"),
   CONVERSATION("Conversation"),
   CONTEXT("Context");
   
   private static final String LOG_PREFIX = "org.jboss.weld.";
   
   private final String name;
   
   Categories(String name)
   {
      this.name = createName(name);
   }
   
   String getName()
   {
      return name;
   }
   
   private static String createName(String name)
   {
      return new StringBuilder().append(LOG_PREFIX).append(name).toString();
   }

}
