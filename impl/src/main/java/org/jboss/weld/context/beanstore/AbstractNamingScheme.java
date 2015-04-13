package org.jboss.weld.context.beanstore;

import java.util.Collection;
import java.util.stream.Collectors;

import org.jboss.weld.bean.StringBeanIdentifier;
import org.jboss.weld.serialization.spi.BeanIdentifier;

public abstract class AbstractNamingScheme implements NamingScheme {

    private final String delimiter;

    /**
     *
     * @param delimiter The delimiter to use between the prefix and the identifier.
     */
    public AbstractNamingScheme(String delimiter) {
        this.delimiter = delimiter;
    }

    public boolean accept(String id) {
        String prefix = getPrefix();
        return id.startsWith(prefix) && id.startsWith(delimiter, prefix.length());
    }

    public BeanIdentifier deprefix(String id) {
        return new StringBeanIdentifier(id.substring(getPrefix().length() + delimiter.length()));
    }

    public String prefix(BeanIdentifier id) {
        return getPrefix() + delimiter + id.asString();
    }

    public Collection<String> filterIds(Collection<String> ids) {
        return ids.stream().filter(this::accept).collect(Collectors.toList());
    }

    public Collection<BeanIdentifier> deprefix(Collection<String> ids) {
        return ids.stream().map(this::deprefix).collect(Collectors.toList());
    }

    public Collection<String> prefix(Collection<BeanIdentifier> ids) {
        return ids.stream().map(this::prefix).collect(Collectors.toList());
    }

    protected abstract String getPrefix();

    protected String getDelimiter() {
        return delimiter;
    }

}
