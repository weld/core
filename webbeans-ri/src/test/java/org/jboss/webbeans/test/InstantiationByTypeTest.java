package org.jboss.webbeans.test;

import javax.webbeans.AmbiguousDependencyException;
import javax.webbeans.AnnotationLiteral;
import javax.webbeans.DuplicateBindingTypeException;
import javax.webbeans.TypeLiteral;
import javax.webbeans.UnproxyableDependencyException;
import javax.webbeans.UnsatisfiedDependencyException;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.binding.CurrentBinding;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.introspector.jlr.AnnotatedFieldImpl;
import org.jboss.webbeans.test.annotations.Whitefish;
import org.jboss.webbeans.test.beans.Cod;
import org.jboss.webbeans.test.beans.FishFarm;
import org.jboss.webbeans.test.beans.Salmon;
import org.jboss.webbeans.test.beans.ScottishFish;
import org.jboss.webbeans.test.beans.Sole;
import org.jboss.webbeans.test.beans.Tuna;
import org.jboss.webbeans.test.beans.broken.FinalTuna;
import org.jboss.webbeans.test.beans.broken.ParameterizedBean;
import org.jboss.webbeans.test.bindings.AnotherDeploymentTypeAnnotationLiteral;
import org.jboss.webbeans.util.BeanValidation;
import org.testng.annotations.Test;

@SpecVersion("20081206")
public class InstantiationByTypeTest extends AbstractTest
{
   
   private AnnotatedClass<FishFarm> fishFarmClass = new AnnotatedClassImpl<FishFarm>(FishFarm.class);
   
   @Test(groups={"resolution", "beanLifecycle"}) @SpecAssertion(section="5.9")
   public void testCurrentBindingTypeAssumed()
   {
      Bean<Tuna> tunaBean = SimpleBean.of(Tuna.class, manager);
      manager.addBean(tunaBean);
      assert manager.getInstanceByType(Tuna.class) != null;
   }
   
   @Test(groups="resolution", expectedExceptions=IllegalArgumentException.class) @SpecAssertion(section="5.9")
   public void testParameterizedTypeWithWildcardParameter()
   {
      manager.getInstanceByType(new TypeLiteral<ParameterizedBean<?>>(){});
   }
   
   @Test(groups="resolution", expectedExceptions=IllegalArgumentException.class) @SpecAssertion(section="5.9")
   public  <T> void testParameterizedTypeWithTypeParameter()
   {
      manager.getInstanceByType(new TypeLiteral<ParameterizedBean<T>>(){});
   }
   
   
   @Test(groups="resolution", expectedExceptions=DuplicateBindingTypeException.class) @SpecAssertion(section="5.9")
   public void testDuplicateBindingTypesUsed()
   {
      manager.getInstanceByType(Tuna.class, new CurrentBinding(), new CurrentBinding());
   }
   
   @Test(groups="resolution", expectedExceptions=IllegalArgumentException.class) @SpecAssertion(section="5.9")
   public void testNonBindingTypeUsed()
   {
      manager.getInstanceByType(Tuna.class, new AnotherDeploymentTypeAnnotationLiteral());
   }
   
   @Test(expectedExceptions=AmbiguousDependencyException.class) @SpecAssertion(section="5.9")
   public void testAmbiguousDependencies() throws Exception
   {
      AnnotatedField<ScottishFish> whiteScottishFishField = new AnnotatedFieldImpl<ScottishFish>(FishFarm.class.getDeclaredField("whiteScottishFish"), fishFarmClass);
      Bean<Cod> codBean = SimpleBean.of(Cod.class, manager);
      Bean<Salmon> salmonBean = SimpleBean.of(Salmon.class, manager);
      Bean<Sole> soleBean = SimpleBean.of(Sole.class, manager);
      manager.addBean(codBean);
      manager.addBean(salmonBean);
      manager.addBean(soleBean);
      manager.getInstanceByType(ScottishFish.class, new AnnotationLiteral<Whitefish>(){});
   }
   
   @Test(expectedExceptions=UnsatisfiedDependencyException.class) @SpecAssertion(section="5.9")
   public void testUnsatisfiedDependencies() throws Exception
   {
      AnnotatedField<ScottishFish> whiteScottishFishField = new AnnotatedFieldImpl<ScottishFish>(FishFarm.class.getDeclaredField("whiteScottishFish"), fishFarmClass);
      Bean<Cod> codBean = SimpleBean.of(Cod.class, manager);
      Bean<Salmon> salmonBean = SimpleBean.of(Salmon.class, manager);
      Bean<Sole> soleBean = SimpleBean.of(Sole.class, manager);
      manager.addBean(codBean);
      manager.addBean(salmonBean);
      manager.addBean(soleBean);
      manager.getInstanceByType(Tuna.class, new CurrentBinding());
   }
   
   @Test(expectedExceptions=UnproxyableDependencyException.class) @SpecAssertion(section="5.9")
   public void testUnproxyableDependencies() throws Exception
   {
      Bean<FinalTuna> FinalTunaBean = SimpleBean.of(FinalTuna.class, manager);
      manager.addBean(FinalTunaBean);
      BeanValidation.validate(manager.getBeans());
      manager.getInstanceByType(FinalTuna.class, new AnnotationLiteral<Whitefish>(){});
   }
   
   /*

   @Test(groups="resolution") @SpecAssertion(section="5.9")
   public void test
   {
      assert false;
   }

   */  
   
}
