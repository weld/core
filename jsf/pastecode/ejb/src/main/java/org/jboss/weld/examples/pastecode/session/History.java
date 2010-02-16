package org.jboss.weld.examples.pastecode.session;

import java.util.List;
import javax.ejb.Local;
import org.jboss.weld.examples.pastecode.model.Code;

@Local
public interface History
{
   public List<Code> getCodes();

   public void setCodes(List<Code> codes);

   public Code getSearchItem();

   public void setSearchItem(Code searchItem);

   public String search();

   public String newSearch();

   public void setPage(int page);

   public int getPage();

   public QueryInfo getInfo();

   public void setInfo(QueryInfo info);

}
