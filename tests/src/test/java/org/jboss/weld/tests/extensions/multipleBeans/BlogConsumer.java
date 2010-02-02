package org.jboss.weld.tests.extensions.multipleBeans;

import javax.inject.Inject;

@Consumer(name = "Barry")
public class BlogConsumer
{
   @Inject
   @FormattedBlog(name = "Barry")
   public String blogContent;
}
