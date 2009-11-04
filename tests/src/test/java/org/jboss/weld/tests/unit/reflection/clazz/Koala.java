package org.jboss.weld.tests.unit.reflection.clazz;

public class Koala
{
   
   public Animal procreate()
   {
      class BabyKoala implements Animal {}
      return new BabyKoala();
   }

}
