/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jboss.weld.tests.decorators.stackoverflow;

import java.math.BigDecimal;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import org.slf4j.Logger;

/**
 * Secure PaymentService implemented by decator
 * 
 * @author wayne
 */
@Decorator
class SecurePaymentService implements PaymentService
{
   @Inject
   private Logger logger;

   @Inject
   @Delegate
   @SimpleService
   private PaymentService paymentService;

   public boolean pay(String account, BigDecimal amount)
   {
      logger.info("I'm a secure payment service");

      return paymentService.pay(account, amount);
   }

}
