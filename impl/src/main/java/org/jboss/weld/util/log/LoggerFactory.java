package org.jboss.weld.util.log;

import org.slf4j.cal10n.LocLogger;
import org.slf4j.cal10n.LocLoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import ch.qos.cal10n.IMessageConveyor;

public class LoggerFactory
{
   
   private static LoggerFactory INSTANCE = new LoggerFactory();
   
   private final LocLoggerFactory locLoggerFactory;
   
   private LoggerFactory() 
   {
      IMessageConveyor messageConveyor = new MessageConveyerFactoryLoader().getMessageConveyerFactory().getDefaultMessageConveyer();
      this.locLoggerFactory = new LocLoggerFactory(messageConveyor);
   }
   
   public LocLogger getLogger(Category category)
   {
      return locLoggerFactory.getLocLogger(category.getName());
   }
   
   public XLogger getXLogger(Category category)
   {
      return XLoggerFactory.getXLogger(category.getName());
   }

   public static LoggerFactory loggerFactory()
   {
      return INSTANCE;
   }
}
