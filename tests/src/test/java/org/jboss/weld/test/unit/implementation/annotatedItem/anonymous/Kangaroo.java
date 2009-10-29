package org.jboss.weld.test.unit.implementation.annotatedItem.anonymous;


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
