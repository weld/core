package org.jboss.weld.tests.serialization;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class SerializationTest extends AbstractWeldTest
{

   @Test(description="WELD-363")
   public void testConversationManagerSerializable()
      throws Exception
   {
      TestConversationManager cMgr = getCurrentManager().getInstanceByType(TestConversationManager.class);
      
      assert cMgr.getConversationInstance() != null;
      assert cMgr.getConversationInstance().get() != null;
      
      Object deserialized = deserialize(serialize(cMgr));
      
      assert deserialized instanceof TestConversationManager;
      TestConversationManager deserializedCMgr = (TestConversationManager) deserialized;
      assert deserializedCMgr.getConversationInstance() != null;
      assert deserializedCMgr.getConversationInstance().get() != null;
   }
}
