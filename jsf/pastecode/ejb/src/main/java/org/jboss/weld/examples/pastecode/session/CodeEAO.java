package org.jboss.weld.examples.pastecode.session;

import javax.ejb.Local;
import java.util.List;
import org.jboss.weld.examples.pastecode.model.Code;

@Local
public interface CodeEAO
{
   public String addCode(Code code, boolean secured);

   public Code getCode(String id);

   public List<Code> allCodes();

   public List<Code> recentCodes();

   public List<Code> searchCodes(Code code, int page, QueryInfo info);
}
