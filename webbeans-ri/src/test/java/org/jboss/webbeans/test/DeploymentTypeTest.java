package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.createSimpleWebBean;
import static org.jboss.webbeans.test.util.Util.getEmptyAnnotatedType;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.webbeans.DefinitionException;
import javax.webbeans.DeploymentException;
import javax.webbeans.Production;
import javax.webbeans.RequestScoped;
import javax.webbeans.Standard;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.bean.SimpleBeanModel;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.FishStereotype;
import org.jboss.webbeans.test.annotations.HornedAnimalDeploymentType;
import org.jboss.webbeans.test.beans.Antelope;
import org.jboss.webbeans.test.beans.Reindeer;
import org.jboss.webbeans.test.beans.Rhinoceros;
import org.jboss.webbeans.test.beans.SeaBass;
import org.jboss.webbeans.test.beans.Tuna;
import org.jboss.webbeans.test.beans.broken.BeanWithTooManyDeploymentTypes;
import org.jboss.webbeans.test.beans.broken.Gazelle;
import org.jboss.webbeans.test.bindings.AnotherDeploymentTypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.FishStereotypeAnnotationLiteral;
import org.jboss.webbeans.test.mock.MockManagerImpl;
import org.testng.annotations.Test;

@SpecVersion("PDR")
public class DeploymentTypeTest extends AbstractTest
{
   
   @Test(expectedExceptions=DefinitionException.class) @SpecAssertion(section="2.5.1")
   public void testNonBuiltInComponentUsesStandard()
   {
      createSimpleWebBean(Gazelle.class, manager);
   }
   
   @Test(groups="annotationDefinition") @SpecAssertion(section="2.5.2")
   public void testDeploymentTypeHasCorrectTarget()
   {
      assert false;
   }

   @Test(groups="annotationDefinition") @SpecAssertion(section="2.5.2")
   public void testDeploymentTypeHasCorrectRetention()
   {
      assert false;
   }

   @Test(groups="annotationDefinition") @SpecAssertion(section="2.5.2")
   public void testDeploymentTypeDeclaresScopeTypeAnnotation()
   {
      assert false;
   }
   
   @Test(expectedExceptions=DefinitionException.class) @SpecAssertion(section="2.5.3")
   public void testTooManyDeploymentTypes()
   {
      new SimpleBeanModel<BeanWithTooManyDeploymentTypes>(new SimpleAnnotatedType<BeanWithTooManyDeploymentTypes>(BeanWithTooManyDeploymentTypes.class), getEmptyAnnotatedType(BeanWithTooManyDeploymentTypes.class), manager);
   }
   
   @Test @SpecAssertion(section="2.5.4")
   public void testXmlDeploymentTypeOverridesJava()
   {
      Map<Class<? extends Annotation>, Annotation> xmlDefinedDeploymentTypeAnnotations = new HashMap<Class<? extends Annotation>, Annotation>();
      xmlDefinedDeploymentTypeAnnotations.put(AnotherDeploymentType.class, new AnotherDeploymentTypeAnnotationLiteral());
      AnnotatedType<BeanWithTooManyDeploymentTypes> xmlDefinedDeploymentTypeAnnotatedItem = new SimpleAnnotatedType<BeanWithTooManyDeploymentTypes>(BeanWithTooManyDeploymentTypes.class, xmlDefinedDeploymentTypeAnnotations);
      
      SimpleBeanModel<BeanWithTooManyDeploymentTypes> model = new SimpleBeanModel<BeanWithTooManyDeploymentTypes>(new SimpleAnnotatedType<BeanWithTooManyDeploymentTypes>(BeanWithTooManyDeploymentTypes.class), xmlDefinedDeploymentTypeAnnotatedItem, manager);
      assert model.getDeploymentType().equals(AnotherDeploymentType.class);
   }
   

   
   @Test @SpecAssertion(section="2.5.4")
   public void testXmlRespectsJavaDeploymentType()
   {
      AnnotatedType<Tuna> annotatedItem = new SimpleAnnotatedType<Tuna>(Tuna.class, new HashMap<Class<? extends Annotation>, Annotation>());
      SimpleBeanModel<Tuna> tuna = new SimpleBeanModel<Tuna>(new SimpleAnnotatedType<Tuna>(Tuna.class), annotatedItem, manager);
      assert tuna.getDeploymentType().equals(AnotherDeploymentType.class);
   }
   
