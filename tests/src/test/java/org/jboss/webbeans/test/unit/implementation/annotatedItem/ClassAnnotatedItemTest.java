package org.jboss.webbeans.test.unit.implementation.annotatedItem;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.inject.deployment.DeploymentType;
import javax.enterprise.inject.deployment.Production;
import javax.enterprise.inject.stereotype.Stereotype;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.introspector.WBClass;
import org.jboss.webbeans.introspector.jlr.WBClassImpl;
import org.jboss.webbeans.metadata.TypeStore;
import org.jboss.webbeans.resources.ClassTransformer;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class ClassAnnotatedItemTest extends AbstractWebBeansTest
{
	
   private final ClassTransformer transformer = new ClassTransformer(new TypeStore());
   
   @Test
   public void testDeclaredAnnotations()
   {
      WBClass<Order> annotatedElement = WBClassImpl.of(Order.class, transformer);
      assert annotatedElement.getAnnotations().size() == 1;
      assert annotatedElement.getAnnotation(Production.class) != null;
      assert annotatedElement.getJavaClass().equals(Order.class);
   }
   
   @Test
   public void testMetaAnnotations()
   {
      WBClass<Order> annotatedElement = WBClassImpl.of(Order.class, transformer);
      Set<Annotation> annotations = annotatedElement.getMetaAnnotations(DeploymentType.class);
      assert annotations.size() == 1;
      Iterator<Annotation> it = annotations.iterator();
      Annotation production = it.next();
      assert Production.class.equals(production.annotationType());
   }
   
   @Test
   public void testEmpty()
   {
      WBClass<Order> annotatedElement = WBClassImpl.of(Order.class, transformer);
      assert annotatedElement.getAnnotation(Stereotype.class) == null;
      assert annotatedElement.getMetaAnnotations(Stereotype.class).size() == 0;
      WBClass<Antelope> classWithNoAnnotations = WBClassImpl.of(Antelope.class, transformer);
      assert classWithNoAnnotations.getAnnotations().size() == 0;
   }
   
}
