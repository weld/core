package org.jboss.weld.tests.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.conversation.ConversationManager;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class SerializationTest extends AbstractWeldTest
{
   @SuppressWarnings("unchecked")
   @Test(description="WELD-363")
   public void testConversationManagerSerializable()
      throws IOException, ClassNotFoundException
   {
      Bean<ConversationManager> cmBean = (Bean<ConversationManager>) getCurrentManager()
         .getBeans(ConversationManager.class).iterator().next();
      
      ConversationManager cMgr = cmBean.create(getCurrentManager().createCreationalContext(cmBean));
      
      ByteArrayOutputStream serialized = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(serialized);
      
      out.writeObject(cMgr);
      out.flush();
      
      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(serialized.toByteArray()));
      Object deserialized = in.readObject();
      
      assert deserialized instanceof ConversationManager;
   }
}
