package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.annotationSetMatches;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.webbeans.ApplicationScoped;
import javax.webbeans.ComponentInstance;
import javax.webbeans.ConversationScoped;
import javax.webbeans.Current;
import javax.webbeans.Dependent;
import javax.webbeans.Named;
import javax.webbeans.Production;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.ComponentInstanceImpl;
import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.StereotypeMetaModel;
import org.jboss.webbeans.bindings.ConversationScopedBinding;
import org.jboss.webbeans.bindings.CurrentBinding;
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
import org.jboss.webbeans.test.components.Carp;
import org.jboss.webbeans.test.components.Cat;
import org.jboss.webbeans.test.components.Chair;
import org.jboss.webbeans.test.components.ComponentWithTooManyDeploymentTypes;
import org.jboss.webbeans.test.components.ComponentWithTooManyScopeTypes;
import org.jboss.webbeans.test.components.Goldfish;
import org.jboss.webbeans.test.components.Gorilla;
import org.jboss.webbeans.test.components.Haddock;
import org.jboss.webbeans.test.components.Moose;
import org.jboss.webbeans.test.components.Order;
import org.jboss.webbeans.test.components.Trout;
import org.jboss.webbeans.test.components.Tuna;
import org.jboss.webbeans.test.mock.MockContainerImpl;
import org.jboss.webbeans.util.AnnotatedItem;
import org.jboss.webbeans.util.ClassAnnotatedItem;
import org.jboss.webbeans.util.MutableAnnotatedItem;
import org.junit.Before;
import org.junit.Test;

