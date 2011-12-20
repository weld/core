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
package org.jboss.weld.tests.enums;

import javax.inject.Inject;

public class EnclosingClass {

    public enum AdvancedEnum {
        FOO() {

            @Inject
            @Persian
            private Cat subclassCat;

            private Dog abstractInitializerDog;
            private Dog subclassInitializerDog;

            @Inject
            @Override
            public void abstractInitializer(Dog dog) {
                this.abstractInitializerDog = dog;
            }

            @Inject
            @SuppressWarnings("unused")
            public void subclassInitializer(Dog dog) {
                this.subclassInitializerDog = dog;
            }

            @Override
            public Cat getSubclassCat() {
                return subclassCat;
            }

            @Override
            public Dog getInitializerAbstractDog() {
                return abstractInitializerDog;
            }

            @Override
            public Dog getSubclassDog() {
                return subclassInitializerDog;
            };
        },

        BAR() {

            @Inject
            @Persian
            private Cat subclassCat;

            private Dog abstractInitializerDog;
            private Dog subclassInitializerDog;

            @Inject
            @Override
            public void abstractInitializer(Dog dog) {
                this.abstractInitializerDog = dog;
            }

            @Inject
            @SuppressWarnings("unused")
            public void subclassInitializer(Dog dog) {
                this.subclassInitializerDog = dog;
            }

            @Override
            public Cat getSubclassCat() {
                return subclassCat;
            }

            @Override
            public Dog getInitializerAbstractDog() {
                return abstractInitializerDog;
            }

            @Override
            public Dog getSubclassDog() {
                return subclassInitializerDog;
            };
        };

        @Inject
        @Persian
        private Cat superclassCat;
        private Dog superclassDog;

        @Inject
        public void superclassInitializer(Dog dog) {
            this.superclassDog = dog;
        }

        public Cat getSuperclassCat() {
            return superclassCat;
        }

        public Dog getSuperclassDog() {
            return superclassDog;
        }

        public abstract void abstractInitializer(Dog dog);

        public abstract Dog getInitializerAbstractDog();

        public abstract Cat getSubclassCat();

        public abstract Dog getSubclassDog();
    }
}
