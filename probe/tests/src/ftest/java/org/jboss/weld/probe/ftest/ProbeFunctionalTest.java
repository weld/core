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

import static org.junit.Assert.assertTrue;

import static org.jboss.arquillian.graphene.Graphene.guardAjax;
import static org.jboss.arquillian.graphene.Graphene.guardNoRequest;
import static org.jboss.arquillian.graphene.Graphene.waitAjax;
import static org.jboss.arquillian.graphene.Graphene.waitModel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Reception;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Model;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
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
import org.openqa.selenium.support.FindBy;

/**
 * @author Tomas Remes
 * @author Matej Novotny
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ProbeFunctionalTest {

    protected static final String PROBE = "weld-probe";
    protected static final String ARCHIVE_NAME = "probe-ftest";
    protected static final String SERVLET_INVOKED = "GET /probe-ftest/test";

    @Drone
    WebDriver driver;

    @ArquillianResource
    private URL contextPath;

    @Page
    private PageFragment page;
    
    @FindBy(className = "form-control-static")
    List<WebElement> listOfTargetElements;
    
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
        // By default you land on Dashboard tab
        driver.navigate().to(new URL(contextPath.toString() + PROBE));
        waitModel().until().element(page.getBeanArchivesTab()).is().present();
    }

    @Test
    public void testBeanArchiveDetail() {
        page.getBeanArchivesTab().click();
        waitAjax(driver).until().element(By.partialLinkText(ARCHIVE_NAME)).is().visible();
        guardNoRequest(driver.findElement(By.partialLinkText(ARCHIVE_NAME))).click();
        assertTrue(listOfTargetElements.stream().anyMatch(webElement -> webElement.getText().equals(BeanDiscoveryMode.ALL.name())));
        assertTrue(listOfTargetElements.stream().anyMatch(webElement -> webElement.getText().contains(InvocationMonitor.class.getName())));
        assertTrue(listOfTargetElements.stream().anyMatch(webElement -> webElement.getText().contains(ARCHIVE_NAME)));
    }

    @Test
    public void testBeanDetail() {
        guardAjax(page.getBeansTab()).click();
        String className = ModelBean.class.getSimpleName();
        waitAjax(driver).until().element(By.partialLinkText(className)).is().visible();
        WebElement modelBeanLink = driver.findElement(By.partialLinkText(className));
        assertTrue("Cannot find element for " + className, modelBeanLink.isDisplayed());
        guardAjax(modelBeanLink).click();
        assertTrue(listOfTargetElements.stream().anyMatch(webElement -> webElement.getText().equals(ModelBean.class.getName())));
        assertTrue(listOfTargetElements.stream().anyMatch(webElement -> webElement.getText().equals("@" + RequestScoped.class.getSimpleName())));
        assertTrue(listOfTargetElements.stream().anyMatch(webElement -> webElement.getText().equals(JSONTestUtil.BeanType.MANAGED.name())));
        assertTrue(listOfTargetElements.stream().anyMatch(webElement -> webElement.getText().equals(Model.class.getName())));
    }

    @Test
    public void testObserverMethodDetail() {
        page.getObserversTab().click();
        waitAjax().until().element(By.xpath("//h1[text()='Observer Methods']")).is().visible();
        WebElement observerLink = driver.findElement(By.partialLinkText(ApplicationScopedObserver.class.getSimpleName()));
        assertTrue("Cannot find element for " + ApplicationScopedObserver.class.getSimpleName(), observerLink.isDisplayed());
        guardAjax(observerLink).click();
        assertTrue(listOfTargetElements.stream().anyMatch(webElement -> webElement.getText().equals(ApplicationScopedObserver.class.getName())));
        assertTrue(listOfTargetElements.stream().anyMatch(webElement -> webElement.getText().equals("@" + ApplicationScoped.class.getSimpleName())));
        assertTrue(listOfTargetElements.stream().anyMatch(webElement -> webElement.getText().equals(JSONTestUtil.BeanType.MANAGED.name())));
        assertTrue(listOfTargetElements.stream().anyMatch(webElement -> webElement.getText().equals("@" + Default.class.getSimpleName())));
        assertTrue(listOfTargetElements.stream().anyMatch(webElement -> webElement.getText().contains(Reception.ALWAYS.name())));
        assertTrue(listOfTargetElements.stream().anyMatch(webElement -> webElement.getText().contains(Reception.IF_EXISTS.name())));
    }

    @Test
    public void testMonitoringSessionScopeContext() throws MalformedURLException {
        invokeServletAndReturnToProbeClient();

        page.getMonitoringTab().click();
        waitAjax(driver).until().element(page.getSessionScopedContext()).is().visible();
        guardAjax(page.getSessionScopedContext()).click();
        WebElement sesionScopedBean = driver.findElement(By.partialLinkText(SessionScopedBean.class.getSimpleName()));
        assertTrue("Cannot find element for " + SessionScopedBean.class.getSimpleName(), sesionScopedBean.isDisplayed());
    }

    @Test
    public void testMonitoringInvocationTree() throws MalformedURLException {
        invokeServletAndReturnToProbeClient();
        
        page.getMonitoringTab().click();
        waitAjax(driver).until().element(page.getInvocationTrees()).is().visible();
        guardAjax(page.getInvocationTrees()).click();
        waitAjax(driver).until().element(By.xpath("//h1[text()='Invocation Trees']")).is().visible();
        List<WebElement> invocationTableValues = driver.findElements(By.tagName("td"));
        assertTrue(invocationTableValues.stream().anyMatch(webElement -> webElement.getText().equals(ProbeFilter.class.getName())));
        assertTrue(invocationTableValues.stream().anyMatch(webElement -> webElement.getText().equals(SERVLET_INVOKED)));
    }

    private void invokeServletAndReturnToProbeClient() throws MalformedURLException {
        driver.navigate().to(new URL(contextPath.toString() + "test"));
        driver.navigate().to(new URL(contextPath.toString() + PROBE));
        waitModel().until().element(page.getBeanArchivesTab()).is().present();
    }
}
