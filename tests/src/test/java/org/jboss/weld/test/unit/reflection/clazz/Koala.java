package org.jboss.weld.test.unit.reflection.clazz;

public class Koala
{
   
   public Animal procreate()
   {
      class BabyKoala implements Animal {}
      return new BabyKoala();
   }

}
