package org.jboss.webbeans.test;

import java.util.HashSet;
import java.util.Set;

import javax.webbeans.Current;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.BeanImpl;
import org.jboss.webbeans.injectable.InjectableField;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.SimpleComponentModel;
import org.jboss.webbeans.test.components.FishFarm;
import org.jboss.webbeans.test.components.Tuna;
import org.jboss.webbeans.util.Reflections;
import org.testng.annotations.Test;

public class InjectableTest extends AbstractTest
{

   @SuppressWarnings("unchecked")
   @Test
   public void testInjectableField() throws Exception
   {
      InjectableField<Tuna> tuna = new InjectableField<Tuna>(FishFarm.class.getDeclaredField("tuna"));
      assert tuna.getAnnotatedItem().getType().isAssignableFrom(Tuna.class);
      assert tuna.getBindingTypes().size() == 1;
      assert Reflections.annotationSetMatches(tuna.getBindingTypes(), Current.class);
      assert tuna.getType().isAssignableFrom(Tuna.class);
   }
   
   @Test
   public void testPossibleTargets() throws Exception
   {
      InjectableField<Tuna> tunaField = new InjectableField<Tuna>(FishFarm.class.getDeclaredField("tuna"));
      Bean<Tuna> tunaBean = new BeanImpl<Tuna>(new SimpleComponentModel<Tuna>(new SimpleAnnotatedType<Tuna>(Tuna.class), getEmptyAnnotatedItem(Tuna.class), super.manager), manager);
      Set<Bean<?>> beans = new HashSet<Bean<?>>();
      beans.add(tunaBean);
      Set<Bean<?>> possibleTargets = tunaField.getPossibleBeans(beans);
      assert possibleTargets.size() == 1;
      assert possibleTargets.contains(tunaBean);
   }
   
}
