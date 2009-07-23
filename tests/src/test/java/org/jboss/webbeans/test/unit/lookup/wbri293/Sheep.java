package org.jboss.webbeans.test.unit.lookup.wbri293;

import javax.enterprise.context.RequestScoped;

@RequestScoped
class Sheep
{
   private int age = 0;

   public int getAge()
   {
      return age;
   }

   public void setAge(int age)
   {
      this.age = age;
   }
}
