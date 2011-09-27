/**
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
package org.jboss.weld.environment.osgi.test.extension;

import org.jboss.weld.environment.osgi.api.annotation.Filter;
import org.jboss.weld.environment.osgi.api.annotation.Properties;
import org.jboss.weld.environment.osgi.api.annotation.Property;
import org.jboss.weld.environment.osgi.impl.extension.FilterGenerator;
import junit.framework.Assert;
import org.junit.Test;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;
import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osgi.application.Framework;
import static java.lang.annotation.ElementType.*;

public class FilterGeneratorTest {

    @Test
    //@Ignore
    public void filterTest() {

        Set<String> testSet = new HashSet<String>();

        Assert.assertEquals("makeFilter() was wrong",
                            "",
                            FilterGenerator.makeFilter().value());

        String tokA = "token1", tokB = "token2", tokC = "token2";
        Set<String> set1 = new HashSet<String>(),
                set2 = new HashSet<String>(),
                set3 = new HashSet<String>(),
                set0 = new HashSet<String>();
        set1.add(tokA);
        set2.add(tokA);
        set3.add(tokA);
        set2.add(tokB);
        set3.add(tokB);
        set3.add(tokC);

        Assert.assertEquals("make(set0) was wrong",
                            "",
                            FilterGenerator.make(set0).value());
        Assert.assertEquals("make(set1) was wrong",
                            "token1",
                            FilterGenerator.make(set1).value());
        Assert.assertEquals("make(set2) was wrong",
                            "(&token1token2)",
                            FilterGenerator.make(set2).value());
        Assert.assertEquals("make(set3) was wrong",
                            "(&token1token2)",
                            FilterGenerator.make(set3).value());

        Assert.assertEquals("makeFilter(tokA) was wrong",
                            "token1",
                            FilterGenerator.makeFilter(tokA).value());
        Assert.assertEquals("makeFilter(tokA + tokB) was wrong",
                            "token1token2",
                            FilterGenerator.makeFilter(tokA + tokB).value());

        FilterAnnotationLiteral emptyFilter = new FilterAnnotationLiteral("");
        FilterAnnotationLiteral filterAnnotationLiteral =
                                new FilterAnnotationLiteral("filterAnnotationLiteral");

        testSet.clear();
        Assert.assertEquals("makeFilter(emptyFilter,\"\") was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(emptyFilter, ""));
        testSet.clear();
        testSet.add(tokA);
        Assert.assertEquals("makeFilter(emptyFilter,tokA) was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(emptyFilter, tokA));
        testSet.clear();
        testSet.add(filterAnnotationLiteral.value());
        Assert.assertEquals("makeFilter(filterAnnotationLiteral,\"\") was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(filterAnnotationLiteral, ""));
        testSet.clear();
        testSet.add(tokA);
        testSet.add(filterAnnotationLiteral.value());
        Assert.assertEquals("makeFilter(filterAnnotationLiteral,tokA) was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(filterAnnotationLiteral, tokA));
        testSet.clear();
        testSet.add(filterAnnotationLiteral.value());
        Assert.assertEquals("makeFilter(filterAnnotationLiteral,"
                            + "filterAnnotationLiteral.value()) was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(filterAnnotationLiteral,
                                                       filterAnnotationLiteral.value()));

        PropertiesAnnotationLiteral emptyProperties = new PropertiesAnnotationLiteral();
        PropertiesAnnotationLiteral singleProperties =
                                    new PropertiesAnnotationLiteral(
                new PropertyAnnotationLiteral("name", "single"));
        PropertiesAnnotationLiteral multipleProperties =
                                    new PropertiesAnnotationLiteral(
                new PropertyAnnotationLiteral("name", "multiple1"),
                new PropertyAnnotationLiteral("name", "multiple2"),
                new PropertyAnnotationLiteral("name", "multiple3"));

        testSet.clear();
        Assert.assertEquals("makeFilter(emptyProperties) was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(emptyProperties));
        testSet.clear();
        testSet.add("(name=single)");
        Assert.assertEquals("makeFilter(singleProperties) was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(singleProperties));
        testSet.clear();
        testSet.add("(name=multiple1)");
        testSet.add("(name=multiple2)");
        testSet.add("(name=multiple3)");
        Assert.assertEquals("makeFilter(multipleProperties) was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(multipleProperties));

        NonQualifierAnnotation nonQualifierAnnotation =
                               new NonQualifierAnnotationLiteral();
        QualifierAnnotation qualifierAnnotation =
                            new QualifierAnnotationLiteral();
        QualifierWithValueAnnotation qualifierWithValueAnnotation =
                                     new QualifierWithValueAnnotationLiteral("QWVA");
        QualifierWithDefaultValueAnnotation qualifierWithDefaultValueAnnotation =
                                            new QualifierWithDefaultValueAnnotationLiteral();
        QualifierWithValuesAnnotation qualifierWithValuesAnnotation =
                                      new QualifierWithValuesAnnotationLiteral("QWVsA");

        List<Annotation> list1 = new ArrayList<Annotation>(),
                list2 = new ArrayList<Annotation>(),
                list3 = new ArrayList<Annotation>(),
                list4 = new ArrayList<Annotation>(),
                list5 = new ArrayList<Annotation>(),
                list6 = new ArrayList<Annotation>(),
                list7 = new ArrayList<Annotation>(),
                list8 = new ArrayList<Annotation>(),
                list0 = new ArrayList<Annotation>();
        list1.add(nonQualifierAnnotation);
        list2.add(qualifierAnnotation);
        list3.add(qualifierWithValueAnnotation);
        list4.add(qualifierWithDefaultValueAnnotation);
        list5.add(qualifierWithValuesAnnotation);
        list6.add(qualifierWithValueAnnotation);
        list6.add(qualifierWithValueAnnotation);
        list7.add(qualifierWithValueAnnotation);
        list7.add(qualifierWithDefaultValueAnnotation);
        list8.add(nonQualifierAnnotation);
        list8.add(qualifierAnnotation);
        list8.add(qualifierWithValueAnnotation);
        list8.add(qualifierWithDefaultValueAnnotation);
        list8.add(qualifierWithValuesAnnotation);

        testSet.clear();
        Assert.assertEquals("makeFilter(list0) was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(list0));
        testSet.clear();
        Assert.assertEquals("makeFilter(list1) was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(list1));
        testSet.clear();
        testSet.add("(qualifierannotation=*)");
        Assert.assertEquals("makeFilter(list2) was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(list2));
        testSet.clear();
        testSet.add("(qualifierwithvalueannotation.value=QWVA)");
        Assert.assertEquals("makeFilter(list3) was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(list3));
        testSet.clear();
        testSet.add("(qualifierwithdefaultvalueannotation.value=default)");
        Assert.assertEquals("makeFilter(list4) was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(list4));
        testSet.clear();
        testSet.add("(qualifierwithvaluesannotation.value=QWVsA)");
        testSet.add("(qualifierwithvaluesannotation.name=name)");
        Assert.assertEquals("makeFilter(list5) was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(list5));
        testSet.clear();
        testSet.add("(qualifierwithvalueannotation.value=QWVA)");
        Assert.assertEquals("makeFilter(list6) was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(list6));
        testSet.clear();
        testSet.add("(qualifierwithvalueannotation.value=QWVA)");
        testSet.add("(qualifierwithdefaultvalueannotation.value=default)");
        Assert.assertEquals("makeFilter(list7) was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(list7));
        testSet.clear();
        testSet.add("(qualifierwithvalueannotation.value=QWVA)");
        testSet.add("(qualifierwithdefaultvalueannotation.value=default)");
        testSet.add("(qualifierwithvaluesannotation.value=QWVsA)");
        testSet.add("(qualifierwithvaluesannotation.name=name)");
        testSet.add("(qualifierannotation=*)");
        Assert.assertEquals("makeFilter(list8) was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(list8));

        List<Annotation> list10 = new ArrayList<Annotation>(),
                list11 = new ArrayList<Annotation>(),
                list12 = new ArrayList<Annotation>(),
                list13 = new ArrayList<Annotation>();

        list10.add(multipleProperties);
        list11.add(filterAnnotationLiteral);
        list12.add(multipleProperties);
        list12.add(filterAnnotationLiteral);
        list13.add(multipleProperties);
        list13.add(filterAnnotationLiteral);
        list13.addAll(list8);

        testSet.clear();
        testSet.add("(name=multiple1)");
        testSet.add("(name=multiple2)");
        testSet.add("(name=multiple3)");
        Assert.assertEquals("makeFilter(list10) was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(list10));
        testSet.clear();
        testSet.add(filterAnnotationLiteral.value());
        Assert.assertEquals("makeFilter(list11) was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(list11));
        testSet.clear();
        testSet.add("(name=multiple1)");
        testSet.add("(name=multiple2)");
        testSet.add("(name=multiple3)");
        testSet.add(filterAnnotationLiteral.value());
        Assert.assertEquals("makeFilter(list12) was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(list12));
        testSet.clear();
        testSet.add("(qualifierwithvalueannotation.value=QWVA)");
        testSet.add("(qualifierwithdefaultvalueannotation.value=default)");
        testSet.add("(qualifierwithvaluesannotation.value=QWVsA)");
        testSet.add("(qualifierwithvaluesannotation.name=name)");
        testSet.add("(qualifierannotation=*)");
        testSet.add("(name=multiple1)");
        testSet.add("(name=multiple2)");
        testSet.add("(name=multiple3)");
        testSet.add(filterAnnotationLiteral.value());
        //Cannot get the right order, should use a string comparator
//      Assert.assertEquals("makeFilter(list13) was wrong",
//                          FilterGenerator.make(testSet),
//                          FilterGenerator.makeFilter(list13));
        testSet.clear();
        Assert.assertEquals("makeFilter(emptyFilter,list13) was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(emptyFilter, list0));
        testSet.clear();
        testSet.add("(qualifierwithvalueannotation.value=QWVA)");
        testSet.add("(qualifierwithdefaultvalueannotation.value=default)");
        testSet.add("(qualifierwithvaluesannotation.value=QWVsA)");
        testSet.add("(qualifierwithvaluesannotation.name=name)");
        testSet.add("(qualifierannotation=*)");
        Assert.assertEquals("makeFilter(emptyFilter,list8) was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(emptyFilter, list8));
        testSet.clear();
        testSet.add(filterAnnotationLiteral.value());
        Assert.assertEquals("makeFilter(filterAnnotationLiteral,list0) was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(filterAnnotationLiteral,
                                                       list0));
        testSet.clear();
        testSet.add("(qualifierwithvalueannotation.value=QWVA)");
        testSet.add("(qualifierwithdefaultvalueannotation.value=default)");
        testSet.add("(qualifierwithvaluesannotation.value=QWVsA)");
        testSet.add("(qualifierwithvaluesannotation.name=name)");
        testSet.add("(qualifierannotation=*)");
        testSet.add(filterAnnotationLiteral.value());
        Assert.assertEquals("makeFilter(filterAnnotationLiteral,list8) was wrong",
                            FilterGenerator.make(testSet),
                            FilterGenerator.makeFilter(filterAnnotationLiteral,
                                                       list8));
    }

    @Target({
        TYPE, METHOD, PARAMETER, FIELD
    })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface NonQualifierAnnotation {
    }

    public class NonQualifierAnnotationLiteral
            extends AnnotationLiteral<NonQualifierAnnotation>
            implements NonQualifierAnnotation {
    }

    @Target({
        TYPE, METHOD, PARAMETER, FIELD
    })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Qualifier
    public @interface QualifierAnnotation {
    }

    public class QualifierAnnotationLiteral
            extends AnnotationLiteral<QualifierAnnotation>
            implements QualifierAnnotation {
    }

    @Target({
        TYPE, METHOD, PARAMETER, FIELD
    })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Qualifier
    public @interface QualifierWithValueAnnotation {

        String value();

    }

    public class QualifierWithValueAnnotationLiteral
            extends AnnotationLiteral<QualifierWithValueAnnotation>
            implements QualifierWithValueAnnotation {

        String value;

        public QualifierWithValueAnnotationLiteral(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }

    }

    @Target({
        TYPE, METHOD, PARAMETER, FIELD
    })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Qualifier
    public @interface QualifierWithDefaultValueAnnotation {

        String value() default "default";

    }

    public class QualifierWithDefaultValueAnnotationLiteral
            extends AnnotationLiteral<QualifierWithDefaultValueAnnotation>
            implements QualifierWithDefaultValueAnnotation {

        String value;

        public QualifierWithDefaultValueAnnotationLiteral() {
            this.value = "default";
        }

        public QualifierWithDefaultValueAnnotationLiteral(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }

    }

    @Target({
        TYPE, METHOD, PARAMETER, FIELD
    })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Qualifier
    public @interface QualifierWithValuesAnnotation {

        String value() default "default";

        String name() default "name";

    }

    public class QualifierWithValuesAnnotationLiteral
            extends AnnotationLiteral<QualifierWithValuesAnnotation>
            implements QualifierWithValuesAnnotation {

        String value;

        String name;

        public QualifierWithValuesAnnotationLiteral() {
            this.value = "default";
        }

        public QualifierWithValuesAnnotationLiteral(String value) {
            this.value = value;
        }

        public QualifierWithValuesAnnotationLiteral(String value, String name) {
            this.value = value;
            this.name = name;
        }

        @Override
        public String value() {
            return value;
        }

        @Override
        public String name() {
            return name;
        }

    }

    public class PropertyAnnotationLiteral
            extends AnnotationLiteral<Property>
            implements Property {

        String value;

        String name;

        public PropertyAnnotationLiteral(String name, String value) {
            this.value = value;
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String value() {
            return value;
        }

    }

    public class PropertiesAnnotationLiteral
            extends AnnotationLiteral<Properties>
            implements Properties {

        Property[] properties;

        public PropertiesAnnotationLiteral(Property... properties) {
            this.properties = properties;
        }

        @Override
        public Property[] value() {
            return properties;
        }

    }

    public class FilterAnnotationLiteral
            extends AnnotationLiteral<Filter>
            implements Filter {

        String filter;

        public FilterAnnotationLiteral(String filter) {
            this.filter = filter;
        }

        @Override
        public String value() {
            return filter;
        }

    }
}
