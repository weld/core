package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Set;

import javax.webbeans.DeploymentType;
import javax.webbeans.Production;
import javax.webbeans.Stereotype;

import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.impl.SimpleAnnotatedClass;
import org.jboss.webbeans.test.beans.Antelope;
import org.jboss.webbeans.test.beans.Order;
import org.testng.annotations.Test;

public class ClassAnnotatedItemTest
{
   
   @Test
   public void testDeclaredAnnotations()
   {
      AnnotatedClass<Order> annotatedElement = new SimpleAnnotatedClass<Order>(Order.class);
      assert annotatedElement.getAnnotations().size() == 1;
      assert annotatedElement.getAnnotation(Production.class) != null;
      assert annotatedElement.getAnnotatedClass().equals(Order.class);
   }
   
   @Test
   public void testMetaAnnotations()
   {
      AnnotatedClass<Order> annotatedElement = new SimpleAnnotatedClass<Order>(Order.class);
      Set<Annotation> annotations = annotatedElement.getAnnotations(DeploymentType.class);
      assert annotations.size() == 1;
      Iterator<Annotation> it = annotations.iterator();
      Annotation production = it.next();
      assert Production.class.equals(production.annotationType());
   }
   
   @Test
   public void testEmpty()
   {
      AnnotatedClass<Order> annotatedElement = new SimpleAnnotatedClass<Order>(Order.class);
      assert annotatedElement.getAnnotation(Stereotype.class) == null;
      assert annotatedElement.getAnnotations(Stereotype.class).size() == 0;
      AnnotatedClass<Antelope> classWithNoAnnotations = new SimpleAnnotatedClass<Antelope>(Antelope.class);
      assert classWithNoAnnotations.getAnnotations().size() == 0;
   }
   
}
