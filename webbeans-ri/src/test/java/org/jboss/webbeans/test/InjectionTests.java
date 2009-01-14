package org.jboss.webbeans.test;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.DefinitionException;
import javax.webbeans.NonexistentFieldException;
import javax.webbeans.NullableDependencyException;
import javax.webbeans.RequestScoped;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.bean.AbstractProducerBean;
import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.context.RequestContext;
import org.jboss.webbeans.test.beans.Fox;
import org.jboss.webbeans.test.beans.FoxRun;
import org.jboss.webbeans.test.beans.SpiderNest;
import org.jboss.webbeans.test.beans.SpiderProducer;
import org.jboss.webbeans.test.beans.Tuna;
import org.jboss.webbeans.test.beans.TunaFarm;
import org.jboss.webbeans.test.beans.broken.BeanWithFinalBoundField;
import org.jboss.webbeans.test.beans.broken.BeanWithStaticBoundField;
import org.jboss.webbeans.test.beans.broken.FarmHouse;
import org.jboss.webbeans.test.beans.broken.FarmHouseProducer;
import org.jboss.webbeans.util.BeanValidation;
import org.testng.annotations.Test;

@SpecVersion("20081206")
public class InjectionTests extends AbstractTest
{

   @Test(groups = { "injection", "producerMethod" })
   @SpecAssertion(section = "5.2")
   public void testInjectionPerformsBoxingIfNecessary() throws Exception
   {
      SimpleBean<SpiderProducer> spiderProducer = SimpleBean.of(SpiderProducer.class, manager);
      manager.addBean(spiderProducer);
      Bean<SpiderNest> spiderNestBean = SimpleBean.of(SpiderNest.class, manager);
      manager.addBean(ProducerMethodBean.of(SpiderProducer.class.getMethod("getWolfSpiderSize"), spiderProducer, manager));
      SpiderNest spiderNest = spiderNestBean.create();
      assert spiderNest.numberOfSpiders != null;
      assert spiderNest.numberOfSpiders.equals(4);
   }

   @Test(groups = { "injection", "producerMethod" }, expectedExceptions = NullableDependencyException.class)
   @SpecAssertion(section = "5.2")
   public void testPrimitiveInjectionPointResolvesToNullableWebBean() throws Exception
   {
      registerProducerBean(FarmHouseProducer.class, "getNumberOfBedrooms", Integer.class);
      manager.addBean(SimpleBean.of(FarmHouse.class, manager));
      BeanValidation.validate(manager.getBeans());
   }

   @Test(groups = { "injection", "clientProxy" }, expectedExceptions = ContextNotActiveException.class)
   @SpecAssertion(section = "5.3")
   public void testInvokeNormalInjectedWebBeanWhenContextNotActive()
   {
      SimpleBean<TunaFarm> tunaFarmBean = SimpleBean.of(TunaFarm.class, manager);
      Bean<Tuna> tunaBean = SimpleBean.of(Tuna.class, manager);
      manager.addBean(tunaBean);
      TunaFarm tunaFarm = tunaFarmBean.create();
      assert tunaFarm.tuna != null;
      RequestContext requestContext = (RequestContext) manager.getContext(RequestScoped.class);
      requestContext.setActive(false);
      tunaFarm.tuna.getName();
   }

   @Test(groups = "injection")
   @SpecAssertion(section = "5.3")
   public void testInvokeDependentScopeWhenContextNotActive()
   {
      Bean<FoxRun> foxRunBean = SimpleBean.of(FoxRun.class, manager);
      Bean<Fox> foxBean = SimpleBean.of(Fox.class, manager);
      manager.addBean(foxBean);
      FoxRun foxRun = foxRunBean.create();
      assert foxRun.fox.getName().equals("gavin");
   }

   @Test(groups = "injection", expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.7")
   public void testInjectingStaticField()
   {
      SimpleBean<BeanWithStaticBoundField> bean = SimpleBean.of(BeanWithStaticBoundField.class, manager);
      Bean<Tuna> tunaBean = SimpleBean.of(Tuna.class, manager);
      manager.addBean(tunaBean);
      BeanWithStaticBoundField instance = bean.create();
   }

   @Test(groups = "injection", expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.7")
   public void testInjectingFinalField()
   {
      SimpleBean<BeanWithFinalBoundField> bean = SimpleBean.of(BeanWithFinalBoundField.class, manager);
      Bean<Tuna> tunaBean = SimpleBean.of(Tuna.class, manager);
      manager.addBean(tunaBean);
      BeanWithFinalBoundField instance = bean.create();
   }

   @Test(groups = { "stub", "injection", "webbeansxml" })
   @SpecAssertion(section = "3.7.2")
   public void testInjectFieldsDeclaredInXml()
   {
      assert false;
   }

   @Test(groups = { "stub", "injection", "webbeansxml" })
   @SpecAssertion(section = "3.7.2")
   public void testInjectedFieldDeclaredInXmlIgnoresJavaAnnotations()
   {
      assert false;
   }

   @Test(groups = { "stub", "injection", "webbeansxml" })
   @SpecAssertion(section = "3.7.2")
   public void testInjectedFieldDeclaredInXmlAssumesCurrent()
   {
      assert false;
   }

   @Test(groups = { "stub", "injection", "webbeansxml" }, expectedExceptions = NonexistentFieldException.class)
   @SpecAssertion(section = "3.7.2")
   public void testNonexistentFieldDefinedInXml()
   {
      assert false;
   }

   @Test(groups = { "stub", "injection", "webbeansxml" })
   @SpecAssertion(section = "3.7.2")
   public void testInjectFieldsDeclaredInXmlAndJava()
   {
      assert false;
   }

   /*
    * 
    * @Test(groups="injection") @SpecAssertion(section="4.2") public void test {
    * assert false; }
    */

}
