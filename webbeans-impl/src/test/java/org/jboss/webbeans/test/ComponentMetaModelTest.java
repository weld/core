package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.annotationSetMatches;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.webbeans.ApplicationScoped;
import javax.webbeans.ConversationScoped;
import javax.webbeans.Current;
import javax.webbeans.Dependent;
import javax.webbeans.Named;
import javax.webbeans.Production;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.ComponentMetaModel;
import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.StereotypeMetaModel;
import org.jboss.webbeans.ComponentMetaModel.ComponentType;
import org.jboss.webbeans.bindings.ConversationScopedBinding;
import org.jboss.webbeans.bindings.CurrentBinding;
import org.jboss.webbeans.bindings.DependentBinding;
import org.jboss.webbeans.bindings.NamedBinding;
import org.jboss.webbeans.bindings.RequestScopedBinding;
import org.jboss.webbeans.bindings.StandardBinding;
import org.jboss.webbeans.test.annotations.AnimalStereotype;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.Asynchronous;
import org.jboss.webbeans.test.annotations.FishStereotype;
import org.jboss.webbeans.test.annotations.HornedAnimalDeploymentType;
import org.jboss.webbeans.test.annotations.HornedMammalStereotype;
import org.jboss.webbeans.test.annotations.MammalStereotype;
import org.jboss.webbeans.test.annotations.RequestScopedAnimalStereotype;
import org.jboss.webbeans.test.annotations.RiverFishStereotype;
import org.jboss.webbeans.test.annotations.Synchronous;
import org.jboss.webbeans.test.bindings.AnimalStereotypeBinding;
import org.jboss.webbeans.test.bindings.AnotherDeploymentTypeBinding;
import org.jboss.webbeans.test.bindings.AsynchronousBinding;
import org.jboss.webbeans.test.bindings.FishStereotypeBinding;
import org.jboss.webbeans.test.bindings.HornedAnimalDeploymentTypeBinding;
import org.jboss.webbeans.test.bindings.HornedMamalStereotypeBinding;
import org.jboss.webbeans.test.bindings.RiverFishStereotypeBinding;
import org.jboss.webbeans.test.bindings.SynchronousBinding;
import org.jboss.webbeans.test.components.Antelope;
import org.jboss.webbeans.test.components.Bear;
import org.jboss.webbeans.test.components.Carp;
import org.jboss.webbeans.test.components.Cat;
import org.jboss.webbeans.test.components.Chair;
import org.jboss.webbeans.test.components.Cheetah;
import org.jboss.webbeans.test.components.ComponentWithTooManyDeploymentTypes;
import org.jboss.webbeans.test.components.ComponentWithTooManyScopeTypes;
import org.jboss.webbeans.test.components.Cougar;
import org.jboss.webbeans.test.components.Cow;
import org.jboss.webbeans.test.components.Elephant;
import org.jboss.webbeans.test.components.Giraffe;
import org.jboss.webbeans.test.components.Goldfish;
import org.jboss.webbeans.test.components.Gorilla;
import org.jboss.webbeans.test.components.Haddock;
import org.jboss.webbeans.test.components.Horse;
import org.jboss.webbeans.test.components.Leopard;
import org.jboss.webbeans.test.components.Lion;
import org.jboss.webbeans.test.components.Moose;
import org.jboss.webbeans.test.components.Order;
import org.jboss.webbeans.test.components.Panther;
import org.jboss.webbeans.test.components.Pig;
import org.jboss.webbeans.test.components.Puma;
import org.jboss.webbeans.test.components.SeaBass;
import org.jboss.webbeans.test.components.Tiger;
import org.jboss.webbeans.test.components.Tuna;
import org.jboss.webbeans.test.mock.MockContainerImpl;
import org.jboss.webbeans.util.AnnotatedItem;
import org.jboss.webbeans.util.ClassAnnotatedItem;
import org.jboss.webbeans.util.MutableAnnotatedItem;
import org.junit.Before;
import org.junit.Test;

public class ComponentMetaModelTest
{
   
   private ContainerImpl container;
   
   private AnnotatedItem emptyAnnotatedItem;
   
