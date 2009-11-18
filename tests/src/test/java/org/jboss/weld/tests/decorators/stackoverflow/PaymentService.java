/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jboss.weld.tests.decorators.stackoverflow;

import java.math.BigDecimal;

/**
 *
 * @author wayne
 */
public interface PaymentService {
    boolean pay(String account, BigDecimal amount);
}
