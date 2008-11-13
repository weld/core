package org.jboss.webbeans.test;

import java.util.HashSet;
import java.util.Set;

import org.jboss.webbeans.bean.AbstractBean;
import org.jboss.webbeans.test.beans.Elephant;
import org.jboss.webbeans.test.beans.Panther;
import org.jboss.webbeans.test.beans.Salmon;
import org.jboss.webbeans.test.beans.SeaBass;
import org.jboss.webbeans.test.beans.Sole;
import org.jboss.webbeans.test.beans.Tarantula;
import org.jboss.webbeans.test.beans.TarantulaProducer;
import org.jboss.webbeans.test.beans.Tiger;
import org.jboss.webbeans.test.beans.Tuna;
import org.jboss.webbeans.test.ejb.model.valid.Hound;
import org.testng.annotations.Test;

public class BoostrapTest extends AbstractTest
{
   @Test(groups="bootstrap")
   public void testSingleSimpleBean()
   {
      Set<AbstractBean<?, ?>> beans = bootstrap.discoverBeans(Tuna.class);
      assert beans.size() == 1;
      assert beans.iterator().next().getType().equals(Tuna.class);
   }
   
   @Test(groups="bootstrap")
   public void testSingleEnterpriseBean()
   {
      Set<AbstractBean<?, ?>> beans = bootstrap.discoverBeans(Hound.class);
      assert beans.size() == 1;
      assert beans.iterator().next().getType().equals(Hound.class);
   }
   
   @Test(groups="bootstrap")
   public void testMultipleSimpleBean()
   {
      Set<AbstractBean<?, ?>> beans = bootstrap.discoverBeans(Tuna.class, Salmon.class, SeaBass.class, Sole.class);
      assert beans.size() == 4;
      Set<Class<?>> classes = new HashSet<Class<?>>();
      for (AbstractBean<?, ?> bean : beans)
      {
         classes.add(bean.getType());
      }
      assert classes.contains(Tuna.class);
      assert classes.contains(Salmon.class);
      assert classes.contains(SeaBass.class);
      assert classes.contains(Sole.class);
   }
   
   @Test(groups="bootstrap")
   public void testProducerMethodBean()
   {
      Set<AbstractBean<?, ?>> beans = bootstrap.discoverBeans(TarantulaProducer.class);
      assert beans.size() == 2;
      Set<Class<?>> classes = new HashSet<Class<?>>();
      for (AbstractBean<?, ?> bean : beans)
      {
         classes.add(bean.getType());
      }
      assert classes.contains(TarantulaProducer.class);
      assert classes.contains(Tarantula.class);
   }
   
   @Test(groups="bootstrap")
   public void testMultipleEnterpriseBean()
   {
      Set<AbstractBean<?, ?>> beans = bootstrap.discoverBeans(Hound.class, Elephant.class, Panther.class, Tiger.class);
      assert beans.size() == 4;
      Set<Class<?>> classes = new HashSet<Class<?>>();
      for (AbstractBean<?, ?> bean : beans)
      {
         classes.add(bean.getType());
      }
      assert classes.contains(Hound.class);
      assert classes.contains(Elephant.class);
      assert classes.contains(Panther.class);
      assert classes.contains(Tiger.class);
   }
   
   @Test(groups="bootstrap")
   public void testMultipleEnterpriseAndSimpleBean()
   {
      Set<AbstractBean<?, ?>> beans = bootstrap.discoverBeans(Hound.class, Elephant.class, Panther.class, Tiger.class, Tuna.class, Salmon.class, SeaBass.class, Sole.class);
      assert beans.size() == 8;
      Set<Class<?>> classes = new HashSet<Class<?>>();
      for (AbstractBean<?, ?> bean : beans)
      {
         classes.add(bean.getType());
      }
      assert classes.contains(Hound.class);
      assert classes.contains(Elephant.class);
      assert classes.contains(Panther.class);
      assert classes.contains(Tiger.class);
      assert classes.contains(Tuna.class);
      assert classes.contains(Salmon.class);
      assert classes.contains(SeaBass.class);
      assert classes.contains(Sole.class);
   }
}
