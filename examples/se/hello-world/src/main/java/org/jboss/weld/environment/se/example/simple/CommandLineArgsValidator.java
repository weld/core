/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.se.example.simple;

import org.jboss.weld.environment.se.bindings.Parameters;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Validates command line arguments, producing errors where applicable.
 *
 * @author Peter Royle
 */
@ApplicationScoped
public class CommandLineArgsValidator {

    @Inject
    private
    @Parameters
    List<String> validParams;
    private List<String> errors = new ArrayList<String>();

    public CommandLineArgsValidator() {
    }

    @Inject
    public void checkParameters() {
        if (validParams.size() != 1) {
            errors.add("Please supply just one parameter: your first name");
            validParams = Collections.emptyList();
        }
    }

    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getValidParameters() {
        return validParams;
    }
}
