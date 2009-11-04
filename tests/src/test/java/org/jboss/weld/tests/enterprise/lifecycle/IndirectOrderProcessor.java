package org.jboss.weld.tests.enterprise.lifecycle;

import javax.ejb.Stateful;

@Stateful
public class IndirectOrderProcessor extends IntermediateOrderProcessor implements OrderProcessorLocal
{
 
}