package org.jboss.webbeans.test.beans;

import java.util.Set;

import javax.webbeans.Observer;
import javax.webbeans.Observes;
import javax.webbeans.manager.Manager;

/**
 * Simple bean with observer method and another injectable parameter.
 *
 */
public class BananaSpider
{
   public void observeStringEvent(@Observes String someEvent, Manager manager)
   {
      assert someEvent != null;
      assert manager != null;
      Set<Observer<String>> allStringObservers = manager.resolveObservers(someEvent);
      assert allStringObservers != null;
      assert allStringObservers.size() > 0;
   }
}
