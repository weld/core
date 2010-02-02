package org.jboss.weld.tests.extensions.multipleBeans;

import javax.enterprise.inject.Produces;

public class BlogSource
{
   @Produces
   @Author(name = "Barry")
   String barrysBlog = "Barry's content";

   @Produces
   @Author(name = "Bob")
   String bobsBlog = "Bob's content";
}
