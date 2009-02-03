package org.jboss.webbeans.conversation;

import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpSession;

import org.jboss.webbeans.context.ConversationContext;
import org.jboss.webbeans.servlet.ConversationBeanMap;

public class ConversationEntry
{
   private String cid;
   private Future<?> terminationHandle;
   private ReentrantLock concurrencyLock;

   protected ConversationEntry(String cid, Future<?> terminationHandle)
   {
      this.cid = cid;
      this.terminationHandle = terminationHandle;
      this.concurrencyLock = new ReentrantLock(true);
   }

   public static ConversationEntry of(String cid, Future<?> terminationHandle)
   {
      return new ConversationEntry(cid, terminationHandle);
   }

   public boolean cancelTermination()
   {
      return terminationHandle.cancel(false);
   }

   public void destroy(HttpSession session)
   {
      ConversationContext terminationContext = new ConversationContext();
      terminationContext.setBeanMap(new ConversationBeanMap(session, cid));
      terminationContext.destroy();
   }
   
   public void lock() {
      concurrencyLock.lock();
   }
   
   public void unlock() {
      concurrencyLock.unlock();
   }

}
