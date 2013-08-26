package org.jboss.weld.context.beanstore;

import static com.google.common.collect.Collections2.filter;

import java.util.ArrayList;
import java.util.Collection;

import org.jboss.weld.bean.StringBeanIdentifier;
import org.jboss.weld.serialization.spi.BeanIdentifier;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public abstract class AbstractNamingScheme implements NamingScheme {

    class PrefixPredicate implements Predicate<String> {

        public boolean apply(String input) {
            return AbstractNamingScheme.this.accept(input);
        }

    }

    class DeprefixerFunction implements Function<String, BeanIdentifier> {

        public BeanIdentifier apply(String from) {
            return AbstractNamingScheme.this.deprefix(from);
        }

    }

    class PrefixerFunction implements Function<BeanIdentifier, String> {

        public String apply(BeanIdentifier from) {
            return AbstractNamingScheme.this.prefix(from);
        }

    }

    private final String delimiter;
    private final PrefixPredicate predicate;
    private final DeprefixerFunction deprefixerFunction;
    private final PrefixerFunction prefixerFunction;

    /**
     * Create a new Prefixer.
     *
     * @param prefix    The prefix
     * @param delimiter The delimiter to use between the prefix and the
     *                  identifier.
     */
    public AbstractNamingScheme(String delimiter) {
        this.delimiter = delimiter;
        this.predicate = new PrefixPredicate();
        this.deprefixerFunction = new DeprefixerFunction();
        this.prefixerFunction = new PrefixerFunction();
    }

    public boolean accept(String id) {
        return id.startsWith(getPrefix() + delimiter);
    }

    public BeanIdentifier deprefix(String id) {
        return new StringBeanIdentifier(id.substring(getPrefix().length() + delimiter.length()));
    }

    public String prefix(BeanIdentifier id) {
        return getPrefix() + delimiter + id.asString();
    }

    public Collection<String> filterIds(Collection<String> ids) {
        return new ArrayList<String>(filter(ids, predicate));
    }

    public Collection<BeanIdentifier> deprefix(Collection<String> ids) {
        return new ArrayList<BeanIdentifier>(Collections2.transform(ids, deprefixerFunction));
    }

    public Collection<String> prefix(Collection<BeanIdentifier> ids) {
        return new ArrayList<String>(Collections2.transform(ids, prefixerFunction));
    }

    protected abstract String getPrefix();

    protected String getDelimiter() {
        return delimiter;
    }

}
