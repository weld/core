package org.jboss.webbeans.test;

import javax.webbeans.Current;

import org.jboss.webbeans.injectable.InjectableField;
import org.jboss.webbeans.test.components.Farm;
import org.jboss.webbeans.test.components.Pig;
import org.jboss.webbeans.util.Reflections;
import org.testng.annotations.Test;

public class InjectableTest
{

   @SuppressWarnings("unchecked")
   @Test
   public void testInjectableField() throws Exception
   {
      InjectableField<Pig> pig = new InjectableField<Pig>(Farm.class.getDeclaredField("pig"));
      assert pig.getAnnotatedItem().getType().isAssignableFrom(Pig.class);
      assert pig.getBindingTypes().size() == 1;
      assert Reflections.annotationSetMatches(pig.getBindingTypes(), Current.class);
      assert pig.getType().isAssignableFrom(Pig.class);
   }
   
}
