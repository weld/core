package org.jboss.webbeans.test;

import static org.jboss.webbeans.util.BeanFactory.createProducerMethodBean;
import static org.jboss.webbeans.util.BeanFactory.createSimpleBean;

import java.lang.reflect.Method;

import javax.webbeans.DefinitionException;
import javax.webbeans.DeploymentException;
import javax.webbeans.Production;
import javax.webbeans.Standard;
import javax.webbeans.UnsatisfiedDependencyException;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.HornedAnimalDeploymentType;
import org.jboss.webbeans.test.beans.BlackWidow;
import org.jboss.webbeans.test.beans.RedSnapper;
import org.jboss.webbeans.test.beans.Reindeer;
import org.jboss.webbeans.test.beans.Rhinoceros;
import org.jboss.webbeans.test.beans.SpiderProducer;
import org.jboss.webbeans.test.beans.broken.BeanWithTooManyDeploymentTypes;
import org.jboss.webbeans.test.beans.broken.Gazelle;
import org.testng.annotations.Test;

@SpecVersion("20081206")
public class DeploymentTypeTest extends AbstractTest
{
   
   @Test(expectedExceptions=DefinitionException.class) @SpecAssertion(section="2.5.1")
   public void testNonBuiltInComponentUsesStandard()
   {
      createSimpleBean(Gazelle.class);
   }
   
   @Test(groups={"stub", "annotationDefinition"}) @SpecAssertion(section="2.5.2")
   public void testDeploymentTypeHasCorrectTarget()
   {
      assert false;
   }

   @Test(groups={"stub", "annotationDefinition"}) @SpecAssertion(section="2.5.2")
   public void testDeploymentTypeHasCorrectRetention()
   {
      assert false;
   }

   @Test(groups={"stub", "annotationDefinition"}) @SpecAssertion(section="2.5.2")
   public void testDeploymentTypeDeclaresScopeTypeAnnotation()
   {
      assert false;
   }
   
   @Test(expectedExceptions=DefinitionException.class) @SpecAssertion(section="2.5.3")
   public void testTooManyDeploymentTypes()
   {
      createSimpleBean(BeanWithTooManyDeploymentTypes.class);
   }
   
   @Test @SpecAssertion(section="2.5.3")
   public void testDeploymentTypeInhertitedFromDeclaringBean() throws Exception
   {
      SimpleBean<SpiderProducer> bean = createSimpleBean(SpiderProducer.class);
      manager.addBean(bean);
      Method method = SpiderProducer.class.getMethod("produceBlackWidow");
      ProducerMethodBean<BlackWidow> blackWidowSpiderModel = createProducerMethodBean(BlackWidow.class, method, bean);
      assert blackWidowSpiderModel.getDeploymentType().equals(AnotherDeploymentType.class);
   }
   
   @Test(groups={"stub", "webbeansxml"}) @SpecAssertion(section="2.5.4")
   public void testXmlDeploymentTypeOverridesJava()
   {
      //Map<Class<? extends Annotation>, Annotation> xmlDefinedDeploymentTypeAnnotations = new HashMap<Class<? extends Annotation>, Annotation>();
      //xmlDefinedDeploymentTypeAnnotations.put(AnotherDeploymentType.class, new AnotherDeploymentTypeAnnotationLiteral());
      //AnnotatedClass<BeanWithTooManyDeploymentTypes> xmlDefinedDeploymentTypeAnnotatedItem = new SimpleAnnotatedClass<BeanWithTooManyDeploymentTypes>(BeanWithTooManyDeploymentTypes.class, xmlDefinedDeploymentTypeAnnotations);
      
      //SimpleBean<BeanWithTooManyDeploymentTypes> model = createSimpleBean(BeanWithTooManyDeploymentTypes.class, xmlDefinedDeploymentTypeAnnotatedItem, manager);
      //assert model.getDeploymentType().equals(AnotherDeploymentType.class);
      assert false;
   }
   

   
   @Test(groups={"stub", "webbeansxml"}) @SpecAssertion(section="2.5.4")
   public void testXmlRespectsJavaDeploymentType()
   {
      //AnnotatedClass<Tuna> annotatedItem = new SimpleAnnotatedClass<Tuna>(Tuna.class, new HashMap<Class<? extends Annotation>, Annotation>());
      //SimpleBean<Tuna> tuna = createSimpleBean(Tuna.class, annotatedItem, manager);
      //assert tuna.getDeploymentType().equals(AnotherDeploymentType.class);
      assert false;
   }
   
