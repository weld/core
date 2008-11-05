package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.createSimpleWebBean;
import static org.jboss.webbeans.test.util.Util.getEmptyAnnotatedType;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.webbeans.ApplicationScoped;
import javax.webbeans.ConversationScoped;
import javax.webbeans.DefinitionException;
import javax.webbeans.Dependent;
import javax.webbeans.RequestScoped;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.bindings.ConversationScopedAnnotationLiteral;
import org.jboss.webbeans.bindings.RequestScopedAnnotationLiteral;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.impl.SimpleAnnotatedClass;
import org.jboss.webbeans.model.bean.SimpleBeanModel;
import org.jboss.webbeans.test.annotations.AnimalStereotype;
import org.jboss.webbeans.test.annotations.AnotherScopeType;
import org.jboss.webbeans.test.annotations.FishStereotype;
import org.jboss.webbeans.test.annotations.RiverFishStereotype;
import org.jboss.webbeans.test.beans.Antelope;
import org.jboss.webbeans.test.beans.BeanWithTooManyScopeTypes;
import org.jboss.webbeans.test.beans.Haddock;
import org.jboss.webbeans.test.beans.Mullet;
import org.jboss.webbeans.test.beans.Order;
import org.jboss.webbeans.test.beans.RedSnapper;
import org.jboss.webbeans.test.beans.SeaBass;
import org.jboss.webbeans.test.bindings.AnimalStereotypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.FishStereotypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.RiverFishStereotypeAnnotationLiteral;
import org.testng.annotations.Test;

@SpecVersion("PDR")
public class ScopeTypeTest extends AbstractTest
{
   
   @Test @SpecAssertion(section="2.4")
   public void testScopeTypesAreExtensible()
   {
      Bean<Mullet> mullet = createSimpleWebBean(Mullet.class, manager);
      assert mullet.getScopeType().equals(AnotherScopeType.class);
   }
   
   @Test(groups="annotationDefinition") @SpecAssertion(section="2.4.2")
   public void testScopeTypeHasCorrectTarget()
   {
      assert false;
   }

   @Test(groups="annotationDefinition") @SpecAssertion(section="2.4.2")
   public void testScopeTypeHasCorrectRetention()
   {
      assert false;
   }

   @Test(groups="annotationDefinition") @SpecAssertion(section="2.4.2")
   public void testScopeTypeDeclaresScopeTypeAnnotation()
   {
      assert false;
   }
   
   @Test @SpecAssertion(section="2.4.3")
   public void testScopeDeclaredInJava()
   {
      SimpleBeanModel<SeaBass> trout = new SimpleBeanModel<SeaBass>(new SimpleAnnotatedClass<SeaBass>(SeaBass.class), getEmptyAnnotatedType(SeaBass.class), manager);
      assert trout.getScopeType().equals(RequestScoped.class);
   }
   
   @Test(expectedExceptions=DefinitionException.class) @SpecAssertion(section="2.4.3")
   public void testTooManyScopesSpecifiedInJava()
   {
      new SimpleBeanModel<BeanWithTooManyScopeTypes>(new SimpleAnnotatedClass<BeanWithTooManyScopeTypes>(BeanWithTooManyScopeTypes.class), getEmptyAnnotatedType(BeanWithTooManyScopeTypes.class), manager);
   }
   
