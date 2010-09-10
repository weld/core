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
import org.jboss.test.selenium.guard.request.RequestTypeGuardFactory;
import org.jboss.test.selenium.locator.IdLocator;
import org.jboss.test.selenium.locator.XpathLocator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.jboss.test.selenium.locator.LocatorFactory.id;
import static org.jboss.test.selenium.locator.LocatorFactory.xp;

/**
 * Tests login examples in Weld
 * 
 * @author kpiwko
 * @author plenyi
 */
public class CommonLoginTest extends AbstractTestCase
{
   protected String MAIN_PAGE = "/home.jsf";
   protected XpathLocator LOGGED_IN = xp("//li[contains(text(),'Welcome')]");
   protected XpathLocator LOGGED_OUT = xp("//li[contains(text(),'Goodbye')]");
   
   protected IdLocator USERNAME_FIELD = id("loginForm:username");
   protected IdLocator PASSWORD_FIELD = id("loginForm:password");
   
   protected IdLocator LOGIN_BUTTON = id("loginForm:login");
   protected IdLocator LOGOUT_BUTTON = id("loginForm:logout");
   
   @BeforeMethod
   public void openStartUrl()
   {
      selenium.deleteAllVisibleCookies();
      selenium.open(contextPath);
   }

   @Test
   public void loginTest()
   {
      waitModel.until(elementPresent.locator(USERNAME_FIELD));
      assertFalse(selenium.isElementPresent(LOGGED_IN), "User should not be logged in!");
      selenium.type(USERNAME_FIELD, "demo");
      selenium.type(PASSWORD_FIELD, "demo");
      RequestTypeGuardFactory.waitHttp(selenium).click(LOGIN_BUTTON);
      assertTrue(selenium.isElementPresent(LOGGED_IN), "User should be logged in!");
   }

   @Test
   public void logoutTest()
   {
      waitModel.until(elementPresent.locator(USERNAME_FIELD));
      assertFalse(selenium.isElementPresent(LOGOUT_BUTTON), "User should not be logged in!");
      selenium.type(USERNAME_FIELD, "demo");
      selenium.type(PASSWORD_FIELD, "demo");
      RequestTypeGuardFactory.waitHttp(selenium).click(LOGIN_BUTTON);
      assertTrue(selenium.isElementPresent(LOGGED_IN), "User should be logged in!");
      RequestTypeGuardFactory.waitHttp(selenium).click(LOGOUT_BUTTON);
      assertTrue(selenium.isElementPresent(LOGGED_OUT), "User should not be logged in!");
   }

}
