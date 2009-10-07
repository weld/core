package org.jboss.webbeans.test.unit.implementation.enterprise.lifecycle;

import javax.ejb.Stateful;

@Stateful
public class IndirectOrderProcessor extends IntermediateOrderProcessor implements OrderProcessorLocal
{
 
}