   @Before
   public void before()
   {
      emptyAnnotatedItem = new MutableAnnotatedItem(null, new HashMap<Class<? extends Annotation>, Annotation>());
      
      List<Annotation> enabledDeploymentTypes = new ArrayList<Annotation>();
      enabledDeploymentTypes.add(new StandardBinding());
      enabledDeploymentTypes.add(new AnotherDeploymentTypeBinding());
      enabledDeploymentTypes.add(new HornedAnimalDeploymentTypeBinding());
      container = new MockContainerImpl(enabledDeploymentTypes);
      
      initStereotypes(container);
   }
   
   private void initStereotypes(ContainerImpl container)
   {
      container.getStereotypeManager().addStereotype(new StereotypeMetaModel(new ClassAnnotatedItem(AnimalStereotype.class)));
      container.getStereotypeManager().addStereotype(new StereotypeMetaModel(new ClassAnnotatedItem(HornedMammalStereotype.class)));
      container.getStereotypeManager().addStereotype(new StereotypeMetaModel(new ClassAnnotatedItem(MammalStereotype.class)));
      container.getStereotypeManager().addStereotype(new StereotypeMetaModel(new ClassAnnotatedItem(FishStereotype.class)));
      container.getStereotypeManager().addStereotype(new StereotypeMetaModel(new ClassAnnotatedItem(RiverFishStereotype.class)));
      container.getStereotypeManager().addStereotype(new StereotypeMetaModel(new ClassAnnotatedItem(RequestScopedAnimalStereotype.class)));
   }
   
