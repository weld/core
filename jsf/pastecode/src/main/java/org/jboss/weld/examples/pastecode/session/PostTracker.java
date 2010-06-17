package org.jboss.weld.examples.pastecode.session;

import java.util.Date;
import java.util.LinkedList;

import javax.ejb.Stateful;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

@SessionScoped
@Stateful // Add passivation capabilities....
public class PostTracker
{
   
   private LinkedList<Date> posts;
   
   @Inject DatabasePopulater databasePopulater;
   
   public PostTracker()
   {
      this.posts = new LinkedList<Date>();
   }
   
   public void addPost()
   {
      this.posts.offerFirst(new Date());
   }
   
   public boolean isNewPostAllowed()
   {
      // if we are populating the database, skip
      if (!databasePopulater.isPopulated())
      {
         return true;
      }
      long diff = new Date().getTime() - posts.get(2).getTime();
      return diff > 20 * 1000;
   }

}
