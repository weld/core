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

package org.jboss.weld.atinject.tck;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Unmanaged;
import org.atinject.tck.auto.Drivers;
import org.atinject.tck.auto.DriversSeat;
import org.atinject.tck.auto.Seat;
import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.accessories.Cupholder;
import org.atinject.tck.auto.accessories.SpareTire;

import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Named;
import jakarta.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Producer methods for the @Inject TCK beans we need greater control over
 *
 * @author pmuir
 */
public class Producers {

    /**
     * Producer method for a bean with qualifier @Drivers and types Seat, Object
     *
     * @return
     */
    @Produces
    @Drivers
    public Seat produceAtDriversSeat(DriversSeat driversSeat) {
        return driversSeat;
    }

    /**
     * Producer method for a bean with default qualifiers and type DriversSeat only
     *
     * @return
     */
    @Produces
    @Typed(DriversSeat.class)
    public DriversSeat produceDriversSeat(BeanManager manager) {
        // TODO this used to use @New to create the instance, do this in a cleaner way
        Unmanaged<DriversSeat> driversSeatUnmanaged = new Unmanaged<>(manager, DriversSeat.class);
        return driversSeatUnmanaged.newInstance().produce().inject().postConstruct().get();
    }

    @Qualifier
    @Retention(RUNTIME)
    @Target({TYPE, METHOD, FIELD, PARAMETER})
    @Documented
    private @interface Spare {

    }

    /**
     * Producer method for a bean with qualifier @Named("spare") and types Tire, Object.
     * <p/>
     * Use the @Spare qualifier to stop @Default being applied
     */
    @Produces
    @Named("spare")
    @Spare
    public Tire produceAtNamedSpareTire(SpareTire spareTire) {
        return spareTire;
    }

    /**
     * Producer method for bean with default qualifiers and type SpareTire only
     */
    @Produces
    @Typed(SpareTire.class)
    public SpareTire produceSpareTire(BeanManager manager) {
        // TODO this used to use @New to create the instance, do this in a cleaner way
        Unmanaged<SpareTire> spareTireUnmanaged = new Unmanaged<>(manager, SpareTire.class);
        return spareTireUnmanaged.newInstance().produce().inject().postConstruct().get();
    }

}
