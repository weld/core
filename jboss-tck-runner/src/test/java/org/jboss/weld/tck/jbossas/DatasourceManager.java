/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tck.jbossas;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.dmr.ModelNode;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Sanity saving measure that forces the TCK to fail early if java:/DefaultDS does not exist.
 * Otherwise the TCK will get 90% of the way through and then hang.
 *
 * @author Stuart Douglas
 */
public class DatasourceManager implements ITestListener {
    private final static String JNDI_NAME = "java:/DefaultDS";

    private volatile boolean dataSourceChecked = false;

    public void onTestStart(ITestResult testResult) {
        if (dataSourceChecked) {
            return;
        }
        dataSourceChecked = true;

        final boolean create = Boolean.getBoolean("jboss.datasource.add");

        if (!create) {
            return;
        }

        String test = System.getProperty(SingleTestMethodListener.TEST_CLASS_PROPERTY);

        try {

            ModelControllerClient client = ModelControllerClient.Factory.create("localhost", 9999);
            ModelNode request = new ModelNode();
            request.get("operation").set("read-resource");
            request.get("address").get("subsystem").set("naming");
            // request.get("address").set("subsystem", "threads");
            request.get("recursive").set(false);
            ModelNode r = client.execute(new OperationBuilder(request).build());
            boolean found = false;

            ModelNode resultNode = r.get("result");
            if (resultNode.hasDefined("binding")) {
                for (ModelNode dataSource : resultNode.get("binding").asList()) {
                    if (dataSource.asProperty().getName().equals(JNDI_NAME)) {
                        found = true;
                    }
                }
            }
            if (!found) {
                if (create) {
                    request = new ModelNode();
                    request.get("address").add("subsystem", "naming");
                    request.get("address").add("binding", JNDI_NAME);
                    request.get("operation").set("add");
                    request.get("lookup").set("java:jboss/datasources/ExampleDS");
                    request.get("binding-type").set("lookup");
                    ModelNode result = client.execute(new OperationBuilder(request).build());
                    if (!result.get("outcome").asString().equals("success")) {
                        throw new RuntimeException("DataSource java:/DefaultDS was not found and could not be created automatically: " + result);
                    }

                } else {
                    if (test != null && !(test.length() == 0)) {
                        //we do not worry about this if we are only running one test
                        return;
                    }
                    throw new RuntimeException("DataSource java:/DefaultDS was not found. This DataSource must be defined, or the TCK will hang half way through due to missing MSC dependencies. To create this DataSource automatically run the TCK with -Djboss.datasource.add=true");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void onTestSuccess(ITestResult result) {
    }

    public void onTestFailure(ITestResult result) {
    }

    public void onTestSkipped(ITestResult result) {
    }

    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    }

    public void onStart(ITestContext context) {

    }

    public void onFinish(ITestContext context) {
    }
}
