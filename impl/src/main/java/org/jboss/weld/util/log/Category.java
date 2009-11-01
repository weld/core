package org.jboss.weld.util.log;

public enum Category
{

   BOOTSTRAP("Bootstrap"),
   UTIL("Utilities"),
   BEAN("Bean"), 
   SERVLET("Servlet"),
   REFLECTION("Reflection"),
   JSF("JSF"),
   EVENT("Event"),
   CONVERSATION("Conversation"),
   CONTEXT("Context");
   
   private static final String LOG_PREFIX = "Weld/";
   
   private final String name;
   
   Category(String name)
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
