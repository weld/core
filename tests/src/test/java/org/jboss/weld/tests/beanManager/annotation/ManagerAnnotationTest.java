package org.jboss.weld.tests.beanManager.annotation;

import javax.persistence.PersistenceContext;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class ManagerAnnotationTest extends AbstractWeldTest
{
   
   @Test(description="WELD-299")
   public void testIsQualifier() throws Exception
   {
      assert !getCurrentManager().isQualifier(PersistenceContext.class);
   }
   
   @Test(description="WELD-299")
   public void testIsInterceptorBinding() throws Exception
   {
      assert !getCurrentManager().isInterceptorBinding(PersistenceContext.class);
   }
   
   @Test(description="WELD-299")
   public void testIsNormalScope() throws Exception
   {
      assert !getCurrentManager().isNormalScope(PersistenceContext.class);
   }
   
   @Test(description="WELD-299")
   public void testIsPassivatingScope() throws Exception
   {
      assert !getCurrentManager().isPassivatingScope(PersistenceContext.class);
   }
   
   @Test(description="WELD-299")
   public void testIsScope() throws Exception
   {
      assert !getCurrentManager().isScope(PersistenceContext.class);
   }
   
   @Test(description="WELD-299")
   public void testIsStereotype() throws Exception
   {
      assert !getCurrentManager().isStereotype(PersistenceContext.class);
   }
   
   
}