   @Test @SpecAssertion(section="2.5.5")
   public void testXmlDefaultDeploymentType()
   {
      AnnotatedType<Antelope> antelopeAnnotatedItem = new SimpleAnnotatedType<Antelope>(Antelope.class, new HashMap<Class<? extends Annotation>, Annotation>());
      SimpleBeanModel<Antelope> antelope = new SimpleBeanModel<Antelope>(getEmptyAnnotatedType(Antelope.class), antelopeAnnotatedItem, manager);
      assert antelope.getDeploymentType().equals(Production.class);
   }
   
   @Test @SpecAssertion(section="2.5.5")
   public void testHighestPrecedenceDeploymentTypeFromStereotype()
   {
      Bean<?> bean = createSimpleWebBean(Rhinoceros.class, manager);
      assert bean.getDeploymentType().equals(HornedAnimalDeploymentType.class);
   }
   
   @Test @SpecAssertion(section="2.5.5")
   public void testDeploymentTypeSpecifiedAndStereotyped()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeAnnotationLiteral());
      AnnotatedType<SeaBass> annotatedItem = new SimpleAnnotatedType<SeaBass>(SeaBass.class, annotations);
      SimpleBeanModel<SeaBass> trout = new SimpleBeanModel<SeaBass>(new SimpleAnnotatedType<SeaBass>(SeaBass.class), annotatedItem, manager);
      assert trout.getScopeType().equals(RequestScoped.class);
   }
   
   @Test(groups="beanLifecycle") @SpecAssertion(section="2.5.6")
   public void testBeanWithDisabledDeploymentTypeNotInstantiated()
   {
      assert false;
   }
   
   @Test @SpecAssertion(section={"2.5.6", "2.5.7"})
   public void testDefaultEnabledDeploymentTypes()
   {
      ManagerImpl container = new MockManagerImpl(null);
      assert container.getEnabledDeploymentTypes().size() == 2;
      assert container.getEnabledDeploymentTypes().get(0).equals(Standard.class);
      assert container.getEnabledDeploymentTypes().get(1).equals(Production.class);
   }

   @Test @SpecAssertion(section={"2.5.6", "2.5.7"})
   public void testCustomDeploymentTypes()
   {
      List<Class<? extends Annotation>> enabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      enabledDeploymentTypes.add(Standard.class);
      enabledDeploymentTypes.add(AnotherDeploymentType.class);
      enabledDeploymentTypes.add(HornedAnimalDeploymentType.class);
      ManagerImpl container = new MockManagerImpl(enabledDeploymentTypes);
      assert container.getEnabledDeploymentTypes().size() == 3;
      assert container.getEnabledDeploymentTypes().get(0).equals(Standard.class);
      assert container.getEnabledDeploymentTypes().get(1).equals(AnotherDeploymentType.class);
      assert container.getEnabledDeploymentTypes().get(2).equals(HornedAnimalDeploymentType.class);
   }
   
   @Test(expectedExceptions=DeploymentException.class) @SpecAssertion(section="2.5.6")
   public void testStandardMustBeDeclared()
   {
      List<Class<? extends Annotation>> enabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      enabledDeploymentTypes.add(AnotherDeploymentType.class);
      enabledDeploymentTypes.add(HornedAnimalDeploymentType.class);
      new MockManagerImpl(enabledDeploymentTypes);
   }
   
   @Test(groups="webbeansxml", expectedExceptions=DeploymentException.class) @SpecAssertion(section="2.5.6")
   public void testMultipleDeployElementsCannotBeDefined()
   {
      
   }
   
   @Test @SpecAssertion(section="2.7.2")
   public void testWebBeanDeploymentTypeOverridesStereotype()
   {
      Bean<Reindeer> bean = createSimpleWebBean(Reindeer.class, manager);
      assert bean.getDeploymentType().equals(Production.class);
   }
}