   @Test
   public void testTooManyDeploymentTypes()
   {
      boolean exception = false;
      try
      {
         new ComponentMetaModel<ComponentWithTooManyDeploymentTypes>(new ClassAnnotatedItem(ComponentWithTooManyDeploymentTypes.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   // **** TESTS FOR DEPLOYMENT TYPE **** //
   
   @Test
   public void testXmlDeploymentTypeOverridesJava()
   {
      Map<Class<? extends Annotation>, Annotation> xmlDefinedDeploymentTypeAnnotations = new HashMap<Class<? extends Annotation>, Annotation>();
      xmlDefinedDeploymentTypeAnnotations.put(AnotherDeploymentType.class, new AnotherDeploymentTypeBinding());
      AnnotatedItem xmlDefinedDeploymentTypeAnnotatedItem = new MutableAnnotatedItem(ComponentWithTooManyDeploymentTypes.class, xmlDefinedDeploymentTypeAnnotations);
      
      ComponentMetaModel<ComponentWithTooManyDeploymentTypes> component = new ComponentMetaModel<ComponentWithTooManyDeploymentTypes>(new ClassAnnotatedItem(ComponentWithTooManyDeploymentTypes.class), xmlDefinedDeploymentTypeAnnotatedItem, container);
      assert component.getDeploymentType().annotationType().equals(AnotherDeploymentType.class);
   }
   
   @Test
   public void testXmlDefaultDeploymentType()
   {
      AnnotatedItem antelopeAnnotatedItem = new MutableAnnotatedItem(Antelope.class, new HashMap<Class<? extends Annotation>, Annotation>());
      ComponentMetaModel<Antelope> antelope = new ComponentMetaModel<Antelope>(emptyAnnotatedItem, antelopeAnnotatedItem, container);
      assert antelope.getDeploymentType().annotationType().equals(Production.class);
   }
   
   @Test
   public void testXmlIgnoresJavaDeploymentType()
   {
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(Tuna.class, new HashMap<Class<? extends Annotation>, Annotation>());
      ComponentMetaModel<Tuna> tuna = new ComponentMetaModel<Tuna>(new ClassAnnotatedItem(Tuna.class), annotatedItem, container);
      assert tuna.getDeploymentType().annotationType().equals(Production.class);
   }
   
   @Test
   public void testDeploymentTypePrecedenceSelection()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(HornedMammalStereotype.class, new HornedMamalStereotypeBinding());
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(Moose.class, annotations);
      
      ComponentMetaModel<Moose> moose = new ComponentMetaModel<Moose>(new ClassAnnotatedItem(Moose.class), annotatedItem, container);
      assert moose.getDeploymentType().annotationType().equals(HornedAnimalDeploymentType.class);
      
   }
   
   // **** TESTS FOR BINDING TYPE **** //
   
   @SuppressWarnings("unchecked")
   @Test
   public void testXmlBindingTypeOverridesJava()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Asynchronous.class, new AsynchronousBinding());
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(Cat.class, annotations);
      
      ComponentMetaModel<Cat> cat = new ComponentMetaModel<Cat>(new ClassAnnotatedItem(Cat.class), annotatedItem, container);
      assert cat.getBindingTypes().size() == 1;
      assert annotationSetMatches(cat.getBindingTypes(), Asynchronous.class);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testBindingTypesDeclaredInJava()
   {
      ComponentMetaModel<Cat> cat = new ComponentMetaModel<Cat>(new ClassAnnotatedItem(Cat.class), emptyAnnotatedItem, container);
      assert cat.getBindingTypes().size() == 1;
      assert annotationSetMatches(cat.getBindingTypes(), Synchronous.class);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testBindingTypesDeclaredInXml()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Asynchronous.class, new AsynchronousBinding());
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(Antelope.class, annotations);
      
      ComponentMetaModel<Antelope> antelope = new ComponentMetaModel<Antelope>(emptyAnnotatedItem, annotatedItem, container);
      assert annotationSetMatches(antelope.getBindingTypes(), Asynchronous.class);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testDefaultBindingType()
   {
      ComponentMetaModel<Order> order = new ComponentMetaModel<Order>(new ClassAnnotatedItem(Order.class), emptyAnnotatedItem, container);
      assert order.getBindingTypes().size() == 1;
      order.getBindingTypes().iterator().next().annotationType().equals(Current.class);
   }
   
   // **** TESTS FOR SCOPES **** //
   
   @Test
   public void testScopeDeclaredInJava()
   {
      ComponentMetaModel<SeaBass> trout = new ComponentMetaModel<SeaBass>(new ClassAnnotatedItem(SeaBass.class), emptyAnnotatedItem, container);
      assert trout.getScopeType().annotationType().equals(RequestScoped.class);
   }
   
   @Test
   public void testScopeDeclaredInXml()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(RequestScoped.class, new RequestScopedBinding());
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(Order.class, annotations);
      
      ComponentMetaModel<Order> order = new ComponentMetaModel<Order>(new ClassAnnotatedItem(Order.class), annotatedItem, container);
      assert order.getScopeType().annotationType().equals(RequestScoped.class);
   }
   
   @Test
   public void testScopeDeclaredInXmlOverridesJava()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(ConversationScoped.class, new ConversationScopedBinding());
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(SeaBass.class, annotations);
      ComponentMetaModel<SeaBass> trout = new ComponentMetaModel<SeaBass>(new ClassAnnotatedItem(SeaBass.class), annotatedItem, container);
      assert trout.getScopeType().annotationType().equals(ConversationScoped.class);
   }
   
   @Test
   public void testScopeMissingInXml()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(SeaBass.class, annotations);
      
