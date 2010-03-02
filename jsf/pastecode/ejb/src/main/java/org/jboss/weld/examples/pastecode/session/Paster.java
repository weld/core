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

import javax.inject.*;
import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.enterprise.inject.*;
import org.jboss.weld.examples.pastecode.model.*;
import java.util.List;

@Model
public class Paster
{
   private CodeEntity code;

   private String codeId;

   private String brush;

   private String theme;

   private boolean secured = false;

   @Inject DataBean data;

   transient @Inject Code eao;

   public Paster()
   {
   }

   @PostConstruct
   public void postConstruct()
   {
      this.code = new CodeEntity();
      this.theme = "shThemeDefault.css";
   }

   public String paste()
   {
      this.codeId = eao.addCode(code, secured);
      return "success";
   }

   /* used for access from jsf page */
   @Produces
   @Named("code")
   public CodeEntity getPasterCodeInstance()
   {
      return this.code;
   }

   public void loadCode()
   {
      this.code = eao.getCode(codeId);

      if (this.code == null)
         throw new EJBException("Could not read entity with given id value");

      this.brush = data.getBrush(this.code.getLanguage());
   }

   public List<CodeEntity> getCodes()
   {
      return eao.recentCodes();
   }

   public String getCodeId()
   {
      return codeId;
   }

   public void setCodeId(String codeId)
   {
      this.codeId = codeId;
   }

   public String getTheme()
   {
      return theme;
   }

   public void setTheme(String theme)
   {
      this.theme = theme;
   }

   public String getBrush()
   {
      return brush;
   }

   public void setBrush(String brush)
   {
      this.brush = brush;
   }

   public boolean isSecured()
   {
      return secured;
   }

   public void setSecured(boolean secured)
   {
      this.secured = secured;
   }
}
