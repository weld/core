package org.jboss.weld.tests.enterprise.lifecycle;

import javax.ejb.Stateful;

@Stateful
public class DirectOrderProcessor extends OrderProcessor implements DirectOrderProcessorLocal
{

}
