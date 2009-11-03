package org.jboss.weld.test.unit.reflection.clazz;


public class Kangaroo 
{
   LittleKangarooInHerPouch<String> procreate()
   {
      return new LittleKangarooInHerPouch<String>("Joey");
   }

   public class LittleKangarooInHerPouch<T>
   {
      LittleKangarooInHerPouch(T formalParam)
      {
      }
   }
}
