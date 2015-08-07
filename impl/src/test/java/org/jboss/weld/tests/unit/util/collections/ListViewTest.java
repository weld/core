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
package org.jboss.weld.tests.unit.util.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.jboss.weld.util.collections.ListView;
import org.junit.Test;

public class ListViewTest {

    private List<Student> list;
    private List<String> view = new ListView<Student, String>() {

        @Override
        protected List<Student> getDelegate() {
            return list;
        }

        @Override
        protected String toView(Student source) {
            return source.getName();
        }

        @Override
        protected Student createSource(String view) {
            return new Student(view, -1);
        }

    };

    @Test
    public void testListView() {
        list = new ArrayList<Student>();
        assertTrue(view.isEmpty());
        assertEquals(0, view.size());
        // add to list
        list.add(new Student("Jozef", 23));
        assertEquals(1, view.size());
        assertEquals("Jozef", view.get(0));
        // add to view
        view.add("Martin");
        assertEquals(2, view.size());
        assertEquals(2, list.size());
        assertEquals("Jozef", view.get(0));
        assertEquals("Martin", view.get(1));
        assertEquals("Jozef", list.get(0).getName());
        assertEquals("Martin", list.get(1).getName());
        // add to specific position
        view.add(1, "Marek");
        assertEquals(3, view.size());
        assertEquals(3, list.size());
        assertEquals("Jozef", view.get(0));
        assertEquals("Marek", view.get(1));
        assertEquals("Martin", view.get(2));
        assertEquals("Jozef", list.get(0).getName());
        assertEquals("Marek", list.get(1).getName());
        assertEquals("Martin", list.get(2).getName());
        // remove from view
        view.remove("Jozef");
        assertEquals(2, view.size());
        assertEquals(2, list.size());
        assertEquals("Marek", view.get(0));
        assertEquals("Martin", view.get(1));
        assertEquals("Marek", list.get(0).getName());
        assertEquals("Martin", list.get(1).getName());
        // remote from view - index
        view.remove(1);
        assertEquals(1, view.size());
        assertEquals(1, list.size());
        assertEquals("Marek", view.get(0));
        assertEquals("Marek", list.get(0).getName());
    }

    @Test
    public void testIterator() {
        list = new ArrayList<Student>();
        list.add(new Student("Foo", 1));
        list.add(new Student("Bar", 2));
        ListIterator<String> listIterator = view.listIterator();
        assertTrue(listIterator.hasNext());
        assertFalse(listIterator.hasPrevious());
        assertEquals("Foo", listIterator.next());
        assertTrue(listIterator.hasNext());
        assertTrue(listIterator.hasPrevious());
        assertEquals("Bar", listIterator.next());
        assertFalse(listIterator.hasNext());
        assertTrue(listIterator.hasPrevious());
        assertEquals("Bar", listIterator.previous());
        assertEquals("Foo", listIterator.previous());
        listIterator.remove();
        assertEquals(1, list.size());
        listIterator.add("Baz");
        assertEquals(2, list.size());
        assertEquals("Baz", view.get(0));
    }

}
