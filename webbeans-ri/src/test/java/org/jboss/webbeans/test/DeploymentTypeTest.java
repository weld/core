package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.createSimpleModel;
import static org.jboss.webbeans.test.util.Util.createSimpleWebBean;

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
import javax.webbeans.UnsatisfiedDependencyException;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.impl.SimpleAnnotatedClass;
import org.jboss.webbeans.model.bean.SimpleBeanModel;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.FishStereotype;
import org.jboss.webbeans.test.annotations.HornedAnimalDeploymentType;
import org.jboss.webbeans.test.beans.Antelope;
import org.jboss.webbeans.test.beans.RedSnapper;
import org.jboss.webbeans.test.beans.Reindeer;
import org.jboss.webbeans.test.beans.Rhinoceros;
import org.jboss.webbeans.test.beans.SeaBass;
import org.jboss.webbeans.test.beans.Tuna;
import org.jboss.webbeans.test.beans.broken.BeanWithTooManyDeploymentTypes;
import org.jboss.webbeans.test.beans.broken.Gazelle;
import org.jboss.webbeans.test.bindings.AnotherDeploymentTypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.FishStereotypeAnnotationLiteral;
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
      createSimpleModel(BeanWithTooManyDeploymentTypes.class, manager);
   }
   
   @Test @SpecAssertion(section="2.5.4")
   public void testXmlDeploymentTypeOverridesJava()
   {
      Map<Class<? extends Annotation>, Annotation> xmlDefinedDeploymentTypeAnnotations = new HashMap<Class<? extends Annotation>, Annotation>();
      xmlDefinedDeploymentTypeAnnotations.put(AnotherDeploymentType.class, new AnotherDeploymentTypeAnnotationLiteral());
      AnnotatedClass<BeanWithTooManyDeploymentTypes> xmlDefinedDeploymentTypeAnnotatedItem = new SimpleAnnotatedClass<BeanWithTooManyDeploymentTypes>(BeanWithTooManyDeploymentTypes.class, xmlDefinedDeploymentTypeAnnotations);
      
      SimpleBeanModel<BeanWithTooManyDeploymentTypes> model = new SimpleBeanModel<BeanWithTooManyDeploymentTypes>(new SimpleAnnotatedClass<BeanWithTooManyDeploymentTypes>(BeanWithTooManyDeploymentTypes.class), xmlDefinedDeploymentTypeAnnotatedItem, manager);
      assert model.getDeploymentType().equals(AnotherDeploymentType.class);
   }
   

   
   @Test @SpecAssertion(section="2.5.4")
   public void testXmlRespectsJavaDeploymentType()
   {
      AnnotatedClass<Tuna> annotatedItem = new SimpleAnnotatedClass<Tuna>(Tuna.class, new HashMap<Class<? extends Annotation>, Annotation>());
      SimpleBeanModel<Tuna> tuna = new SimpleBeanModel<Tuna>(new SimpleAnnotatedClass<Tuna>(Tuna.class), annotatedItem, manager);
      assert tuna.getDeploymentType().equals(AnotherDeploymentType.class);
   }
   
   @Test(groups="webbeansxml") @SpecAssertion(section="2.5.5")
   public void testXmlDefaultDeploymentType()
   {
      AnnotatedClass<Antelope> antelopeAnnotatedItem = new SimpleAnnotatedClass<Antelope>(Antelope.class, new HashMap<Class<? extends Annotation>, Annotation>());
      SimpleBeanModel<Antelope> antelope = new SimpleBeanModel<Antelope>(null, antelopeAnnotatedItem, manager);
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
      AnnotatedClass<SeaBass> annotatedItem = new SimpleAnnotatedClass<SeaBass>(SeaBass.class, annotations);
      SimpleBeanModel<SeaBass> trout = new SimpleBeanModel<SeaBass>(new SimpleAnnotatedClass<SeaBass>(SeaBass.class), annotatedItem, manager);
      assert trout.getScopeType().equals(RequestScoped.class);
   }
   
   @Test(groups="beanLifecycle", expectedExceptions=UnsatisfiedDependencyException.class) @SpecAssertion(section="2.5.6")
   public void testBeanWithDisabledDeploymentTypeNotInstantiated()
   {
      List<Class<? extends Annotation>> enabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      enabledDeploymentTypes.add(Standard.class);
      enabledDeploymentTypes.add(AnotherDeploymentType.class);
      enabledDeploymentTypes.add(HornedAnimalDeploymentType.class);
      manager.setEnabledDeploymentTypes(enabledDeploymentTypes);
      
      Bean<RedSnapper> bean = createSimpleWebBean(RedSnapper.class, manager);
      manager.addBean(bean);
      manager.getInstanceByType(RedSnapper.class);
   }
   
   @Test @SpecAssertion(section={"2.5.6", "2.5.7"})
   public void testDefaultEnabledDeploymentTypes()
   {
      manager.setEnabledDeploymentTypes(null);
      assert manager.getEnabledDeploymentTypes().size() == 2;
      assert manager.getEnabledDeploymentTypes().get(0).equals(Standard.class);
      assert manager.getEnabledDeploymentTypes().get(1).equals(Production.class);
   }

   @Test @SpecAssertion(section={"2.5.6", "2.5.7"})
   public void testCustomDeploymentTypes()
   {
      List<Class<? extends Annotation>> enabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      enabledDeploymentTypes.add(Standard.class);
      enabledDeploymentTypes.add(AnotherDeploymentType.class);
      enabledDeploymentTypes.add(HornedAnimalDeploymentType.class);
      manager.setEnabledDeploymentTypes(enabledDeploymentTypes);
      assert manager.getEnabledDeploymentTypes().size() == 3;
      assert manager.getEnabledDeploymentTypes().get(0).equals(Standard.class);
      assert manager.getEnabledDeploymentTypes().get(1).equals(AnotherDeploymentType.class);
      assert manager.getEnabledDeploymentTypes().get(2).equals(HornedAnimalDeploymentType.class);
   }
   
   @Test(expectedExceptions=DeploymentException.class) @SpecAssertion(section="2.5.6")
   public void testStandardMustBeDeclared()
   {
      List<Class<? extends Annotation>> enabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      enabledDeploymentTypes.add(AnotherDeploymentType.class);
      enabledDeploymentTypes.add(HornedAnimalDeploymentType.class);
      manager.setEnabledDeploymentTypes(enabledDeploymentTypes);
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