public class ComponentInstanceTest
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
         new ComponentInstanceImpl<ComponentWithTooManyDeploymentTypes>(new ClassAnnotatedItem(ComponentWithTooManyDeploymentTypes.class), emptyAnnotatedItem, container);
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
      
      ComponentInstance<ComponentWithTooManyDeploymentTypes> component = new ComponentInstanceImpl<ComponentWithTooManyDeploymentTypes>(new ClassAnnotatedItem(ComponentWithTooManyDeploymentTypes.class), xmlDefinedDeploymentTypeAnnotatedItem, container);
      assert component.getDeploymentType().annotationType().equals(AnotherDeploymentType.class);
   }
   
   @Test
   public void testXmlDefaultDeploymentType()
   {
      AnnotatedItem antelopeAnnotatedItem = new MutableAnnotatedItem(Antelope.class, new HashMap<Class<? extends Annotation>, Annotation>());
      ComponentInstance<Antelope> antelope = new ComponentInstanceImpl<Antelope>(emptyAnnotatedItem, antelopeAnnotatedItem, container);
      assert antelope.getDeploymentType().annotationType().equals(Production.class);
   }
   
   @Test
   public void testXmlIgnoresJavaDeploymentType()
   {
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(Tuna.class, new HashMap<Class<? extends Annotation>, Annotation>());
      ComponentInstance<Tuna> tuna = new ComponentInstanceImpl<Tuna>(new ClassAnnotatedItem(Tuna.class), annotatedItem, container);
      assert tuna.getDeploymentType().annotationType().equals(Production.class);
   }
   
   @Test
   public void testDeploymentTypePrecedenceSelection()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(HornedMammalStereotype.class, new HornedMamalStereotypeBinding());
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(Moose.class, annotations);
      
      ComponentInstance<Moose> moose = new ComponentInstanceImpl<Moose>(new ClassAnnotatedItem(Moose.class), annotatedItem, container);
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
      
      ComponentInstance<Cat> cat = new ComponentInstanceImpl<Cat>(new ClassAnnotatedItem(Cat.class), annotatedItem, container);
      assert cat.getBindingTypes().size() == 1;
      assert annotationSetMatches(cat.getBindingTypes(), Asynchronous.class);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testBindingTypesDeclaredInJava()
   {
      ComponentInstance<Cat> cat = new ComponentInstanceImpl<Cat>(new ClassAnnotatedItem(Cat.class), emptyAnnotatedItem, container);
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
      
      ComponentInstance<Antelope> antelope = new ComponentInstanceImpl<Antelope>(emptyAnnotatedItem, annotatedItem, container);
      assert annotationSetMatches(antelope.getBindingTypes(), Asynchronous.class);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testDefaultBindingType()
   {
      ComponentInstance<Order> order = new ComponentInstanceImpl<Order>(new ClassAnnotatedItem(Order.class), emptyAnnotatedItem, container);
      assert order.getBindingTypes().size() == 1;
      order.getBindingTypes().iterator().next().annotationType().equals(Current.class);
   }
   
   // **** TESTS FOR SCOPES **** //
   
   @Test
   public void testScopeDeclaredInJava()
   {
      ComponentInstance<Trout> trout = new ComponentInstanceImpl<Trout>(new ClassAnnotatedItem(Trout.class), emptyAnnotatedItem, container);
      assert trout.getScopeType().annotationType().equals(RequestScoped.class);
   }
   
   @Test
   public void testScopeDeclaredInXml()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(RequestScoped.class, new RequestScopedBinding());
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(Order.class, annotations);
      
      ComponentInstance<Order> order = new ComponentInstanceImpl<Order>(new ClassAnnotatedItem(Order.class), annotatedItem, container);
      assert order.getScopeType().annotationType().equals(RequestScoped.class);
   }
   
   @Test
   public void testScopeDeclaredInXmlOverridesJava()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(ConversationScoped.class, new ConversationScopedBinding());
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(Trout.class, annotations);
      ComponentInstance<Trout> trout = new ComponentInstanceImpl<Trout>(new ClassAnnotatedItem(Trout.class), annotatedItem, container);
      assert trout.getScopeType().annotationType().equals(ConversationScoped.class);
   }
   
   @Test
   public void testScopeMissingInXml()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(Trout.class, annotations);
      
      ComponentInstance<Trout> trout = new ComponentInstanceImpl<Trout>(new ClassAnnotatedItem(Trout.class), annotatedItem, container);
      assert trout.getScopeType().annotationType().equals(RequestScoped.class);
   }
   
   @Test
   public void testNoScopeSpecified()
   {
      ComponentInstance<Order> order = new ComponentInstanceImpl<Order>(new ClassAnnotatedItem(Order.class), emptyAnnotatedItem, container);
      assert order.getScopeType().annotationType().equals(Dependent.class);
   }
   
   @Test
   public void testTooManyScopesSpecified()
   {
      boolean exception = false;
      try
      {
         new ComponentInstanceImpl<ComponentWithTooManyScopeTypes>(new ClassAnnotatedItem(ComponentWithTooManyScopeTypes.class), emptyAnnotatedItem, container);
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
         new ComponentInstanceImpl<Antelope>(emptyAnnotatedItem, antelopeAnnotatedItem, container);
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
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(Trout.class, annotations);
      ComponentInstance<Trout> trout = new ComponentInstanceImpl<Trout>(new ClassAnnotatedItem(Trout.class), annotatedItem, container);
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
         new ComponentInstanceImpl<Haddock>(new ClassAnnotatedItem(Haddock.class), annotatedItem, container);
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
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(Trout.class, annotations);
      
      ComponentInstance<Trout> trout = new ComponentInstanceImpl<Trout>(new ClassAnnotatedItem(Trout.class), annotatedItem, container);
      assert trout.getScopeType().annotationType().equals(RequestScoped.class);     
   }
   
   @Test
   public void testMutipleCompatibleScopeStereotypes()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeBinding());
      annotations.put(RiverFishStereotype.class, new RiverFishStereotypeBinding());
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(Haddock.class, annotations);
      
      ComponentInstance<Haddock> haddock = new ComponentInstanceImpl<Haddock>(new ClassAnnotatedItem(Haddock.class), annotatedItem, container);
      assert haddock.getScopeType().annotationType().equals(ApplicationScoped.class);
   }
   
   // **** TESTS FOR COMPONENT NAME **** /
   
   @Test
   public void testNamed()
   {
      ComponentInstance<Haddock> haddock = new ComponentInstanceImpl<Haddock>(new ClassAnnotatedItem(Haddock.class), emptyAnnotatedItem, container);
      assert haddock.getName() != null;
      // TODO Test naming algorithms
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
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(Trout.class, annotations);
      ComponentInstance<Trout> trout = new ComponentInstanceImpl<Trout>(new ClassAnnotatedItem(Trout.class), annotatedItem, container);
      
      assert trout.getName() != null;
      // TODO Test naming algorithms
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
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(Trout.class, annotations);
      ComponentInstance<Trout> trout = new ComponentInstanceImpl<Trout>(new ClassAnnotatedItem(Trout.class), annotatedItem, container);
      
      assert trout.getName().equals("aTrout");
   }
   
   @Test
   public void testNotNamed()
   {
      ComponentInstance<Trout> trout = new ComponentInstanceImpl<Trout>(new ClassAnnotatedItem(Trout.class), emptyAnnotatedItem, container);
      assert trout.getName() == null;
   }
   
   @Test
   public void testNonDefaultNamed()
   {
      ComponentInstance<Moose> moose = new ComponentInstanceImpl<Moose>(new ClassAnnotatedItem(Moose.class), emptyAnnotatedItem, container);
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
      
      ComponentInstance<Order> order = new ComponentInstanceImpl<Order>(new ClassAnnotatedItem(Order.class), currentSynchronousOrderAnnotatedItem, container);
      assert Production.class.equals(order.getDeploymentType().annotationType());
      assert "currentSynchronousOrder".equals(order.getName());
      assert order.getBindingTypes().size() == 2;
      assert annotationSetMatches(order.getBindingTypes(), Current.class, Synchronous.class);
      assert order.getScopeType().annotationType().equals(Dependent.class);
   }
   
   @Test
   public void testSingleStereotype()
   {
      ComponentInstance<Gorilla> gorilla = new ComponentInstanceImpl<Gorilla>(new ClassAnnotatedItem(Gorilla.class), emptyAnnotatedItem, container);
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
         new ComponentInstanceImpl<Gorilla>(new ClassAnnotatedItem(Gorilla.class), emptyAnnotatedItem, container);
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
         new ComponentInstanceImpl<Chair>(new ClassAnnotatedItem(Chair.class), emptyAnnotatedItem, container);
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
         new ComponentInstanceImpl<Goldfish>(new ClassAnnotatedItem(Goldfish.class), emptyAnnotatedItem, container);
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
         new ComponentInstanceImpl<Carp>(new ClassAnnotatedItem(Carp.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
      
   }
}
