/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.examples.login.test.selenium;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.jboss.test.selenium.AbstractTestCase;
import org.jboss.test.selenium.locator.XpathLocator;
import org.jboss.test.selenium.guard.request.RequestTypeGuardFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests login examples in Weld
 * 
 * @author kpiwko
 * @author plenyi
 */
public class CommonLoginTest extends AbstractTestCase
{

   protected String MAIN_PAGE = "/home.jsf";
   protected String LOGGED_IN = "xpath=//li[contains(text(),'Welcome')]";
   protected String LOGGED_OUT = "xpath=//li[contains(text(),'Goodbye')]";

   protected String USERNAME_FIELD = "id=loginForm:username";
   protected String PASSWORD_FIELD = "id=loginForm:password";

   protected String LOGIN_BUTTON = "id=loginForm:login";
   protected String LOGOUT_BUTTON = "id=loginForm:logout";

   @Test
   public void loginTest()
   {
      assertFalse(selenium.isElementPresent(new XpathLocator(LOGGED_IN)), "User should not be logged in!");
      selenium.type(new XpathLocator(USERNAME_FIELD), "demo");
      selenium.type(new XpathLocator(PASSWORD_FIELD), "demo");
      RequestTypeGuardFactory.waitHttp(selenium).click(new XpathLocator(LOGIN_BUTTON));
      assertTrue(selenium.isElementPresent(new XpathLocator(LOGGED_IN)), "User should be logged in!");
   }

   @Test
   public void logoutTest()
   {
      assertFalse(selenium.isElementPresent(new XpathLocator(LOGGED_IN)), "User should not be logged in!");
      selenium.type(new XpathLocator(USERNAME_FIELD), "demo");
      selenium.type(new XpathLocator(PASSWORD_FIELD), "demo");
      RequestTypeGuardFactory.waitHttp(selenium).click(new XpathLocator(LOGIN_BUTTON));
      assertTrue(selenium.isElementPresent(new XpathLocator(LOGGED_IN)), "User should be logged in!");
      RequestTypeGuardFactory.waitHttp(selenium).click(new XpathLocator(LOGOUT_BUTTON));
      assertTrue(selenium.isElementPresent(new XpathLocator(LOGGED_OUT)), "User should not be logged in!");
   }

}
