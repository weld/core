package org.jboss.weld.test.unit.reflection.clazz;


class Kangaroo 
{
   LittleKangarooInHerPouch<String> procreate()
   {
      return new LittleKangarooInHerPouch<String>("Joey");
   }

   class LittleKangarooInHerPouch<T>
   {
      LittleKangarooInHerPouch(T formalParam)
      {
      }
   }
}
