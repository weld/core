package org.jboss.weld.tests.decorators.custom;

/**
 * @author Marius Bogoevici
 */
public class CustomFrame
{
   public static boolean drawn;

   Window window;

   public void draw()
   {
      window.draw();
      drawn = true;   
   }
}
