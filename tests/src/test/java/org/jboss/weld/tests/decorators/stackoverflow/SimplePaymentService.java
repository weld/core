/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jboss.weld.tests.decorators.stackoverflow;

import java.math.BigDecimal;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

/**
 * 
 * @author wayne
 */
@SimpleService
@ApplicationScoped
public class SimplePaymentService implements PaymentService
{
   @Inject
   Logger logger;

   public boolean pay(String account, BigDecimal amount)
   {
      logger.info("Pay ${} for {}.", amount, account);

      return true;
   }
}
