package org.jboss.weld.tests.generic;

/**
 * @author Marius Bogoevici
 */
public interface GenericInterface<T>
{
   T echo(T parameter);
}
