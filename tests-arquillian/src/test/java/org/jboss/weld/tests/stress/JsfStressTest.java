/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.weld.tests.stress;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.tests.category.Broken;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * Stress test of basically the JSF NumberGuess game example.
 *
 * @author David Allen
 *
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
@RunAsClient
public class JsfStressTest
{
   protected final String MAIN_PAGE = "/home.jsf";
   protected final String GUESS_MESSAGES = "numberGuess:messages";

   protected final String GUESS_FIELD = "numberGuess:inputGuess";
   protected final String GUESS_SUBMIT = "numberGuess:guessButton";
   protected final String GUESS_RESET = "numberGuess:restartButton";
   protected final String GUESS_SMALLEST = "numberGuess:smallest";
   protected final String GUESS_BIGGEST = "numberGuess:biggest";

   protected String WIN_MSG = "Correct!";
   protected String LOSE_MSG = "No guesses left!";

   @Rule
   public ContiPerfRule i = new ContiPerfRule();

   @Deployment
   public static WebArchive createDeployment()
   {
      return ShrinkWrap.create(WebArchive.class, "test.war")
               .addClasses(Game.class, Generator.class, MaxNumber.class, Random.class)
               .addAsWebResource(JsfStressTest.class.getPackage(), "web.xml", "web.xml")
               .addAsWebResource(JsfStressTest.class.getPackage(), "faces-config.xml", "faces-config.xml")
               .addAsResource(JsfStressTest.class.getPackage(), "home.xhtml", "home.xhtml")
               .addAsResource(JsfStressTest.class.getPackage(), "index.html", "index.html")
               .addAsResource(JsfStressTest.class.getPackage(), "template.xhtml", "template.xhtml")
               .addAsWebResource(EmptyAsset.INSTANCE, "beans.xml");
   }

   // WELD-676
   @Category(Broken.class)
   @Test
   @PerfTest(invocations = 500)
   public void testJsfApp() throws Exception
   {
      WebClient client = new WebClient();
      client.setThrowExceptionOnFailingStatusCode(false);
      HtmlPage page = client.getPage(getPath(MAIN_PAGE));

      int min;
      int max;
      int guess;
      int i = 0;

      while (isOnGuessPage(page))
      {
         Assert.assertTrue("Game should not be longer than 10 guesses", i <= 10);

         min = Integer.parseInt(getSpanValue(page, GUESS_SMALLEST));
         max = Integer.parseInt(getSpanValue(page, GUESS_BIGGEST));
         guess = min + ((max - min) / 2);
         page = enterGuess(page, guess);
         i++;
      }
      reset(page);
      Assert.assertTrue("Win page expected after playing smart.", isOnWinPage(page));

      client.closeAllWindows();
   }

   protected void reset(HtmlPage page) throws IOException
   {
      ((HtmlSubmitInput)page.getElementById(GUESS_RESET)).click();
   }

   protected HtmlPage enterGuess(HtmlPage page, int guess) throws IOException
   {
      ((HtmlInput)page.getElementById(GUESS_FIELD)).setValueAttribute(String.valueOf(guess));
      HtmlSubmitInput submitButton = (HtmlSubmitInput)page.getElementById(GUESS_SUBMIT);
      if (submitButton.isDisabled())
      {
         throw new RuntimeException("Guess button disabled on page: " + page.asText());
      }
      return submitButton.click();
   }

   protected boolean isOnGuessPage(HtmlPage page)
   {
      return !(isOnWinPage(page) || isOnLosePage(page));
   }

   protected boolean isOnWinPage(HtmlPage page)
   {
      String text = page.getElementById(GUESS_MESSAGES).asText();
      return WIN_MSG.equals(text);
   }

   protected boolean isOnLosePage(HtmlPage page)
   {
      String text = page.getElementById(GUESS_MESSAGES).asText();
      return LOSE_MSG.equals(text);
   }

   protected String getSpanValue(HtmlPage page, String fieldId)
   {
      HtmlSpan span = (HtmlSpan) page.getElementById(fieldId);
      return span.asText();
   }

   protected String getPath(String viewId)
   {
      // TODO: this should be moved out and be handled by Arquillian
      return "http://localhost:8080/test/";
   }


}
