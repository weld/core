package org.jboss.webbeans.test;


import static org.jboss.webbeans.test.util.Util.createProducerMethodBean;
import static org.jboss.webbeans.test.util.Util.createSimpleWebBean;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.DefinitionException;
import javax.webbeans.NonexistentFieldException;
import javax.webbeans.NullableDependencyException;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.test.beans.Fox;
import org.jboss.webbeans.test.beans.FoxRun;
import org.jboss.webbeans.test.beans.Tuna;
import org.jboss.webbeans.test.beans.broken.BeanWithFinalBoundField;
import org.jboss.webbeans.test.beans.broken.BeanWithStaticBoundField;
import org.jboss.webbeans.test.beans.broken.FarmHouse;
import org.jboss.webbeans.test.beans.broken.FarmHouseProducer;
import org.testng.annotations.Test;

@SpecVersion("PDR")
public class InjectionTests extends AbstractTest
{
   
   @Test(groups={"injection", "producerMethod"}) @SpecAssertion(section="4.2")
   public void testPrimitiveTypesEquivalentToBoxedTypes()
   {
      assert false;
   }
   
   @Test(groups={"injection", "producerMethod"}) @SpecAssertion(section="4.2")
   public void testInjectionPerformsBoxingIfNecessary()
   {
      assert false;
   }
   
   @Test(groups={"injection", "producerMethod"}) @SpecAssertion(section="4.2")
   public void testInjectionPerformsUnboxingIfNecessary()
   {
      assert false;
   }
   
   @Test(groups={"injection", "producerMethod"}, expectedExceptions=NullableDependencyException.class) @SpecAssertion(section="4.2")
   public void testPrimitiveInjectionPointResolvesToNullableWebBean() throws Exception
   {
      Bean<FarmHouse> farmHouseBean = createSimpleWebBean(FarmHouse.class, manager);
      manager.addBean(createProducerMethodBean(Integer.class, FarmHouseProducer.class.getMethod("getNumberOfBedrooms"), manager));
      FarmHouse farmHouse = farmHouseBean.create();
   }
   
   @Test(groups={"injection", "clientProxy"}, expectedExceptions=ContextNotActiveException.class) @SpecAssertion(section="4.3")
   public void testInvokeNormalInjectedWebBeanWhenContextNotActive()
   {
      
   }
   
   @Test(groups="injection") @SpecAssertion(section="4.3")
   public void testInvokeDependentScopeWhenContextNotActive()
   {
      Bean<FoxRun> foxRunBean = createSimpleWebBean(FoxRun.class, manager);
      Bean<Fox> foxBean = createSimpleWebBean(Fox.class, manager);
      manager.addBean(foxBean);
      FoxRun foxRun = foxRunBean.create();
      assert foxRun.fox.getName().equals("gavin");
   }
   
   @Test(groups="injection", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.6")
   public void testInjectingStaticField()
   {
      SimpleBean<BeanWithStaticBoundField> bean = createSimpleWebBean(BeanWithStaticBoundField.class, manager);
      Bean<Tuna> tunaBean = createSimpleWebBean(Tuna.class, manager);
      manager.addBean(tunaBean);
      BeanWithStaticBoundField instance = bean.create();
   }
   
   @Test(groups="injection",expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.6")
   public void testInjectingFinalField()
   {
      SimpleBean<BeanWithFinalBoundField> bean = createSimpleWebBean(BeanWithFinalBoundField.class, manager);
      Bean<Tuna> tunaBean = createSimpleWebBean(Tuna.class, manager);
      manager.addBean(tunaBean);
      BeanWithFinalBoundField instance = bean.create();
   }
   
   @Test(groups={"injection", "webbeansxml"}) @SpecAssertion(section="3.6.2")
   public void testInjectFieldsDeclaredInXml()
   {
      assert false;
   }
   
   @Test(groups={"injection", "webbeansxml"}) @SpecAssertion(section="3.6.2")
   public void testInjectedFieldDeclaredInXmlIgnoresJavaAnnotations()
   {
      assert false;
   }
   
   @Test(groups={"injection", "webbeansxml"}) @SpecAssertion(section="3.6.2")
   public void testInjectedFieldDeclaredInXmlAssumesCurrent()
   {
      assert false;
   }
   
   @Test(groups={"injection", "webbeansxml"}, expectedExceptions=NonexistentFieldException.class) @SpecAssertion(section="3.6.2")
   public void testNonexistentFieldDefinedInXml()
   {
      assert false;
   }
   
   @Test(groups={"injection", "webbeansxml"}) @SpecAssertion(section="3.6.2")
   public void testInjectFieldsDeclaredInXmlAndJava()
   {
      assert false;
   }
   
  /*

   @Test(groups="injection") @SpecAssertion(section="4.2")
   public void test
   {
      assert false;
   }

   */
   
}
