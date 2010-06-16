package org.jboss.weld.examples.pastecode.session;

public enum Theme
{
   
   DEFAULT("Default Theme"),
   DJANGO( "Django Theme"),
   ECLIPSE("Eclipse Theme"),
   EMACS("Emacs Theme"),
   MIDNIGHT("Midnight Theme"),
   DARK("Dark Theme");
   
   private final String name;
   
   private Theme(String name)
   {
      this.name = name;
   }
   
   public String getName()
   {
      return name;
   }

}
