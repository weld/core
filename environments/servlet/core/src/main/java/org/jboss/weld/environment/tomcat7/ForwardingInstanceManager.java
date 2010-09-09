package org.jboss.weld.environment.tomcat7;
import java.lang.reflect.InvocationTargetException;

import javax.naming.NamingException;


import org.apache.tomcat.InstanceManager;

public abstract class ForwardingInstanceManager implements InstanceManager
{
   
   protected abstract InstanceManager delegate();

public void destroyInstance(Object arg0) throws IllegalAccessException,
		InvocationTargetException {
	delegate().destroyInstance(arg0);
	
}

public void newInstance(Object arg0) throws IllegalAccessException,
		InvocationTargetException, NamingException {
	delegate().newInstance(arg0);
}

public Object newInstance(String arg0, ClassLoader arg1)
		throws IllegalAccessException, InvocationTargetException,
		NamingException, InstantiationException, ClassNotFoundException {
	// TODO Auto-generated method stub
	return delegate().newInstance(arg0,arg1);
}

public Object newInstance(String arg0) throws IllegalAccessException,
		InvocationTargetException, NamingException, InstantiationException,
		ClassNotFoundException {
	// TODO Auto-generated method stub
	return delegate().newInstance(arg0);
}
   
 
   
}