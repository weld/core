package org.jboss.webbeans.test.beans;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.webbeans.Current;

public class Farm
{
   
   public Date founded;
   public Date closed;
   public int initialStaff;
   
   @Current
   FarmOffice farmOffice;
   
   @PostConstruct
   private void postConstruct() 
   {
      founded = new Date();
      initialStaff = farmOffice.noOfStaff;
   }
   
   @PreDestroy
   private void preDestroy() 
   {
      closed = new Date();
   }
   
}
