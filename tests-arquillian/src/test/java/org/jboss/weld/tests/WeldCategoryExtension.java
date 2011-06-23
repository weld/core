package org.jboss.weld.tests;

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class WeldCategoryExtension implements LoadableExtension
{

   public void register(ExtensionBuilder builder)
   {
      builder.service(AuxiliaryArchiveAppender.class, CategoryArchiveAppender.class);
   }

}
