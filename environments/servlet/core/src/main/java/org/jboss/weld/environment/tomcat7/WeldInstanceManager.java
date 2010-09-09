package org.jboss.weld.environment.tomcat7;


import java.lang.reflect.InvocationTargetException;

import javax.naming.NamingException;

import org.apache.tomcat.InstanceManager;
import org.jboss.weld.environment.servlet.inject.AbstractInjector;
import org.jboss.weld.manager.api.WeldManager;

public class WeldInstanceManager  extends AbstractInjector implements InstanceManager
{

	protected WeldInstanceManager(WeldManager manager) {
		super(manager);
		// TODO Auto-generated constructor stub
	}

	
	public void destroyInstance(Object arg0) throws IllegalAccessException,
			InvocationTargetException {
		
		
	}

	
	public Object newInstance(String arg0) throws IllegalAccessException,
			InvocationTargetException, NamingException, InstantiationException,
			ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void newInstance(Object arg0) throws IllegalAccessException,
			InvocationTargetException, NamingException {
		inject(arg0);
		
	}

	
	public Object newInstance(String arg0, ClassLoader arg1)
			throws IllegalAccessException, InvocationTargetException,
			NamingException, InstantiationException, ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}
   

   
   
}