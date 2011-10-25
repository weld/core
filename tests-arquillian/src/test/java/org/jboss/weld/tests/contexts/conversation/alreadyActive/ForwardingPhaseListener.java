/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.contexts.conversation.alreadyActive;

import javax.faces.component.UIViewRoot;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class ForwardingPhaseListener implements PhaseListener
{
   private static final long serialVersionUID = 7215399908914547315L;

   public void afterPhase(PhaseEvent event)
   {
      if (PhaseId.RESTORE_VIEW.equals(event.getPhaseId()))
      {
         if ("/conversations.xhtml".equals(getViewId(event)))
         {
            try {
               HttpServletRequest request = (HttpServletRequest) event.getFacesContext().getExternalContext()
                        .getRequest();
               HttpServletResponse response = (HttpServletResponse) event.getFacesContext().getExternalContext()
                        .getResponse();
               request.getRequestDispatcher("/result.jsf").forward(request, response);
               event.getFacesContext().responseComplete();
            }
            catch (Exception e) {
               throw new RuntimeException("blah", e);
            }
         }
      }
   }

   private String getViewId(PhaseEvent event)
   {
      UIViewRoot viewRoot = event.getFacesContext().getViewRoot();
      return viewRoot == null ? null : viewRoot.getViewId();
   }

   public void beforePhase(PhaseEvent event)
   {
      if (PhaseId.RESTORE_VIEW.equals(event.getPhaseId()))
      {
         String uri = ((HttpServletRequest) event.getFacesContext().getExternalContext().getRequest()).getRequestURI();
         if (uri.contains("missing-page-error"))
         {
            try {
               HttpServletResponse response = (HttpServletResponse) event.getFacesContext().getExternalContext()
                        .getResponse();
               response.sendError(404);
               event.getFacesContext().responseComplete();
            }
            catch (Exception e) {
               throw new RuntimeException("blah", e);
            }
         }
      }
   }

   public PhaseId getPhaseId()
   {
      return PhaseId.ANY_PHASE;
   }

}
