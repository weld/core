package org.jboss.weld.examples.pastecode.session;

import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.*;
import org.jboss.weld.examples.pastecode.model.Code;
import javax.inject.*;
import java.util.*;

/**
 * Session Bean implementation class CodeEAOBean
 */
@Stateless
@Named("codeEAOBean")
public class CodeEAOBean implements CodeEAO
{

   @PersistenceContext(unitName = "pastecodeDatabase")
   private EntityManager em;

   @Resource
   EJBContext ctx;

   @Inject
   private HashComputer hashComp;

   private int MAX_CODES = 7;

   private int PAGE_SIZE = 2;

   public CodeEAOBean()
   {
   }

   public String addCode(Code code, boolean secured)
   {
      String result;
      code.setDatetime(Calendar.getInstance().getTime());
      if (code.getUser().trim().isEmpty())
      {
         code.setUser("Anonymous");
      }

      /* compute hash value and return it if secured flag has been set */
      if (secured)
      {
         try
         {
            String hashValue = hashComp.getHashValue(code);
            code.setHash(hashValue);
            result = hashValue;
            em.persist(code);
         }
         catch (NoSuchAlgorithmException e)
         {
            e.printStackTrace();
            return null;
         }
      }
      else
      {
         em.persist(code);
         result = new Integer(code.getId()).toString();
      }

      //System.out.println("Result: " + result);

      return result;
   }

   public Code getCode(String id)
   {
      boolean secured = true;

      try
      {
         Integer.parseInt(id);
         secured = false; /*
                           * if it is possible to convert to number -> not
                           * secured, otherwise -> secured
                           */
      }
      catch (NumberFormatException e)
      {
      }

      if (secured)
      {
         Query q = em.createQuery("SELECT c FROM Code c WHERE hash = :hash");
         q.setParameter("hash", id);
         return (Code) q.getSingleResult();
      }
      else
      {
         Code c = em.find(Code.class, Integer.parseInt(id));
         /*
          * if somebody is trying to guess Id of secured code paste he cannot
          * pass
          */
         if (c.getHash() == null)
         {
            return c;
         }
         else
         {
            throw new EJBException("Access denied");
         }
      }
   }

   public List<Code> recentCodes()
   {
      Query q = em.createQuery("SELECT c FROM Code c WHERE hash=null ORDER BY datetime DESC ");
      q.setMaxResults(MAX_CODES);
      List<Code> codes = q.getResultList();
      return codes;
   }


   @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
   public List<Code> allCodes()
   {
      Query q = em.createQuery("SELECT c FROM Code c WHERE hash=null ORDER BY datetime DESC ");
      List<Code> codes = q.getResultList();
      return codes;
   }
   
   /**
    * getting codes from database needs new transaction so that we can
    * further modify returned Codes without affecting database (when we call
    * this function from another session bean
    */
   @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
   public List<Code> searchCodes(Code code, int page, QueryInfo info)
   {
      StringBuilder sb = new StringBuilder();

      String delim = "";
      if (!code.getUser().trim().equals(""))
      {
         sb.append("c.user = \'" + code.getUser().trim().toLowerCase() + "\'");
         delim = " AND";
      }
      if (!code.getLanguage().trim().equals(""))
      {
         sb.append(delim).append(" c.language = \'" + code.getLanguage().trim().toLowerCase() + "\'");
         delim = " AND";
      }
      if (!code.getNote().trim().equals(""))
      {
         sb.append(delim).append(" c.note LIKE \'%" + code.getNote().trim().toLowerCase() + "%\'");
         delim = " AND";
      }
      if (!code.getText().trim().equals(""))
      {
         sb.append(delim).append(" c.text LIKE \'%" + code.getText().toLowerCase() + "%\'");
         delim = " AND";
      }
      if (code.getDatetime() != null)
      {
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
         Date date2 = new Date();
         date2.setTime(code.getDatetime().getTime() + 24 * 60 * 60 * 1000); // +1 day

         String formattedDate1 = formatter.format(code.getDatetime());
         String formattedDate2 = formatter.format(date2);

         sb.append(delim).append(" c.datetime between \'" + formattedDate1 + "\' and \'" + formattedDate2 + "\'");
         delim = " AND";
      }

      if (sb.toString().length() == 0)
         sb.append("1 = \'1\'");

      Query q = em.createQuery("SELECT c FROM Code c WHERE hash=null AND " + sb.toString() + " ORDER BY datetime DESC");
      int allRecords = q.getResultList().size();
      q.setFirstResult(page * PAGE_SIZE);
      q.setMaxResults(PAGE_SIZE);
      List<Code> codes = q.getResultList();

      info.setPage(page);
      info.setRecordsCount(allRecords);
      info.setPagesCount(allRecords / PAGE_SIZE);

      return codes;
   }
}
