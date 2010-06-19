/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.examples.pastecode.session;

import java.io.Serializable;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import org.jboss.weld.examples.pastecode.model.CodeFragment;

/**
 * Prohibit posting more than 2 fragments a minute
 * 
 * @author Pete Muir
 *
 */
@Decorator
public abstract class FloodingDecorator implements CodeFragmentManager, Serializable
{

   private static final long serialVersionUID = -4615837206290420112L;

   @Inject @Delegate 
   private CodeFragmentManager codeFragmentManager; 
   
   @Inject
   private PostTracker postTracker;
   
   public String addCodeFragment(CodeFragment code, boolean privateFragment)
   {
      // Check if we are allowed to post
      if (postTracker.isNewPostAllowed())
      {
         postTracker.addPost();
         return codeFragmentManager.addCodeFragment(code, privateFragment);
      }
      else
      {
         throw new IllegalStateException("You've posted more than 2 fragments in the last 20s. No flooding allowed!");
      }
   }
   
}
