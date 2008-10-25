package org.jboss.webbeans.exceptions;

public class NameResolutionLocation extends Location
{
   
   private String target;
   
   public NameResolutionLocation(String target)
   {
      super("Named Based Resolution", null, null);
      
   }
   
   public String getTarget()
   {
      return target;
   }
   
   public void setTarget(String target)
   {
      this.target = target;
   }
   
   @Override
   protected String getMessage()
   {
      String location = super.getMessage();
      if (getTarget() != null)
      {
         location += "target: " + getTarget() + ";"; 
      }
      return location;
   }
   
}
