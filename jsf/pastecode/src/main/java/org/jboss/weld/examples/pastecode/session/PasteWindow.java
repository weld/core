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

import javax.ejb.EJBException;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.weld.examples.pastecode.model.CodeFragment;

/**
 * PasteWindow holds the code fragment and other selections when a code fragment is viewed and entered
 *
 */
@Named
@RequestScoped
public class PasteWindow
{
   private CodeFragment codeFragment;

   private String codeFragmentId;

   private Theme theme;

   private boolean privateFragment;

   @Inject
   private CodeFragmentManager codeFragmentManager;

   public PasteWindow()
   {
      this.codeFragment = new CodeFragment();
      this.theme = Theme.DEFAULT;
   }

   // The send method is called when we hit the Send button
   public String send()
   {
      codeFragmentId = codeFragmentManager.addCodeFragment(codeFragment, privateFragment);
      return "success";
   }

   // loadCodeFragment is a view action called to load the code fragment from
   // the database when requested for viewing
   public void loadCodeFragment()
   {
      this.codeFragment = codeFragmentManager.getCodeFragment(codeFragmentId);

      if (this.codeFragment == null)
      {
         throw new EJBException("Could not read entity with given id value");
      }
   }

   public CodeFragment getCodeFragment()
   {
      return codeFragment;
   }

   public String getCodeFragmentId()
   {
      return codeFragmentId;
   }

   public void setCodeFragmentId(String codeFragmentId)
   {
      this.codeFragmentId = codeFragmentId;
   }

   public Theme getTheme()
   {
      return theme;
   }

   public void setTheme(Theme theme)
   {
      this.theme = theme;
   }

   public boolean isPrivateFragment()
   {
      return privateFragment;
   }

   public void setPrivateFragment(boolean privateFragment)
   {
      this.privateFragment = privateFragment;
   }
}
