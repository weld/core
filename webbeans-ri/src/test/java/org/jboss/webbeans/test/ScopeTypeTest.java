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

public class ScopeTypeTest extends AbstractModelTest
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
      SimpleComponentModel<SeaBass> trout = new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType(SeaBass.class), emptyAnnotatedItem, container);
      assert trout.getScopeType().annotationType().equals(RequestScoped.class);
   }
   
   @Test @SpecAssertion(section="2.4.3")
   public void testTooManyScopesSpecifiedInJava()
   {
      boolean exception = false;
      try
      {
         new SimpleComponentModel<ComponentWithTooManyScopeTypes>(new SimpleAnnotatedType(ComponentWithTooManyScopeTypes.class), emptyAnnotatedItem, container);
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
         AnnotatedType antelopeAnnotatedItem = new SimpleAnnotatedType(Antelope.class, annotations);
         new SimpleComponentModel<Antelope>(emptyAnnotatedItem, antelopeAnnotatedItem, container);
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
      AnnotatedType annotatedItem = new SimpleAnnotatedType(Order.class, annotations);
      
      SimpleComponentModel<Order> order = new SimpleComponentModel<Order>(new SimpleAnnotatedType(Order.class), annotatedItem, container);
      assert order.getScopeType().annotationType().equals(RequestScoped.class);
   }
   
   @Test @SpecAssertion(section="2.4.4")
   public void testScopeMissingInXml()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      AnnotatedType annotatedItem = new SimpleAnnotatedType(SeaBass.class, annotations);
      
      SimpleComponentModel<SeaBass> trout = new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType(SeaBass.class), annotatedItem, container);
      assert trout.getScopeType().annotationType().equals(RequestScoped.class);
   }

   @Test @SpecAssertion(section="2.4.4")
   public void testScopeDeclaredInXmlOverridesJava()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(ConversationScoped.class, new ConversationScopedAnnotationLiteral());
      AnnotatedType annotatedItem = new SimpleAnnotatedType(SeaBass.class, annotations);
      SimpleComponentModel<SeaBass> trout = new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType(SeaBass.class), annotatedItem, container);
      assert trout.getScopeType().annotationType().equals(ConversationScoped.class);
   }
   
   @Test @SpecAssertion(section="2.4.5")
   public void testDefaultScope()
   {
      SimpleComponentModel<Order> order = new SimpleComponentModel<Order>(new SimpleAnnotatedType(Order.class), emptyAnnotatedItem, container);
      assert order.getScopeType().annotationType().equals(Dependent.class);
   }
   
   @Test @SpecAssertion(section={"2.4.5", "2.7.2"})
   public void testScopeSpecifiedAndStereotyped()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeAnnotationLiteral());
      AnnotatedType annotatedItem = new SimpleAnnotatedType(SeaBass.class, annotations);
      SimpleComponentModel<SeaBass> trout = new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType(SeaBass.class), annotatedItem, container);
      assert trout.getScopeType().annotationType().equals(RequestScoped.class);
   }
   
   @Test @SpecAssertion(section="2.4.5")
   public void testMutipleIncompatibleScopeStereotypes()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeAnnotationLiteral());
      annotations.put(AnimalStereotype.class, new AnimalStereotypeAnnotationLiteral());
      AnnotatedType annotatedItem = new SimpleAnnotatedType(Haddock.class, annotations);
      
      boolean exception = false;
      try
      {
         new SimpleComponentModel<Haddock>(new SimpleAnnotatedType(Haddock.class), annotatedItem, container);
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
      AnnotatedType annotatedItem = new SimpleAnnotatedType(SeaBass.class, annotations);
      
      SimpleComponentModel<SeaBass> trout = new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType(SeaBass.class), annotatedItem, container);
      assert trout.getScopeType().annotationType().equals(RequestScoped.class);     
   }
   
   @Test @SpecAssertion(section="2.4.5")
   public void testMutipleCompatibleScopeStereotypes()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeAnnotationLiteral());
      annotations.put(RiverFishStereotype.class, new RiverFishStereotypeAnnotationLiteral());
      AnnotatedType annotatedItem = new SimpleAnnotatedType(Haddock.class, annotations);
      
      SimpleComponentModel<Haddock> haddock = new SimpleComponentModel<Haddock>(new SimpleAnnotatedType(Haddock.class), annotatedItem, container);
      assert haddock.getScopeType().annotationType().equals(ApplicationScoped.class);
   }
   
}
