package org.jboss.webbeans.test.beans.broken;

import javax.webbeans.Production;

public class OuterBean
{

   @Production
   public class InnerBean
   {
      
      public InnerBean()
      {
         
      }
      
   }
   
   public static class StaticInnerBean
   {
      
   }

}
