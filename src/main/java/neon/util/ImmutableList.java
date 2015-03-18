package neon.util;

import java.util.Iterator;
import java.util.List;

public final class ImmutableList<T> implements Iterable<T> {

    private final List<T> wrapped;

    public ImmutableList(List<T> wrapped) {
        this.wrapped = wrapped;
    }

    public int size() {
        return wrapped.size();
    }

    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    public boolean contains(T o) {
        return wrapped.contains(o);
    }

    public Object[] toArray() {
        return wrapped.toArray();
    }

    public T[] toArray(T[] a) {
        return wrapped.toArray(a);
    }

    @Override
    public Iterator<T> iterator() {
        return wrapped.iterator();
    }

    public T get(int index) {
        return wrapped.get(index);
    }

    public int indexOf(Object o) {
        return wrapped.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return wrapped.lastIndexOf(o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ImmutableList that = (ImmutableList) o;

        return wrapped.equals(that.wrapped);
    }

    @Override
    public int hashCode() {
        return wrapped.hashCode();
    }
}
