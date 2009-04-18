package org.jboss.jsr299.tck.tests.implementation.enterprise.lifecycle;

import javax.ejb.Stateful;

@Stateful
public class IndirectOrderProcessor extends IntermediateOrderProcessor implements OrderProcessorLocal
{
 
}