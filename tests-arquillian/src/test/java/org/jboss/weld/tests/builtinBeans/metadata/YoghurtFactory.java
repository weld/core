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
package org.jboss.weld.tests.builtinBeans.metadata;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;

@ApplicationScoped
public class YoghurtFactory {

    private Bean<?> fruitYoghurtBean;
    private Bean<?> probioticYoghurtBean;
    private final List<Bean<?>> beans = new ArrayList<Bean<?>>();

    @Produces
    @Fruit
    public Yoghurt produce1(Bean<Yoghurt> bean) {
        fruitYoghurtBean = bean;
        return new Yoghurt();
    }

    @Produces
    @Probiotic
    public Yoghurt produce2(Bean<Yoghurt> bean) {
        probioticYoghurtBean = bean;
        return new Yoghurt();
    }

    public void dispose(@Disposes @Any Yoghurt yoghurt, Bean<?> bean) {
        beans.add(bean);
    }

    public Bean<?> getFruitYoghurtBean() {
        return fruitYoghurtBean;
    }

    public Bean<?> getProbioticYoghurtBean() {
        return probioticYoghurtBean;
    }

    public List<Bean<?>> getBeans() {
        return beans;
    }
}
