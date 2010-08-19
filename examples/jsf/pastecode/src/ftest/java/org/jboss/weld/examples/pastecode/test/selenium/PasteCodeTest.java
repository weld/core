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
package org.jboss.weld.examples.pastecode.test.selenium;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.test.selenium.AbstractTestCase;
import org.jboss.test.selenium.locator.Attribute;
import org.jboss.test.selenium.locator.AttributeLocator;
import org.jboss.test.selenium.locator.XpathLocator;
import org.jboss.test.selenium.locator.option.OptionLabelLocator;
import org.jboss.test.selenium.locator.option.OptionLocator;
import org.jboss.test.selenium.framework.AjaxSelenium;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.jboss.test.selenium.locator.LocatorFactory.*;
import static org.jboss.test.selenium.locator.option.OptionLocatorFactory.*;
import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.*;

/**
 * This class tests PasteCode example using selenium framework. Furthermore this test
 * is used as a basis for performance tests of Weld. A SmartFrog component is generated
 * from this test through Smartfrog-Sniff and used for measuring availability, scalability,
 * response times and other parameters related to performance.
 * 
 * @author mgencur
 * 
 */
public class PasteCodeTest extends AbstractTestCase
{
   protected String MAIN_PAGE = "home.jsf";
   
   //main page elements
   protected XpathLocator SUBMIT_BUTTON = xp("//input[contains(@src,'img/submit.png')]");
   protected XpathLocator NEW_LINK = xp("//a[contains(text(),'new')]");
   protected XpathLocator HISTORY_LINK = xp("//a[contains(text(),'history')]");
   protected XpathLocator HELP_LINK = xp("//a[contains(text(),'help')]");
   protected XpathLocator POST_AREA = xp("//textarea[contains(@class,'pastecode')]");
   protected XpathLocator SYNTAX_SELECT = xp("//select[contains(@id,'language')]");
   protected XpathLocator EXPOSURE_SELECT = xp("//select[contains(@id,'exposure')]");
   protected OptionLocator<OptionLabelLocator> ANY_SYNTAX = optionLabel("Any");
   protected OptionLocator<OptionLabelLocator> JS_SYNTAX = optionLabel("JavaScript");
   protected OptionLocator<OptionLabelLocator> PRIVATE_EXPOSURE = optionLabel("Private");
   protected XpathLocator NAME_INPUT = xp("//input[contains(@id,'user')]");
   //resulting page after new post submitting
   protected XpathLocator DOWNLOAD_LINK = xp("//a[contains(text(),'DOWNLOAD')]");
   protected XpathLocator PUBLIC_TESTER_LINK = xp("//div[contains(@class,'recentPaste')]/a[contains(text(),'PublicTester')]");
   protected XpathLocator PRIVATE_TESTER_LINK = xp("//div[contains(@class,'recentPaste')]/a[contains(text(),'PrivateTester')]");
   //TODO: change Plain text to JavaScript when it's possible to choose it during posting of code-fragment
   protected XpathLocator JS_TEXT = xp("//span[contains(@class,'recentPasteLang')][contains(text(),'JavaScript')]");
   //recent posts test elements
   protected XpathLocator CRAZYMAN_LINK = xp("//a[contains(text(),'crazyman')][contains(@href,'24')]");
   //history page elements
   protected XpathLocator USER_SEARCH_INPUT = xp("//input[contains(@name,'user')]");
   protected XpathLocator DATE_SEARCH_INPUT = xp("//input[contains(@name,'pasteDate')]");
   protected XpathLocator CODE_SEARCH_INPUT = xp("//textarea[contains(@name,'code')]");
   protected XpathLocator SEARCH_BUTTON = xp("//input[contains(@src,'img/search.png')]");
   protected XpathLocator ACTIVE_FIRST_PAGE_LINK = xp("//span[contains(@class,'currentPage')][contains(text(),'1')]");
   protected XpathLocator SECOND_PAGE_LINK = xp("//a[contains(@class,'pagination')][contains(text(),'2')]");
   protected XpathLocator THIRD_PAGE_LINK = xp("//a[contains(@class,'pagination')][contains(text(),'3')]");
   
