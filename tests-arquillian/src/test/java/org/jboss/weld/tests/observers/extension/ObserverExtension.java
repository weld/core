/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.tests.observers.extension;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.literal.AnyLiteral;

public class ObserverExtension implements Extension {

    private GiraffeObserver anyGiraffeObserver = new GiraffeObserver(AnyLiteral.INSTANCE);
    private GiraffeObserver fiveMeterTallGiraffeObserver = new GiraffeObserver(Tall.Literal.FIVE_METERS);
    private GiraffeObserver sixMeterTallAngryGiraffeObserver = new GiraffeObserver(Tall.Literal.SIX_METERS, new Angry.Literal());
    private GiraffeObserver angryNubianGiraffeObserver = new GiraffeObserver(new Angry.Literal(), new Nubian.Literal());

    public void registerObservers(@Observes AfterBeanDiscovery event) {
        event.addObserverMethod(anyGiraffeObserver);
        event.addObserverMethod(fiveMeterTallGiraffeObserver);
        event.addObserverMethod(sixMeterTallAngryGiraffeObserver);
        event.addObserverMethod(angryNubianGiraffeObserver);
    }

    public GiraffeObserver getAnyGiraffeObserver() {
        return anyGiraffeObserver;
    }

    public GiraffeObserver getFiveMeterTallGiraffeObserver() {
        return fiveMeterTallGiraffeObserver;
    }

    public GiraffeObserver getSixMeterTallAngryGiraffeObserver() {
        return sixMeterTallAngryGiraffeObserver;
    }

    public GiraffeObserver getAngryNubianGiraffeObserver() {
        return angryNubianGiraffeObserver;
    }
}
