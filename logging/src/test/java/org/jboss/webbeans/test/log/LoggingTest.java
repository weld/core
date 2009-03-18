package org.jboss.webbeans.test.log;

import org.jboss.webbeans.log.Log;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.testng.annotations.Test;

/**
 * Simple tests to verify proper operation of the logging system.
 * 
 * @author David Allen
 *
 */
public class LoggingTest
{
   private static final String TEST_MSG = "Test Message";

   @Test
   public void testLogProvider()
   {
      LogProvider logProvider = Logging.getLogProvider(LoggingTest.class);
      assert logProvider != null;
   }
   
   @Test
   public void testLogBasedOnClassObject()
   {
      Log log = Logging.getLog(LoggingTest.class);
      assert log != null : "No Log object returned";
      
      log.info(TEST_MSG);
      assert TestAppender.getLastEvent() != null : "There was no last event in Log4j";
      assert TestAppender.getLastEvent().getLoggerName().equals("org.jboss.webbeans.test.log.LoggingTest");
      assert TestAppender.getLastEvent().getRenderedMessage().equals(TEST_MSG) : "Last event has incorrect message";
   }

   @Test
   public void testLogBasedOnStringObject()
   {
      Log log = Logging.getLog("MyLogger");
      assert log != null;
      
      log.info(TEST_MSG);
      assert TestAppender.getLastEvent() != null : "There was no last event in Log4j";
      assert TestAppender.getLastEvent().getLoggerName().equals("MyLogger");
      assert TestAppender.getLastEvent().getRenderedMessage().equals(TEST_MSG) : "Last event has incorrect message";
   }
   
   @Test
   public void testLogMessageFormatting()
   {
      Log log = Logging.getLog(LoggingTest.class);
      assert log != null : "No Log object returned";
      
      log.info("Message with {0} parameters starting with {1} and including {2}", 3, "param1", "param2");
      assert TestAppender.getLastEvent() != null : "There was no last event in Log4j";
      assert TestAppender.getLastEvent().getRenderedMessage().equals("Message with 3 parameters starting with param1 and including param2");
   }
}
