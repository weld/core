package org.jboss.weld.test.log;

import org.jboss.weld.log.Log;
import org.jboss.weld.log.LogProvider;
import org.jboss.weld.log.Logging;
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
      assert TestAppender.getLastEvent().getLoggerName().equals("org.jboss.weld.test.log.LoggingTest");
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

   @Test
   public void testLogMessageFormattingWithNullValue()
   {
      Log log = Logging.getLog(LoggingTest.class);
      assert log != null : "No Log object returned";

      String value = null;
      log.info("Verify we do not barf on a {0} value", value);
      assert TestAppender.getLastEvent() != null : "There was no last event in Log4j";
      assert TestAppender.getLastEvent().getRenderedMessage().equals("Verify we do not barf on a null value");
   }
   
   @Test
   public void testArrayConversion()
   {
      Log log = Logging.getLog(LoggingTest.class);
      assert log != null : "No Log object returned";
      
      String[] arr = new String[2];
      arr[0] = "foo";
      arr[1] = "bar";
      
      log.info("Message with {0} array {1}", arr, "baz");
      assert TestAppender.getLastEvent() != null : "There was no last event in Log4j";
      assert TestAppender.getLastEvent().getRenderedMessage().equals("Message with [foo, bar] array baz");
   }
}
