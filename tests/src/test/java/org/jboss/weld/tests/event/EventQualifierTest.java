package org.jboss.weld.tests.event;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class EventQualifierTest extends AbstractWeldTest
{
   
   @Test(description="WELD-226")
   public void testDefaultQualifierNotRequired()
   {
      Bar bar = getReference(Bar.class);
      bar.fireWithNoQualifiers();
      assert bar.isUnqualifiedObserved();
      assert !bar.isUpdatedObserved();
      bar.reset();
      bar.fireWithNoQualifiersViaManager();
      assert bar.isUnqualifiedObserved();
      assert !bar.isUpdatedObserved();
      bar.reset();
      bar.fireWithUpdatedQualifierViaAnnotation();
      assert bar.isUnqualifiedObserved();
      assert bar.isUpdatedObserved();
      bar.reset();
      bar.fireWithUpdatedQualifierViaManager();
      assert bar.isUpdatedObserved();
      assert bar.isUnqualifiedObserved();
      bar.reset();
      bar.fireWithUpdatedQualifierViaSelect();
      assert bar.isUnqualifiedObserved();
      assert bar.isUpdatedObserved();
   }

}