   @BeforeMethod
   public void openStartURL() throws MalformedURLException 
   {
       selenium.open(new URL(contextPath.toString() + MAIN_PAGE));
   }
   
   @Test
   public void mainPageTest()
   {
	   assertTrue(selenium.isTextPresent("new"), "A page should contain text 'new'");
       assertTrue(selenium.isTextPresent("martin"), "A page should contain text 'martin'");
       assertTrue(selenium.isTextPresent("6 Feb"), "A page should contain text '6 Feb'");
       assertTrue(selenium.isTextPresent("Exposure"), "A page should contain text 'Exposure'");
   }
   
   @Test(dependsOnMethods={"mainPageTest"})
   public void newPublicPostTest()
   {
	   selenium.type(POST_AREA, CODE_FRAGMENT);
	   selenium.select(SYNTAX_SELECT, JS_SYNTAX);
	   selenium.type(NAME_INPUT, "PublicTester");
	   waitHttp(selenium).click(SUBMIT_BUTTON);

	   assertTrue(isLocationCorrect(selenium.getLocation(), false), "Location is not correct");
	   
	   assertTrue(selenium.isTextPresent("Posted by PublicTester just now"), "A page should contain text 'Posted by PublicTester just now'");
	   assertTrue(selenium.isTextPresent(CODE_FRAGMENT_PART), "A page should contain pasted text");
	   assertTrue(selenium.isElementPresent(DOWNLOAD_LINK), "A page should contain element 'DOWNLOAD'");
	   
	   assertTrue(isDownloadWorking(selenium, DOWNLOAD_LINK, CODE_FRAGMENT_PART), "It should be able to download file from database");
	   
	   assertTrue(selenium.isElementPresent(PUBLIC_TESTER_LINK), "A page should contain element 'Tester'");
	   assertTrue(selenium.isElementPresent(JS_TEXT), "A page should contain element 'JavaScript'");
   }

   @Test(dependsOnMethods={"newPublicPostTest"})
   public void newPrivatePostTest()
   {
	   selenium.type(POST_AREA, CODE_FRAGMENT);
	   selenium.select(SYNTAX_SELECT, JS_SYNTAX);
	   selenium.select(EXPOSURE_SELECT, PRIVATE_EXPOSURE);
	   selenium.type(NAME_INPUT, "PrivateTester");
	   waitHttp(selenium).click(SUBMIT_BUTTON);

       assertTrue(isLocationCorrect(selenium.getLocation(), true), "Location is not correct");
	   assertTrue(selenium.isTextPresent("Posted by PrivateTester just now"), "A page should contain text 'Posted by PrivateTester just now'");
	   assertTrue(selenium.isTextPresent(CODE_FRAGMENT_PART), "A page should contain pasted text");
	   assertTrue(selenium.isElementPresent(DOWNLOAD_LINK), "A page should contain element 'DOWNLOAD'");
	   assertFalse(selenium.isElementPresent(PRIVATE_TESTER_LINK), "A page shouldn't contain element 'PrivateTester'");
   }
   
   @Test
   public void recentPostsTest()
   {
	   waitHttp(selenium).click(CRAZYMAN_LINK);
	   assertTrue(selenium.isTextPresent("Posted by crazyman on 19 Feb"), "A page should contain 'Posted by crazyman on 19 Feb'");
	   assertTrue(selenium.isTextPresent("@GeneratedValue(strategy = GenerationType.IDENTITY)"), "A page should contain code fragment");
   }
   
