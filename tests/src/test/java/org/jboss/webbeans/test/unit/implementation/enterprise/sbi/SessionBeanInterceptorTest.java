package org.jboss.webbeans.test.unit.implementation.enterprise.sbi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class SessionBeanInterceptorTest extends AbstractWebBeansTest
{
   
   @Test(groups="incontainer-broken")
   public void testSerializeSessionBeanInterceptor() throws Exception
   {
      Bean<?> foobean =  getCurrentManager().getNewEnterpriseBeanMap().get(Foo.class);
      assert foobean != null;
      MockSessionBeanInterceptor interceptor = new MockSessionBeanInterceptor();
      interceptor.postConstruct(new MockInvocationContext());
      assert interceptor.getBean() != null;
      Bean<?> bean = interceptor.getBean();
      interceptor = (MockSessionBeanInterceptor) deserialize(serialize(interceptor));
      assert interceptor.getBean() != null;
      assert bean.equals(interceptor.getBean());
   }
   
   protected byte[] serialize(Object instance) throws IOException
   {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bytes);
      out.writeObject(instance);
      return bytes.toByteArray();
   }

   protected Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException
   {
      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
      return in.readObject();
   }
   
}
