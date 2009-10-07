package org.jboss.webbeans.test.unit.lookup.wbri279;

@IntFactory
public class IntegerFactory extends AbstractFactory<Integer>
{

   public static final Integer VALUE = 123;
   
   public Integer createObject()
   {
      return VALUE;
   }

}