   @Test(expectedExceptions=DefinitionException.class)
   public void testTooManyScopesSpecifiedInXml()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(RequestScoped.class, new RequestScopedAnnotationLiteral());
      annotations.put(ConversationScoped.class, new ConversationScopedAnnotationLiteral());
      AnnotatedClass<Antelope> antelopeAnnotatedItem = new SimpleAnnotatedClass<Antelope>(Antelope.class, annotations);
      new SimpleBeanModel<Antelope>(getEmptyAnnotatedType(Antelope.class), antelopeAnnotatedItem, manager);
   }
   
   @Test @SpecAssertion(section="2.4.4")
   public void testScopeDeclaredInXml()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(RequestScoped.class, new RequestScopedAnnotationLiteral());
      AnnotatedClass<Order> annotatedItem = new SimpleAnnotatedClass<Order>(Order.class, annotations);
      
      SimpleBeanModel<Order> order = new SimpleBeanModel<Order>(new SimpleAnnotatedClass<Order>(Order.class), annotatedItem, manager);
      assert order.getScopeType().equals(RequestScoped.class);
   }
   
   @Test @SpecAssertion(section="2.4.4")
   public void testScopeMissingInXml()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      AnnotatedClass<SeaBass> annotatedItem = new SimpleAnnotatedClass<SeaBass>(SeaBass.class, annotations);
      
      SimpleBeanModel<SeaBass> trout = new SimpleBeanModel<SeaBass>(new SimpleAnnotatedClass<SeaBass>(SeaBass.class), annotatedItem, manager);
      assert trout.getScopeType().equals(RequestScoped.class);
   }

   @Test @SpecAssertion(section="2.4.4")
   public void testScopeDeclaredInXmlOverridesJava()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(ConversationScoped.class, new ConversationScopedAnnotationLiteral());
      AnnotatedClass<SeaBass> annotatedItem = new SimpleAnnotatedClass<SeaBass>(SeaBass.class, annotations);
      SimpleBeanModel<SeaBass> trout = new SimpleBeanModel<SeaBass>(new SimpleAnnotatedClass<SeaBass>(SeaBass.class), annotatedItem, manager);
      assert trout.getScopeType().equals(ConversationScoped.class);
   }
   
   @Test @SpecAssertion(section="2.4.5")
   public void testDefaultScope()
   {
      SimpleBeanModel<Order> order = new SimpleBeanModel<Order>(new SimpleAnnotatedClass<Order>(Order.class), getEmptyAnnotatedType(Order.class), manager);
      assert order.getScopeType().equals(Dependent.class);
   }
   
   @Test @SpecAssertion(section={"2.4.5", "2.7.2"})
   public void testScopeSpecifiedAndStereotyped()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeAnnotationLiteral());
      AnnotatedClass<SeaBass> annotatedItem = new SimpleAnnotatedClass<SeaBass>(SeaBass.class, annotations);
      SimpleBeanModel<SeaBass> trout = new SimpleBeanModel<SeaBass>(new SimpleAnnotatedClass<SeaBass>(SeaBass.class), annotatedItem, manager);
      assert trout.getScopeType().equals(RequestScoped.class);
   }
   
   @Test @SpecAssertion(section="2.4.5")
   public void testMutipleIncompatibleScopeStereotypes()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeAnnotationLiteral());
      annotations.put(AnimalStereotype.class, new AnimalStereotypeAnnotationLiteral());
      AnnotatedClass<Haddock> annotatedItem = new SimpleAnnotatedClass<Haddock>(Haddock.class, annotations);
      
      boolean exception = false;
      try
      {
         new SimpleBeanModel<Haddock>(new SimpleAnnotatedClass<Haddock>(Haddock.class), annotatedItem, manager);
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
      AnnotatedClass<SeaBass> annotatedItem = new SimpleAnnotatedClass<SeaBass>(SeaBass.class, annotations);
      
      SimpleBeanModel<SeaBass> trout = new SimpleBeanModel<SeaBass>(new SimpleAnnotatedClass<SeaBass>(SeaBass.class), annotatedItem, manager);
      assert trout.getScopeType().equals(RequestScoped.class);     
   }
   
   @Test @SpecAssertion(section="2.4.5")
   public void testMutipleCompatibleScopeStereotypes()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeAnnotationLiteral());
      annotations.put(RiverFishStereotype.class, new RiverFishStereotypeAnnotationLiteral());
      AnnotatedClass<Haddock> annotatedItem = new SimpleAnnotatedClass<Haddock>(Haddock.class, annotations);
      
      SimpleBeanModel<Haddock> haddock = new SimpleBeanModel<Haddock>(new SimpleAnnotatedClass<Haddock>(Haddock.class), annotatedItem, manager);
      assert haddock.getScopeType().equals(ApplicationScoped.class);
   }
   
   @Test @SpecAssertion(section="2.7.2")
   public void testWebBeanScopeTypeOverridesStereotype()
   {
      Bean<RedSnapper> bean = createSimpleWebBean(RedSnapper.class, manager);
      assert bean.getScopeType().equals(RequestScoped.class);
   }
   
}
