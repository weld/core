/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.servlet.dispatch;

import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TestBean {

    private int constructions;
    private int destructions;
    private Phaser phaser;

    public void constructed() {
        constructions++;
        phaser.register();
    }

    public void destroyed() {
        destructions++;
        phaser.arriveAndDeregister();
    }

    public boolean isOk() {
        try {
            // either the phaser has already reached stability (phase 0 and terminated) or we wait for it
            phaser.awaitAdvanceInterruptibly(0, 2l, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            throw new IllegalStateException("Waiting for Phaser stability failed, exception throws was: " + e);
        }
        return (constructions == destructions) && (constructions + destructions > 0);
    }

    public void reset() {
        constructions = destructions = 0;
        phaser = new Phaser();
    }

    public int getConstructions() {
        return constructions;
    }

    public int getDestructions() {
        return destructions;
    }

}
