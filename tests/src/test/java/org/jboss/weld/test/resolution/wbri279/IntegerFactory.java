package org.jboss.weld.test.resolution.wbri279;

@IntFactory
public class IntegerFactory extends AbstractFactory<Integer>
{

   public static final Integer VALUE = 123;
   
   public Integer createObject()
   {
      return VALUE;
   }

}
