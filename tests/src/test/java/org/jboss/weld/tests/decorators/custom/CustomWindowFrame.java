package org.jboss.weld.tests.decorators.custom;

/**
 * @author Marius Bogoevici
 */
public class CustomWindowFrame implements Window
{
   public static boolean drawn;

   Window window;

   public void draw()
   {
      window.draw();
      drawn = true;   
   }
}
