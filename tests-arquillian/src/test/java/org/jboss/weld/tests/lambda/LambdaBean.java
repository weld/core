/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.lambda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;

/**
 *
 * @author Martin Kouba
 */
@Dependent
public class LambdaBean {

    private final int limit = 5;

    public List<Integer> ping() {
        List<Integer> data = new ArrayList<Integer>();
        data.add(1);
        data.add(10);
        data.add(5);
        Collections.sort(data, (d1, d2) -> d1.compareTo(d2));
        return data;
    }

    public List<String> pingStream() {
        List<Integer> data = new ArrayList<Integer>();
        data.add(1);
        data.add(10);
        data.add(5);
        return data.stream().sorted((d1, d2) -> d1.compareTo(d2)).map(i -> i.toString()).collect(Collectors.toList());
    }

    public List<Integer> lambdaAsInstanceMethod() {
        List<Integer> data = new ArrayList<Integer>();
        data.add(1);
        data.add(10);
        data.add(5);
        return data.stream().sorted((d1, d2) -> d1.compareTo(d2)).map(d -> plusOne(d)).filter(d -> d > limit)
                .collect(Collectors.toList());
    }

    public List<Integer> lambdaAsInstanceMethod02() {
        List<Integer> data = new ArrayList<Integer>();
        data.add(1);
        data.add(10);
        data.add(5);
        return data.stream().sorted((d1, d2) -> d1.compareTo(d2)).map(d -> toNull(d)).collect(Collectors.toList());
    }

    private int plusOne(int value) {
        return value++;
    }

    private <E extends Number> E toNull(E value) {
        return null;
    }

}