      ComponentMetaModel<SeaBass> trout = new ComponentMetaModel<SeaBass>(new ClassAnnotatedItem(SeaBass.class), annotatedItem, container);
      assert trout.getScopeType().annotationType().equals(RequestScoped.class);
   }
   
   @Test
   public void testNoScopeSpecified()
   {
      ComponentMetaModel<Order> order = new ComponentMetaModel<Order>(new ClassAnnotatedItem(Order.class), emptyAnnotatedItem, container);
      assert order.getScopeType().annotationType().equals(Dependent.class);
   }
   
   @Test
   public void testTooManyScopesSpecified()
   {
      boolean exception = false;
      try
      {
         new ComponentMetaModel<ComponentWithTooManyScopeTypes>(new ClassAnnotatedItem(ComponentWithTooManyScopeTypes.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;  
   }
   
   @Test
   public void testTooManyScopesSpecifiedInXml()
   {
      boolean exception = false;
      try
      {
         Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
         annotations.put(RequestScoped.class, new RequestScopedBinding());
         annotations.put(ConversationScoped.class, new ConversationScopedBinding());
         AnnotatedItem antelopeAnnotatedItem = new MutableAnnotatedItem(Antelope.class, annotations);
         new ComponentMetaModel<Antelope>(emptyAnnotatedItem, antelopeAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;  
   }
   
   @Test
   public void testScopeSpecifiedAndStereotyped()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeBinding());
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(SeaBass.class, annotations);
      ComponentMetaModel<SeaBass> trout = new ComponentMetaModel<SeaBass>(new ClassAnnotatedItem(SeaBass.class), annotatedItem, container);
      assert trout.getScopeType().annotationType().equals(RequestScoped.class);
   }
   
   @Test
   public void testMutipleIncompatibleScopeStereotypes()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeBinding());
      annotations.put(AnimalStereotype.class, new AnimalStereotypeBinding());
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(Haddock.class, annotations);
      
      boolean exception = false;
      try
      {
         new ComponentMetaModel<Haddock>(new ClassAnnotatedItem(Haddock.class), annotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   @Test
   public void testMutipleIncompatibleScopeStereotypesWithScopeSpecified()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeBinding());
      annotations.put(AnimalStereotype.class, new AnimalStereotypeBinding());
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(SeaBass.class, annotations);
      
      ComponentMetaModel<SeaBass> trout = new ComponentMetaModel<SeaBass>(new ClassAnnotatedItem(SeaBass.class), annotatedItem, container);
      assert trout.getScopeType().annotationType().equals(RequestScoped.class);     
   }
   
   @Test
   public void testMutipleCompatibleScopeStereotypes()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeBinding());
      annotations.put(RiverFishStereotype.class, new RiverFishStereotypeBinding());
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(Haddock.class, annotations);
      
      ComponentMetaModel<Haddock> haddock = new ComponentMetaModel<Haddock>(new ClassAnnotatedItem(Haddock.class), annotatedItem, container);
      assert haddock.getScopeType().annotationType().equals(ApplicationScoped.class);
   }
   
   // **** TESTS FOR COMPONENT NAME **** /
   
   @Test
   public void testNamed()
   {
      ComponentMetaModel<Haddock> haddock = new ComponentMetaModel<Haddock>(new ClassAnnotatedItem(Haddock.class), emptyAnnotatedItem, container);
      assert haddock.getName() != null;
      haddock.getComponentType().equals(ComponentType.SIMPLE);
      assert haddock.getName().equals("haddock");
   }
   
   @Test
   public void testXmlNamed()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Named.class, new NamedBinding()
      {

         public String value()
         {
            return "";
         }
      });
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(SeaBass.class, annotations);
      ComponentMetaModel<SeaBass> trout = new ComponentMetaModel<SeaBass>(new ClassAnnotatedItem(SeaBass.class), annotatedItem, container);
      
      assert trout.getName() != null;
      trout.getComponentType().equals(ComponentType.SIMPLE);
      assert trout.getName().equals("seaBass");
   }
   
   @Test
   public void testNonDefaultXmlNamed()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Named.class, new NamedBinding()
      {

         public String value()
         {
            return "aTrout";
         }
      });
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(SeaBass.class, annotations);
      ComponentMetaModel<SeaBass> trout = new ComponentMetaModel<SeaBass>(new ClassAnnotatedItem(SeaBass.class), annotatedItem, container);
      
      assert trout.getName().equals("aTrout");
   }
   
   @Test
   public void testNotNamed()
   {
      ComponentMetaModel<SeaBass> trout = new ComponentMetaModel<SeaBass>(new ClassAnnotatedItem(SeaBass.class), emptyAnnotatedItem, container);
      assert trout.getName() == null;
   }
   
   @Test
   public void testNonDefaultNamed()
   {
      ComponentMetaModel<Moose> moose = new ComponentMetaModel<Moose>(new ClassAnnotatedItem(Moose.class), emptyAnnotatedItem, container);
      assert moose.getName().equals("aMoose");
   }
   
   
   // **** TESTS FOR STEREOTYPES **** //
   
   @SuppressWarnings("unchecked")
   @Test
   public void testStereotypeDeclaredInXmlAndJava()
   {
      Map<Class<? extends Annotation>, Annotation> orderXmlAnnotations = new HashMap<Class<? extends Annotation>, Annotation>();
      orderXmlAnnotations.put(Current.class, new CurrentBinding());
      orderXmlAnnotations.put(Synchronous.class, new SynchronousBinding());
      orderXmlAnnotations.put(Named.class, new NamedBinding()
      {
         public String value()
         {
            return "currentSynchronousOrder";
         }
      });
      AnnotatedItem currentSynchronousOrderAnnotatedItem = new MutableAnnotatedItem(Order.class, orderXmlAnnotations);
      
      ComponentMetaModel<Order> order = new ComponentMetaModel<Order>(new ClassAnnotatedItem(Order.class), currentSynchronousOrderAnnotatedItem, container);
      assert Production.class.equals(order.getDeploymentType().annotationType());
      assert "currentSynchronousOrder".equals(order.getName());
      assert order.getBindingTypes().size() == 2;
      assert annotationSetMatches(order.getBindingTypes(), Current.class, Synchronous.class);
      assert order.getScopeType().annotationType().equals(Dependent.class);
   }
   
   @Test
   public void testSingleStereotype()
   {
      ComponentMetaModel<Gorilla> gorilla = new ComponentMetaModel<Gorilla>(new ClassAnnotatedItem(Gorilla.class), emptyAnnotatedItem, container);
      assert gorilla.getName() == null;
      assert gorilla.getDeploymentType().annotationType().equals(Production.class);
      assert gorilla.getBindingTypes().iterator().next().annotationType().equals(Current.class);
      assert gorilla.getScopeType().annotationType().equals(RequestScoped.class);
   }
   
   @Test
   public void testRequiredTypeIsImplemented()
   {
      try
      {
         new ComponentMetaModel<Gorilla>(new ClassAnnotatedItem(Gorilla.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         assert false;
      }
      
   }
   
   @Test
   public void testRequiredTypeIsNotImplemented()
   {
      boolean exception = false;
      try
      {
         new ComponentMetaModel<Chair>(new ClassAnnotatedItem(Chair.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
      
   }
   
   @Test
   public void testScopeIsSupported()
   {
      try
      {
         new ComponentMetaModel<Goldfish>(new ClassAnnotatedItem(Goldfish.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         e.printStackTrace();
         assert false;
      }
      
   }
   
   @Test
   public void testScopeIsNotSupported()
   {
      boolean exception = false;
      try
      {
         new ComponentMetaModel<Carp>(new ClassAnnotatedItem(Carp.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
      
   }
   
   //*** COMPONENT CLASS CHECKS ****//
   
   @Test
   public void testAbstractClassIsNotAllowed()
   {
      boolean exception = false;
      try
      {
         new ComponentMetaModel<Cow>(new ClassAnnotatedItem(Cow.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   @Test
   public void testFinalClassMustBeDependentScoped()
   {
      boolean exception = false;
      try
      {
         new ComponentMetaModel<Horse>(new ClassAnnotatedItem(Horse.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
      
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Dependent.class, new DependentBinding());
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(Horse.class, annotations);
      try
      {
         new ComponentMetaModel<Horse>(new ClassAnnotatedItem(Horse.class), annotatedItem, container);
      }
      catch (Exception e) 
      {
         assert false;
      }
   }
   
   @Test
   public void testClassWithFinalMethodMustBeDependentScoped()
   {
      boolean exception = false;
      try
      {
         new ComponentMetaModel<Pig>(new ClassAnnotatedItem(Pig.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
      
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Dependent.class, new DependentBinding());
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(Pig.class, annotations);
      try
      {
         new ComponentMetaModel<Pig>(new ClassAnnotatedItem(Pig.class), annotatedItem, container);
      }
      catch (Exception e) 
      {
         assert false;
      }
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testStateless()
   {
      ComponentMetaModel<Lion> lion = new ComponentMetaModel<Lion>(new ClassAnnotatedItem(Lion.class), emptyAnnotatedItem, container);
      assert lion.getComponentType().equals(ComponentType.ENTERPRISE);
      assert lion.getScopeType().annotationType().equals(Dependent.class);
      annotationSetMatches(lion.getBindingTypes(), Current.class);
      assert lion.getName().equals("lion");
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testStatelessDefinedInXml()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(Giraffe.class, annotations);
      
      ComponentMetaModel<Giraffe> giraffe = new ComponentMetaModel<Giraffe>(new ClassAnnotatedItem(Giraffe.class), annotatedItem, container);
      assert giraffe.getComponentType().equals(ComponentType.ENTERPRISE);
      assert giraffe.getScopeType().annotationType().equals(Dependent.class);
      annotationSetMatches(giraffe.getBindingTypes(), Current.class);
   }
   
   @Test
   public void testStatelessWithRequestScope()
   {
      boolean exception = false;
      try
      {
         new ComponentMetaModel<Bear>(new ClassAnnotatedItem(Bear.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   // TODO Need EJB3.1 @Test
   public void testSingleton()
   {
      //ComponentMetaModel<Lion> lion = new ComponentMetaModel<Lion>(new ClassAnnotatedItem(Lion.class), emptyAnnotatedItem, container);
      //assert lion.getComponentType().equals(ComponentType.ENTERPRISE);
      //assert lion.getScopeType().annotationType().equals(ApplicationScoped.class);
   }
   
   // TODO Need EJB3.1 @Test
   public void testSingletonWithRequestScope()
   {
      //ComponentMetaModel<Lion> lion = new ComponentMetaModel<Lion>(new ClassAnnotatedItem(Lion.class), emptyAnnotatedItem, container);
      //assert lion.getComponentType().equals(ComponentType.ENTERPRISE);
      //assert lion.getScopeType().annotationType().equals(ApplicationScoped.class);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testStateful()
   {

      ComponentMetaModel<Tiger> tiger = new ComponentMetaModel<Tiger>(new ClassAnnotatedItem(Tiger.class), emptyAnnotatedItem, container);
      assert tiger.getComponentType().equals(ComponentType.ENTERPRISE);
      annotationSetMatches(tiger.getBindingTypes(), Synchronous.class);
      assert tiger.getRemoveMethod().getMethod().getName().equals("remove");
      assert tiger.getName() == null;
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testMultipleRemoveMethodsWithDestroys()
   {

      ComponentMetaModel<Elephant> elephant = new ComponentMetaModel<Elephant>(new ClassAnnotatedItem(Elephant.class), emptyAnnotatedItem, container);
      assert elephant.getComponentType().equals(ComponentType.ENTERPRISE);
      assert elephant.getRemoveMethod().getMethod().getName().equals("remove2");
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testMultipleRemoveMethodsWithoutDestroys()
   {
      boolean exception = false;
      try
      {
         new ComponentMetaModel<Puma>(new ClassAnnotatedItem(Puma.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testMultipleRemoveMethodsWithMultipleDestroys()
   {
      boolean exception = false;
      try
      {
         new ComponentMetaModel<Cougar>(new ClassAnnotatedItem(Cougar.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testNonStatefulEnterpriseComponentWithDestroys()
   {
      boolean exception = false;
      try
      {
         new ComponentMetaModel<Cheetah>(new ClassAnnotatedItem(Cheetah.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   @Test
   public void testRemoveMethodWithDefaultBinding()
   {

      ComponentMetaModel<Panther> panther = new ComponentMetaModel<Panther>(new ClassAnnotatedItem(Panther.class), emptyAnnotatedItem, container);
      assert panther.getComponentType().equals(ComponentType.ENTERPRISE);
      assert panther.getRemoveMethod().getMethod().getName().equals("remove");
      assert panther.getRemoveMethod().getParameters().size() == 1;
      assert panther.getRemoveMethod().getParameters().get(0).getType().equals(String.class);
      assert panther.getRemoveMethod().getParameters().get(0).getBindingTypes().length == 1;
      assert panther.getRemoveMethod().getParameters().get(0).getBindingTypes()[0].annotationType().equals(Current.class);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testMessageDriven()
   {
      ComponentMetaModel<Leopard> leopard = new ComponentMetaModel<Leopard>(new ClassAnnotatedItem(Leopard.class), emptyAnnotatedItem, container);
      assert leopard.getComponentType().equals(ComponentType.ENTERPRISE);
      annotationSetMatches(leopard.getBindingTypes(), Current.class);
   }
   
   
}
