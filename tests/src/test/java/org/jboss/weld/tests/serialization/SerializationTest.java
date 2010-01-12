package org.jboss.weld.tests.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class SerializationTest extends AbstractWeldTest
{
   @SuppressWarnings("unchecked")
   @Test(description="WELD-363", groups = "broken")
   public void testConversationManagerSerializable()
      throws Exception
   {
      TestConversationManager cMgr = getCurrentManager().getInstanceByType(TestConversationManager.class);
      
      assert cMgr.getConversationInstance() != null;
      assert cMgr.getConversationInstance().get() != null;
      
      ByteArrayOutputStream serialized = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(serialized);
      
      out.writeObject(cMgr);
      out.flush();
      
      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(serialized.toByteArray()));
      Object deserialized = in.readObject();
      
      assert deserialized instanceof TestConversationManager;
      TestConversationManager deserializedCMgr = (TestConversationManager) deserialized;
      assert deserializedCMgr.getConversationInstance() != null;
      assert deserializedCMgr.getConversationInstance().get() != null;
   }
}
