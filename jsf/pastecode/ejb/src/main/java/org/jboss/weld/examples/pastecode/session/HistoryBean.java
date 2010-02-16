package org.jboss.weld.examples.pastecode.session;

import java.util.List;
import javax.ejb.Stateful;
import javax.annotation.PostConstruct;
import org.jboss.weld.examples.pastecode.model.Code;
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
   CodeEAO eao;

   private QueryInfo info;

   private List<Code> codes;

   private int TRIMMED_TEXT_LEN = 120;

   private Code searchItem;

   private int page = 0;

   public HistoryBean()
   {
   }

   @PostConstruct
   public void initialize()
   {
      this.searchItem = new Code();
      // this.info = new QueryInfo();
   }

   public List<Code> getCodes()
   {
      return this.codes;
   }

   public void setCodes(List<Code> codes)
   {
      this.codes = codes;
   }

   @Produces
   @Named("searchItem")
   public Code getSearchItem()
   {
      return searchItem;
   }

   public void setSearchItem(Code searchItem)
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
