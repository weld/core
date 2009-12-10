package org.jboss.weld.tests.unit.bootstrap.xml;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

@Decorator
public class Dec
{
   @Inject @Delegate Plain plain;
}