   @Test(groups={"stub", "webbeansxml"}) @SpecAssertion(section="2.5.5")
   public void testXmlDefaultDeploymentType()
   {
      //AnnotatedClass<Antelope> antelopeAnnotatedItem = new SimpleAnnotatedClass<Antelope>(Antelope.class, new HashMap<Class<? extends Annotation>, Annotation>());
      //SimpleBean<Antelope> antelope = createSimpleBean(Antelope.class, antelopeAnnotatedItem, manager);
      // assert antelope.getDeploymentType().equals(Production.class);
      assert false;
   }
   
   @Test @SpecAssertion(section="2.5.5")
   public void testHighestPrecedenceDeploymentTypeFromStereotype()
   {
      Bean<?> bean = createSimpleBean(Rhinoceros.class);
      assert bean.getDeploymentType().equals(HornedAnimalDeploymentType.class);
   }
   
   @Test(groups="beanLifecycle", expectedExceptions=UnsatisfiedDependencyException.class) @SpecAssertion(section="2.5.6")
   public void testBeanWithDisabledDeploymentTypeNotInstantiated()
   {
      manager.setEnabledDeploymentTypes(Standard.class, AnotherDeploymentType.class, HornedAnimalDeploymentType.class);
      
      Bean<RedSnapper> bean = createSimpleBean(RedSnapper.class);
      manager.addBean(bean);
      manager.getInstanceByType(RedSnapper.class);
   }
   
   @Test @SpecAssertion(section={"2.5.6", "2.5.7"})
   public void testDefaultEnabledDeploymentTypes()
   {
      manager.setEnabledDeploymentTypes();
      assert manager.getEnabledDeploymentTypes().size() == 2;
      assert manager.getEnabledDeploymentTypes().get(0).equals(Standard.class);
      assert manager.getEnabledDeploymentTypes().get(1).equals(Production.class);
   }

   @Test @SpecAssertion(section={"2.5.6", "2.5.7"})
   public void testCustomDeploymentTypes()
   {
      manager.setEnabledDeploymentTypes(Standard.class, AnotherDeploymentType.class, HornedAnimalDeploymentType.class);
      assert manager.getEnabledDeploymentTypes().size() == 3;
      assert manager.getEnabledDeploymentTypes().get(0).equals(Standard.class);
      assert manager.getEnabledDeploymentTypes().get(1).equals(AnotherDeploymentType.class);
      assert manager.getEnabledDeploymentTypes().get(2).equals(HornedAnimalDeploymentType.class);
   }
   
   @Test(expectedExceptions=DeploymentException.class) @SpecAssertion(section="2.5.6")
   public void testStandardMustBeDeclared()
   {
      manager.setEnabledDeploymentTypes(AnotherDeploymentType.class, HornedAnimalDeploymentType.class);
   }
   
   @Test(groups={"stub", "webbeansxml"}, expectedExceptions=DeploymentException.class) @SpecAssertion(section="2.5.6")
   public void testMultipleDeployElementsCannotBeDefined()
   {
      
   }
   
   @Test @SpecAssertion(section={"2.5.5", "2.7.2"})
   public void testWebBeanDeploymentTypeOverridesStereotype()
   {
      Bean<Reindeer> bean = createSimpleBean(Reindeer.class);
      assert bean.getDeploymentType().equals(Production.class);
   }
}
