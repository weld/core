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
   private Code code;

   private String codeId;

   private String brush;

   private String theme;

   private boolean secured = false;

   @Inject
   DataBean data;

   transient @Inject
   CodeEAO eao;

   public Paster()
   {
   }

   @PostConstruct
   public void postConstruct()
   {
      this.code = new Code();
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
   public Code getPasterCodeInstance()
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

   public List<Code> getCodes()
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
