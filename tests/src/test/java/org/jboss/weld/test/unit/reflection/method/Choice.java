package org.jboss.weld.test.unit.reflection.method;

abstract class ChoiceParent<T>
{
}


class Choice<T, E> extends ChoiceParent<T>
{
   public Choice<T, E> aMethod()
   {
      return null;
   }
}
