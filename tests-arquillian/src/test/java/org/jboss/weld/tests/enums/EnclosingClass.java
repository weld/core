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
