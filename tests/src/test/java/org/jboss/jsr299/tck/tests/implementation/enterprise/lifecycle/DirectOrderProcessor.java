package org.jboss.jsr299.tck.tests.implementation.enterprise.lifecycle;

import javax.ejb.Stateful;

@Stateful
public class DirectOrderProcessor extends OrderProcessor implements DirectOrderProcessorLocal
{

}
