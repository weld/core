package org.jboss.weld.tests.extensions.multipleBeans;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

public class BlogFormatter
{
   @Inject
   @Author(name = "Barry")
   public String content;

   @Produces
   @FormattedBlog(name = "Barry")
   public String format()
   {
      return "+" + content + "+";
   }

}
