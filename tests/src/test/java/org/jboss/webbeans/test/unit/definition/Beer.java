package org.jboss.webbeans.test.unit.definition;

import javax.annotation.Named;

public
@Named
class Beer
{
   private String name = "Chimay Grande Reserve (Blue)";
   
   private String style = "Belgium Strong Dark Ale";

   public Beer() {}
   
   public Beer(String name, String style)
   {
      this.name = name;
      this.style = style;
   }
   
   public String getName()
   {
      return name;
   }
   
   public String getStyle()
   {
      return style;
   }
}
