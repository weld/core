package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.webbeans.ApplicationScoped;
import javax.webbeans.ConversationScoped;
import javax.webbeans.Dependent;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.bindings.ConversationScopedAnnotationLiteral;
import org.jboss.webbeans.bindings.RequestScopedAnnotationLiteral;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.SimpleComponentModel;
import org.jboss.webbeans.test.annotations.AnimalStereotype;
import org.jboss.webbeans.test.annotations.FishStereotype;
import org.jboss.webbeans.test.annotations.RiverFishStereotype;
import org.jboss.webbeans.test.bindings.AnimalStereotypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.FishStereotypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.RiverFishStereotypeAnnotationLiteral;
import org.jboss.webbeans.test.components.Antelope;
import org.jboss.webbeans.test.components.ComponentWithTooManyScopeTypes;
import org.jboss.webbeans.test.components.Haddock;
import org.jboss.webbeans.test.components.Order;
import org.jboss.webbeans.test.components.SeaBass;
import org.testng.annotations.Test;

public class ScopeTypeTest extends AbstractTest
{
   
   @Test @SpecAssertion(section="2.4")
   public void testScopeTypesAreExtensible()
   {
      assert false;
   }
   
   @Test @SpecAssertion(section="2.4.2")
   public void testScopeTypeHasCorrectTarget()
   {
      assert false;
   }

   @Test @SpecAssertion(section="2.4.2")
   public void testScopeTypeHasCorrectRetention()
   {
      assert false;
   }

   @Test @SpecAssertion(section="2.4.2")
   public void testScopeTypeDeclaresBindingTypeAnnotation()
   {
      assert false;
   }
   
   @Test @SpecAssertion(section="2.4.3")
   public void testScopeDeclaredInJava()
   {
      SimpleComponentModel<SeaBass> trout = new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType<SeaBass>(SeaBass.class), getEmptyAnnotatedItem(SeaBass.class), manager);
      assert trout.getScopeType().equals(RequestScoped.class);
   }
   
   @Test @SpecAssertion(section="2.4.3")
   public void testTooManyScopesSpecifiedInJava()
   {
      boolean exception = false;
      try
      {
         new SimpleComponentModel<ComponentWithTooManyScopeTypes>(new SimpleAnnotatedType<ComponentWithTooManyScopeTypes>(ComponentWithTooManyScopeTypes.class), getEmptyAnnotatedItem(ComponentWithTooManyScopeTypes.class), manager);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;  
   }
   
   @Test @SpecAssertion(section="2.4.3")
   public void testTooManyScopesSpecifiedInXml()
   {
      boolean exception = false;
      try
      {
         Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
         annotations.put(RequestScoped.class, new RequestScopedAnnotationLiteral());
         annotations.put(ConversationScoped.class, new ConversationScopedAnnotationLiteral());
         AnnotatedType<Antelope> antelopeAnnotatedItem = new SimpleAnnotatedType<Antelope>(Antelope.class, annotations);
         new SimpleComponentModel<Antelope>(getEmptyAnnotatedItem(Antelope.class), antelopeAnnotatedItem, manager);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;  
   }
   
   @Test @SpecAssertion(section="2.4.4")
   public void testScopeDeclaredInXml()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(RequestScoped.class, new RequestScopedAnnotationLiteral());
      AnnotatedType<Order> annotatedItem = new SimpleAnnotatedType<Order>(Order.class, annotations);
      
      SimpleComponentModel<Order> order = new SimpleComponentModel<Order>(new SimpleAnnotatedType<Order>(Order.class), annotatedItem, manager);
      assert order.getScopeType().equals(RequestScoped.class);
   }
   
   @Test @SpecAssertion(section="2.4.4")
   public void testScopeMissingInXml()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      AnnotatedType<SeaBass> annotatedItem = new SimpleAnnotatedType<SeaBass>(SeaBass.class, annotations);
      
      SimpleComponentModel<SeaBass> trout = new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType<SeaBass>(SeaBass.class), annotatedItem, manager);
      assert trout.getScopeType().equals(RequestScoped.class);
   }

   @Test @SpecAssertion(section="2.4.4")
   public void testScopeDeclaredInXmlOverridesJava()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(ConversationScoped.class, new ConversationScopedAnnotationLiteral());
      AnnotatedType<SeaBass> annotatedItem = new SimpleAnnotatedType<SeaBass>(SeaBass.class, annotations);
      SimpleComponentModel<SeaBass> trout = new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType<SeaBass>(SeaBass.class), annotatedItem, manager);
      assert trout.getScopeType().equals(ConversationScoped.class);
   }
   
   @Test @SpecAssertion(section="2.4.5")
   public void testDefaultScope()
   {
      SimpleComponentModel<Order> order = new SimpleComponentModel<Order>(new SimpleAnnotatedType<Order>(Order.class), getEmptyAnnotatedItem(Order.class), manager);
      assert order.getScopeType().equals(Dependent.class);
   }
   
   @Test @SpecAssertion(section={"2.4.5", "2.7.2"})
   public void testScopeSpecifiedAndStereotyped()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeAnnotationLiteral());
      AnnotatedType<SeaBass> annotatedItem = new SimpleAnnotatedType<SeaBass>(SeaBass.class, annotations);
      SimpleComponentModel<SeaBass> trout = new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType<SeaBass>(SeaBass.class), annotatedItem, manager);
      assert trout.getScopeType().equals(RequestScoped.class);
   }
   
   @Test @SpecAssertion(section="2.4.5")
   public void testMutipleIncompatibleScopeStereotypes()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeAnnotationLiteral());
      annotations.put(AnimalStereotype.class, new AnimalStereotypeAnnotationLiteral());
      AnnotatedType<Haddock> annotatedItem = new SimpleAnnotatedType<Haddock>(Haddock.class, annotations);
      
      boolean exception = false;
      try
      {
         new SimpleComponentModel<Haddock>(new SimpleAnnotatedType<Haddock>(Haddock.class), annotatedItem, manager);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   @Test @SpecAssertion(section="2.4.5")
   public void testMutipleIncompatibleScopeStereotypesWithScopeSpecified()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeAnnotationLiteral());
      annotations.put(AnimalStereotype.class, new AnimalStereotypeAnnotationLiteral());
      AnnotatedType<SeaBass> annotatedItem = new SimpleAnnotatedType<SeaBass>(SeaBass.class, annotations);
      
      SimpleComponentModel<SeaBass> trout = new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType<SeaBass>(SeaBass.class), annotatedItem, manager);
      assert trout.getScopeType().equals(RequestScoped.class);     
   }
   
   @Test @SpecAssertion(section="2.4.5")
   public void testMutipleCompatibleScopeStereotypes()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeAnnotationLiteral());
      annotations.put(RiverFishStereotype.class, new RiverFishStereotypeAnnotationLiteral());
      AnnotatedType<Haddock> annotatedItem = new SimpleAnnotatedType<Haddock>(Haddock.class, annotations);
      
      SimpleComponentModel<Haddock> haddock = new SimpleComponentModel<Haddock>(new SimpleAnnotatedType<Haddock>(Haddock.class), annotatedItem, manager);
      assert haddock.getScopeType().equals(ApplicationScoped.class);
   }
   
}
