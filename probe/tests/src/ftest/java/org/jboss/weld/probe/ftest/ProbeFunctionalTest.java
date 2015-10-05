/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.probe.ftest;

import static junit.framework.Assert.assertTrue;
import static org.jboss.arquillian.graphene.Graphene.guardAjax;
import static org.jboss.arquillian.graphene.Graphene.guardNoRequest;
import static org.jboss.arquillian.graphene.Graphene.waitModel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Reception;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Model;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.probe.InvocationMonitor;
import org.jboss.weld.probe.ProbeFilter;
import org.jboss.weld.probe.tests.integration.JSONTestUtil;
import org.jboss.weld.probe.tests.integration.ProbeBeansTest;
import org.jboss.weld.probe.tests.integration.deployment.InvokingServlet;
import org.jboss.weld.probe.tests.integration.deployment.annotations.Collector;
import org.jboss.weld.probe.tests.integration.deployment.beans.ApplicationScopedObserver;
import org.jboss.weld.probe.tests.integration.deployment.beans.ModelBean;
import org.jboss.weld.probe.tests.integration.deployment.beans.SessionScopedBean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * @author Tomas Remes
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ProbeFunctionalTest {

    protected static final String PROBE = "weld-probe";
    protected static final String ARCHIVE_NAME = "probe-ftest";
    protected static final String SERVLET_INVOKED = "GET /probe-ftest/test";
    protected By BEAN_ARCHIVES = By.linkText("Bean Archives");
    protected By BEANS = By.linkText("Beans");
    protected By MODEL_BEAN_LINK = By.partialLinkText(ModelBean.class.getSimpleName());
    protected By FORM_CONTROL_CLASS = By.className("form-control-static");
    protected By ARCHIVE_LINK = By.partialLinkText(ARCHIVE_NAME);
    protected By EVENTS_LINK = By.partialLinkText("Events");
    protected By OBSERVER_METHODS_LINK = By.partialLinkText("Observer Methods");
    protected By OBSERVER_LINK = By.partialLinkText(ApplicationScopedObserver.class.getSimpleName());
    protected By CONTEXTS_LINK = By.linkText("Contexts");
    protected By SESSIONSCOPED_LINK = By.partialLinkText(SessionScoped.class.getSimpleName());
    protected By SESSIONSCOPEDBEAN_LINK = By.partialLinkText(SessionScopedBean.class.getSimpleName());
    protected By INVOCATIONS_TREES_LINK = By.linkText("Invocation Trees");
    protected By TD_TAG = By.tagName("td");

    @Drone
    WebDriver driver;

    @ArquillianResource
    private URL contextPath;

    @Deployment(testable = false)
    public static WebArchive createTestDeployment1() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class, ARCHIVE_NAME + ".war")
                .addAsWebInfResource(ProbeBeansTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(ProbeBeansTest.class.getPackage(), "beans.xml", "beans.xml")
                .addPackage(ModelBean.class.getPackage())
                .addPackage(Collector.class.getPackage())
                .addClass(InvokingServlet.class);
        return webArchive;
    }

    @Before
    public void openStartUrl() throws MalformedURLException {
        driver.navigate().to(new URL(contextPath.toString() + PROBE));
        waitModel().until().element(BEAN_ARCHIVES).is().present();
    }

    @Test
    public void testBeanArchiveDetail() {
        guardNoRequest(driver.findElement(ARCHIVE_LINK)).click();
        List<WebElement> beanArchiveAttributes = driver.findElements(FORM_CONTROL_CLASS);
        assertTrue(checkListContainsElementWithText(beanArchiveAttributes, BeanDiscoveryMode.ALL.name()));
        assertTrue(checkListContainsElementWithPartialText(beanArchiveAttributes, InvocationMonitor.class.getName()));
        assertTrue(checkListContainsElementWithPartialText(beanArchiveAttributes, ARCHIVE_NAME));
    }

    @Test
    public void testBeanDetail() {
        guardAjax(driver.findElement(BEANS)).click();
        WebElement modelBeanLink = driver.findElement(MODEL_BEAN_LINK);
        assertTrue("Cannot find element for " + MODEL_BEAN_LINK.toString(), modelBeanLink.isDisplayed());
        guardAjax(modelBeanLink).click();
        List<WebElement> modelBeanAttributes = driver.findElements(FORM_CONTROL_CLASS);
        assertTrue(checkListContainsElementWithText(modelBeanAttributes, ModelBean.class.getName()));
        assertTrue(checkListContainsElementWithText(modelBeanAttributes, "@" + RequestScoped.class.getSimpleName()));
        assertTrue(checkListContainsElementWithText(modelBeanAttributes, JSONTestUtil.BeanType.MANAGED.name()));
        assertTrue(checkListContainsElementWithText(modelBeanAttributes, Model.class.getName()));
    }

    @Test
    public void testObserverMethodDetail() {
        guardNoRequest(driver.findElement(EVENTS_LINK)).click();
        guardAjax(driver.findElement(OBSERVER_METHODS_LINK)).click();
        WebElement observerLink = driver.findElement(OBSERVER_LINK);
        assertTrue("Cannot find element for " + OBSERVER_LINK.toString(), observerLink.isDisplayed());
        guardAjax(observerLink).click();
        List<WebElement> observerDetailAttributes = driver.findElements(FORM_CONTROL_CLASS);
        assertTrue(checkListContainsElementWithText(observerDetailAttributes, ApplicationScopedObserver.class.getName()));
        assertTrue(checkListContainsElementWithText(observerDetailAttributes, "@" + ApplicationScoped.class.getSimpleName()));
        assertTrue(checkListContainsElementWithText(observerDetailAttributes, JSONTestUtil.BeanType.MANAGED.name()));
        assertTrue(checkListContainsElementWithText(observerDetailAttributes, "@" + Default.class.getSimpleName()));
        assertTrue(checkListContainsElementWithPartialText(observerDetailAttributes, Reception.ALWAYS.name()));
        assertTrue(checkListContainsElementWithPartialText(observerDetailAttributes, Reception.IF_EXISTS.name()));
    }

    @Test
    public void testContextsView() throws MalformedURLException {
        invokeServletAndReturnToProbeClient();

        guardNoRequest(driver.findElement(CONTEXTS_LINK)).click();
        guardAjax(driver.findElement(SESSIONSCOPED_LINK)).click();
        WebElement sesionScopedBeanLink = driver.findElement(SESSIONSCOPEDBEAN_LINK);
        assertTrue("Cannot find element for " + SESSIONSCOPEDBEAN_LINK.toString(), sesionScopedBeanLink.isDisplayed());
    }

    @Test
    public void testInvocationTreeView() throws MalformedURLException {
        invokeServletAndReturnToProbeClient();
        guardAjax(driver.findElement(INVOCATIONS_TREES_LINK)).click();
        List<WebElement> invocationTableValues = driver.findElements(TD_TAG);
        assertTrue(checkListContainsElementWithText(invocationTableValues, ProbeFilter.class.getName()));
        assertTrue(checkListContainsElementWithText(invocationTableValues, SERVLET_INVOKED));
    }

    private void invokeServletAndReturnToProbeClient() throws MalformedURLException {
        driver.navigate().to(new URL(contextPath.toString() + "test"));
        driver.navigate().to(new URL(contextPath.toString() + PROBE));
        waitModel().until().element(BEAN_ARCHIVES).is().present();
    }

    private boolean checkListContainsElementWithText(List<WebElement> elements, String text) {
        boolean contains = false;
        for (WebElement element : elements) {
            if (element.getText().equals(text)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    private boolean checkListContainsElementWithPartialText(List<WebElement> elements, String text) {
        boolean contains = false;
        for (WebElement element : elements) {
            if (element.getText().contains(text)) {
                contains = true;
                break;
            }
        }
        return contains;
    }
}
