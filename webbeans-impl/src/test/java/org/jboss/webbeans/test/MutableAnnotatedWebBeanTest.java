package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Set;

import javax.webbeans.BindingType;
import javax.webbeans.Current;
import javax.webbeans.DeploymentType;
import javax.webbeans.Named;
import javax.webbeans.Production;
import javax.webbeans.ScopeType;
import javax.webbeans.Stereotype;

import org.jboss.webbeans.CurrentBinding;
import org.jboss.webbeans.test.components.ClassWithNoAnnotations;
import org.jboss.webbeans.test.components.Order;
import org.jboss.webbeans.util.AnnotatedWebBean;
import org.jboss.webbeans.util.MutableAnnotatedWebBean;
import org.junit.Test;

public class MutableAnnotatedWebBeanTest
{
   
   @Test
   public void testDeclaredAnnotations()
   {
      AnnotatedWebBean annotatedElement = new MutableAnnotatedWebBean(Order.class);
      assert annotatedElement.getAnnotations().size() == 2;
      assert annotatedElement.getAnnotation(Production.class) != null;
      assert annotatedElement.getAnnotation(Named.class) != null;
      System.out.println(annotatedElement.getAnnotatedClass());
      assert annotatedElement.getAnnotatedClass().equals(Order.class);
   }
   
   @Test
   public void testMutability()
   {
      MutableAnnotatedWebBean annotatedElement = new MutableAnnotatedWebBean(Order.class);
      assert annotatedElement.getAnnotations().size() == 2;
      annotatedElement.add(new CurrentBinding());
      assert annotatedElement.getAnnotations().size() == 3;
      assert annotatedElement.getAnnotation(Production.class) != null;
      assert annotatedElement.getAnnotation(Named.class) != null;
      assert annotatedElement.getAnnotation(Current.class) != null;
   }
   
   @Test
   public void testMetaAnnotations()
   {
      AnnotatedWebBean annotatedElement = new MutableAnnotatedWebBean(Order.class);
      Set<Annotation> annotations = annotatedElement.getAnnotations(DeploymentType.class);
      assert annotations.size() == 1;
      Iterator<Annotation> it = annotations.iterator();
      Annotation production = it.next();
      assert Production.class.equals(production.annotationType());
   }
   
   @Test
   public void testMutableMetaAnnotations()
   {
      MutableAnnotatedWebBean annotatedElement = new MutableAnnotatedWebBean(Order.class);
      annotatedElement.add(new CurrentBinding());
      Set<Annotation> annotations = annotatedElement.getAnnotations(BindingType.class);
      assert annotations.size() == 1;
      Iterator<Annotation> it = annotations.iterator();
      Annotation production = it.next();
      assert Current.class.equals(production.annotationType());
   }
   
   @Test
   public void testEmpty()
   {
      AnnotatedWebBean annotatedElement = new MutableAnnotatedWebBean(Order.class);
      assert annotatedElement.getAnnotation(Stereotype.class) == null;
      assert annotatedElement.getAnnotations(Stereotype.class).size() == 0;
      AnnotatedWebBean classWithNoAnnotations = new MutableAnnotatedWebBean(ClassWithNoAnnotations.class);
      assert classWithNoAnnotations.getAnnotations().size() == 0;
   }
   
}