   @Test(dependsOnMethods={"newPublicPostTest"})
   public void exactSearchTest()
   {
	   waitHttp(selenium).click(HISTORY_LINK);
	   assertTrue(selenium.isTextPresent("Posted by PublicTester"), "A page should contain 'Posted by PublicTester'");
	   assertFalse(selenium.isTextPresent("Posted by PrivateTester"), "A page shouldn't contain 'Posted by PrivateTester'");
	   
	   selenium.type(USER_SEARCH_INPUT, "graham");
	   selenium.select(SYNTAX_SELECT, JS_SYNTAX);
	   selenium.type(DATE_SEARCH_INPUT, "2009-02-02");
	   selenium.type(CODE_SEARCH_INPUT, "toggle_visibility(id)");
	   waitHttp(selenium).click(SEARCH_BUTTON);
	   
	   assertTrue(selenium.isTextPresent("Posted by graham on 3 Feb 2009 at 12:01AM"), "A page should contain 'Posted by graham ...'");
	   assertTrue(selenium.isTextPresent("Language: JavaScript"), "A page should contain 'Language: JavaScript'");
	   assertTrue(selenium.isTextPresent("var e = document.ge"), "A page should contain the code fragment found");
	   assertFalse(selenium.isElementPresent(ACTIVE_FIRST_PAGE_LINK)); //assert that only one record was found
   }
   
   @Test(dependsOnMethods={"exactSearchTest"})
   public void searchAndPaginationTest()
   {
	   waitHttp(selenium).click(HISTORY_LINK);
	   selenium.type(USER_SEARCH_INPUT, "martin");
	   selenium.select(SYNTAX_SELECT, ANY_SYNTAX);
	   selenium.type(DATE_SEARCH_INPUT, "");
	   selenium.type(CODE_SEARCH_INPUT, "");
	   waitHttp(selenium).click(SEARCH_BUTTON);
	   assertTrue(selenium.isTextPresent("Posted by martin on 15 Feb 2009 at 12:01AM"), "A page should contain 'Posted by martin ...'");
	   waitHttp(selenium).click(SECOND_PAGE_LINK);
	   assertTrue(selenium.isTextPresent("Posted by martin on 1 Feb 2009 at 12:01AM"), "A page should contain 'Posted by martin ...'");
	   waitHttp(selenium).click(THIRD_PAGE_LINK);
	   assertTrue(selenium.isTextPresent("Posted by martin on 9 Jan 2009 at 12:01AM"), "A page should contain 'Posted by martin ...'");
	   assertTrue(selenium.isTextPresent("function build_calendar($month,$year,$dateArray)"), "A page should contain code fragment");
   }
   
   @Test
   public void helpPageTest()
   {
	   waitHttp(selenium).click(HELP_LINK);
	   assertTrue(selenium.isTextPresent("Useful Information"), "A page should contain help information");
	   assertTrue(selenium.isTextPresent("Weld Features Covered"), "A page should contain 'Weld Features Covered'");
   }
   
   private boolean isLocationCorrect(URL url, boolean privatePost)
   {
	   String regexp;

	   if (!privatePost)
	   {
		   regexp = "[0-9]{1,}";
	   }
	   else
	   {
		   regexp = "[a-z0-9]{6}";
	   }
	   
	   return url.getPath().substring("/weld-pastecode/".length()).matches(regexp);
   }
   
   private boolean isDownloadWorking(AjaxSelenium s, XpathLocator xp, String textToFind)
   {
	   AttributeLocator<XpathLocator> al = xp.getAttribute(Attribute.HREF);
	   try 
	   {
		   URL downloadUrl = new URL(contextPath + s.getAttribute(al));
		   System.out.println(downloadUrl);
		   BufferedReader r = new BufferedReader(new InputStreamReader(downloadUrl.openStream()));
		   String str;
		   StringBuffer sb = new StringBuffer();
		   while ((str = r.readLine()) != null)
		   {
			   sb.append(str);
		   }
		   System.out.println(sb.toString());
		   return sb.toString().contains(textToFind);
	   } 
	   catch (IOException e) 
	   {
		   return false;
	   } 
   }
   
   protected String CODE_FRAGMENT =      
      "function addleadingzeros(array) {" + "\n" +
      "var highestint = Math.max.apply(Math,array);" + "\n" +
      "for(var i=0; i<array.length; i++) {" +  "\n" +
      "var nleadingzeros = highestint.toString().length - array[i].toString().length;" + "\n" +
      "for(var j=0; j<nleadingzeros; j++) array[i] = '0' + array[i];" + "\n" +
      "}" + "\n" +
      "return array;" + "\n" +
      "}";
   
   protected String CODE_FRAGMENT_PART = CODE_FRAGMENT.substring(0,30);
}
