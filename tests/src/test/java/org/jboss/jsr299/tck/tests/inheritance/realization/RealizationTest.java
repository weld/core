package org.jboss.jsr299.tck.tests.inheritance.realization;

import java.lang.annotation.Annotation;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.AnnotationLiteral;
import javax.enterprise.inject.Any;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.jsr299.BeansXml;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

/**
 * 
 * Spec version: PRD2
 * 
 */
@Artifact
@BeansXml("beans.xml")
public class RealizationTest extends AbstractWebBeansTest
{

   private static Annotation SMELLY_LITERAL = new AnnotationLiteral<Smelly>()
   {
   };
   private static Annotation TAME_LITERAL = new AnnotationLiteral<Tame>()
   {
   };

   @Test
   public void testRealizedBeanWithProducerMethodHasSameScope()
   {
      assert getBeans(CowDung.class, SMELLY_LITERAL).size() == 1;
      assert getBeans(CowDung.class, SMELLY_LITERAL).iterator().next().getScopeType().equals(RequestScoped.class);
   }

   @Test
   public void testRealizedBeanWithProducerMethodHasDeploymentTypeOfRealizingClass()
   {
      assert getBeans(CowDung.class, SMELLY_LITERAL).size() == 1;
      assert getBeans(CowDung.class, SMELLY_LITERAL).iterator().next().getDeploymentType().equals(AnotherDeploymentType.class);
   }

   @Test
   public void testRealizedBeanWithProducerMethodHasSameBindings()
   {
      assert getCurrentManager().getBeans(CowDung.class, SMELLY_LITERAL).size() == 1;
      assert getCurrentManager().getBeans(CowDung.class, SMELLY_LITERAL).iterator().next().getBindings().size() == 2;
      assert getCurrentManager().getBeans(CowDung.class, SMELLY_LITERAL).iterator().next().getBindings().contains(SMELLY_LITERAL);
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testRealizedBeanWithProducerMethodHasBindingsOfMethodAndRealizingType()
   {
      assert getCurrentManager().getBeans(CowDung.class, SMELLY_LITERAL, TAME_LITERAL).size() == 1;
      assert getCurrentManager().getBeans(CowDung.class, SMELLY_LITERAL, TAME_LITERAL).iterator().next().getBindings().size() == 3;
      assert annotationSetMatches(getCurrentManager().getBeans(CowDung.class, SMELLY_LITERAL, TAME_LITERAL).iterator().next().getBindings(), Smelly.class, Tame.class, Any.class);
   }

   @Test
   public void testRealizedBeanWithProducerMethodHasSameStereotypes()
   {
      assert getBeans(Dog.class).size() == 1;
      assert getBeans(Dog.class).iterator().next().getScopeType().equals(RequestScoped.class);
   }

   @Test
   public void testRealizedBeanWithProducerFieldHasSameScope()
   {
      assert getBeans(HorseDung.class, SMELLY_LITERAL).size() == 1;
      assert getBeans(HorseDung.class, SMELLY_LITERAL).iterator().next().getScopeType().equals(RequestScoped.class);
   }

   @Test
   public void testRealizedBeanWithProducerFieldHasDeploymentTypeOfRealizingClass()
   {
      assert getBeans(HorseDung.class, SMELLY_LITERAL).size() == 1;
      assert getBeans(HorseDung.class, SMELLY_LITERAL).iterator().next().getDeploymentType().equals(AnotherDeploymentType.class);
   }

   @Test
   public void testRealizedBeanWithProducerFieldHasSameBindings()
   {
      assert getCurrentManager().getBeans(HorseDung.class, SMELLY_LITERAL).size() == 1;
      assert getCurrentManager().getBeans(HorseDung.class, SMELLY_LITERAL).iterator().next().getBindings().size() == 2;
      assert getCurrentManager().getBeans(HorseDung.class, SMELLY_LITERAL).iterator().next().getBindings().contains(SMELLY_LITERAL);
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testRealizedBeanWithProducerFieldHasBindingsOfMethodAndRealizingType()
   {
      assert getCurrentManager().getBeans(HorseDung.class, SMELLY_LITERAL, TAME_LITERAL).size() == 1;
      assert getCurrentManager().getBeans(HorseDung.class, SMELLY_LITERAL, TAME_LITERAL).iterator().next().getBindings().size() == 3;
      assert annotationSetMatches(getCurrentManager().getBeans(HorseDung.class, SMELLY_LITERAL, TAME_LITERAL).iterator().next().getBindings(), Smelly.class, Tame.class, Any.class);
   }

   @Test
   public void testRealizedBeanWithProducerFieldHasSameStereotypes()
   {
      assert getBeans(Donkey.class).size() == 1;
      assert getBeans(Donkey.class).iterator().next().getScopeType().equals(RequestScoped.class);
   }

   @Test(groups = "stub")
   public void testDisposalMethodInherited()
   {
      assert false;
   }

   @Test(groups = "stub")
   public void testRealizedDisposalMethodHasBindingsOfMethodAndRealizingType()
   {
      assert false;
   }

   @Test
   public void testObserverMethodInheritedAndHasSameBindings()
   {
      assert getCurrentManager().resolveObservers(new Cow(), new AnnotationLiteral<Tame>()
      {
      }).size() == 1;
      assert getCurrentManager().resolveObservers(new Cow(), SMELLY_LITERAL).size() == 0;
   }

}
