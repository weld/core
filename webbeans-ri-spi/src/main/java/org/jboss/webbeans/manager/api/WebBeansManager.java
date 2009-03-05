package org.jboss.webbeans.manager.api;

import javax.inject.manager.Manager;
import javax.servlet.Servlet;

public interface WebBeansManager extends Manager
{
   
   public void injectIntoServlet(Servlet servlet);
   
}
