package org.jboss.webbeans.exceptions;

public class Location
{
   
   private String type;
   
   private String bean;
   
   private String element;

   public Location(String type, String bean, String element)
   {
      super();
      this.type = type;
      this.bean = bean;
      this.element = element;
   }

   public String getType()
   {
      return type;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public String getBean()
   {
      return bean;
   }

   public void setBean(String bean)
   {
      this.bean = bean;
   }

   public String getElement()
   {
      return element;
   }

   public void setElement(String element)
   {
      this.element = element;
   }
   
   protected String getMessage()
   {
      String location = "";
      if (getType() != null)
      {
         location += "type: " + getType() + "; ";
      }
      if (getBean() != null)
      {
         location += "bean: " + getBean() + "; ";
      }
      if (getElement() != null)
      {
         location += "element: " + getElement() + "; ";
      }
      return location;
   }
   
   @Override
   public String toString()
   {
      return getMessage();
   }
   
}
