package org.jboss.weld.tests.beanManager.annotation;

import javax.persistence.PersistenceContext;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class ManagerAnnotationTest extends AbstractWeldTest
{
   
   @Test
   public void testIsQualifier() throws Exception
   {
      assert !getCurrentManager().isQualifier(PersistenceContext.class);
   }
   
   @Test
   public void testIsInterceptorBinding() throws Exception
   {
      assert !getCurrentManager().isInterceptorBinding(PersistenceContext.class);
   }
   
   @Test
   public void testIsNormalScope() throws Exception
   {
      assert !getCurrentManager().isNormalScope(PersistenceContext.class);
   }
   
   @Test
   public void testIsPassivatingScope() throws Exception
   {
      assert !getCurrentManager().isPassivatingScope(PersistenceContext.class);
   }
   
   @Test
   public void testIsScope() throws Exception
   {
      assert !getCurrentManager().isScope(PersistenceContext.class);
   }
   
   @Test
   public void testIsStereotype() throws Exception
   {
      assert !getCurrentManager().isStereotype(PersistenceContext.class);
   }
   
   
}
