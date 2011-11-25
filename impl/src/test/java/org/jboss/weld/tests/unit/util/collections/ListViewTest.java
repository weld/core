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
package org.jboss.weld.tests.unit.util.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.jboss.weld.util.collections.ViewProvider;
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
        protected ViewProvider<Student, String> getViewProvider() {
            return new ViewProvider<Student, String>() {
                @Override
                public String toView(Student from) {
                    return from.getName();
                }

                @Override
                public Student fromView(String to) {
                    return new Student(to, -1);
                }
            };
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
        ListIterator<String> i = view.listIterator();
        assertTrue(i.hasNext());
        assertFalse(i.hasPrevious());
        assertEquals("Foo", i.next());
        assertTrue(i.hasNext());
        assertTrue(i.hasPrevious());
        assertEquals("Bar", i.next());
        assertFalse(i.hasNext());
        assertTrue(i.hasPrevious());
        assertEquals("Bar", i.previous());
        assertEquals("Foo", i.previous());
        i.remove();
        assertEquals(1, list.size());
    }
}
