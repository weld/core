package org.jboss.weld.util;

public class NamesStringBuilder
{
   private StringBuilder stringBuilder = new StringBuilder();

   public NamesStringBuilder(String context)
   {
      stringBuilder.append("[");
      stringBuilder.append(context);
      stringBuilder.append("]");
   }

   public NamesStringBuilder()
   {
   }

   public NamesStringBuilder add(String text)
   {
      if (text != null && !"".equals(text))
      {
         stringBuilder.append(" ");
         stringBuilder.append(text);
      }
      return this;
   }

   public String toString()
   {
      return stringBuilder.toString().trim();
   }
}
