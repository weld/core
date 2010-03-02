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

import java.util.List;
import javax.ejb.Stateful;
import javax.annotation.PostConstruct;
import org.jboss.weld.examples.pastecode.model.CodeEntity;
import javax.inject.Named;
import javax.inject.Inject;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;

/**
 * Session Bean implementation class HistoryBean
 */

@SessionScoped
@Named("history")
@Stateful
public class HistoryBean implements History, Serializable
{

   private static final long serialVersionUID = 20L;

   transient @Inject
   Code eao;

   private QueryInfo info;

   private List<CodeEntity> codes;

   private int TRIMMED_TEXT_LEN = 120;

   private CodeEntity searchItem;

   private int page = 0;

   public HistoryBean()
   {
   }

   @PostConstruct
   public void initialize()
   {
      this.searchItem = new CodeEntity();
      // this.info = new QueryInfo();
   }

   public List<CodeEntity> getCodes()
   {
      return this.codes;
   }

   public void setCodes(List<CodeEntity> codes)
   {
      this.codes = codes;
   }

   @Produces
   @Named("searchItem")
   public CodeEntity getSearchItem()
   {
      return searchItem;
   }

   public void setSearchItem(CodeEntity searchItem)
   {
      this.searchItem = searchItem;
   }

   public String newSearch()
   {
      this.page = 0;
      return "history";
   }

   public String search()
   {
      this.info = new QueryInfo();
      this.codes = null;
      this.codes = eao.searchCodes(this.searchItem, this.page, this.info);

      for (int i = 0; i != this.codes.size(); i++)
      {
         String s = this.codes.get(i).getText();
         this.codes.get(i).setText(s.substring(0, s.length() > TRIMMED_TEXT_LEN ? TRIMMED_TEXT_LEN : s.length()) + "  .....");
      }
      return "history";
   }

   public int getPage()
   {
      return page;
   }

   public void setPage(int page)
   {
      this.page = page;
   }

   public QueryInfo getInfo()
   {
      return info;
   }

   public void setInfo(QueryInfo info)
   {
      this.info = info;
   }
}
