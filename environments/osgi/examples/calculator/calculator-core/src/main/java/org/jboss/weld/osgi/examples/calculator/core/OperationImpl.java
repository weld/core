/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.weld.osgi.examples.calculator.core;

import org.jboss.weld.osgi.examples.calculator.api.Operation;
import org.jboss.weld.osgi.examples.calculator.api.Operator;

public class OperationImpl implements Operation {

    private Operator operator;

    private int value1;

    private int value2;

    private boolean v1 = false;
    private boolean v2 = false;
    private boolean op = false;


    @Override
    public Operator getOperator() {
        return operator;
    }

    @Override
    public void setOperator(Operator operator) {
        this.op = true;
        this.operator = operator;
    }

    @Override
    public int getValue1() {
        return value1;
    }

    @Override
    public void setValue1(int value1) {
        this.v1 = true;
        this.value1 = value1;
    }

    @Override
    public int getValue2() {
        return value2;
    }

    @Override
    public void setValue2(int value2) {
        this.v2 = true;
        this.value2 = value2;
    }

    @Override
    public int value() {
        return operator.value(value1, value2);
    }

    @Override
    public boolean isValue1Set() {
        return v1;
    }

    @Override
    public boolean isValue2Set() {
        return v2;
    }

    @Override
    public boolean isOperatorSet() {
        return op;
    }
}
