package org.jboss.weld.examples.pastecode.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.jboss.weld.examples.pastecode.model.CodeFragment;

/**
 * Holds a log of code fragments added by all users
 * 
 * Threadsafe.
 * 
 * @author Pete Muir
 * 
 */
@ApplicationScoped
@Singleton
public class CodeFragmentLogger
{

   private final List<CodeFragment> log;

   public CodeFragmentLogger()
   {
      this.log = new ArrayList<CodeFragment>();
   }

   @Lock(LockType.READ)
   public List<CodeFragment> getLog()
   {
      return Collections.unmodifiableList(log);
   }

   @Lock(LockType.WRITE)
   public void clearLog()
   {
      this.log.clear();
   }

   @Lock(LockType.WRITE)
   public void addEntry(@Observes CodeFragment codeFragment)
   {
      this.log.add(codeFragment);
   }

}
