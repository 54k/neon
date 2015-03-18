package neon.util;

public class Bag<E> {

    private E[] data;
    private int size = 0;

    public Bag() {
        this(16);
    }

    @SuppressWarnings("unchecked")
    public Bag(int capacity) {
        data = (E[]) new Object[capacity];
    }

    public E remove(int index) {
        E e = data[index]; // make copy of element to remove so it can be returned
        data[index] = data[--size]; // overwrite item to remove with last element
        data[size] = null; // null last element, so gc can do its work
        return e;
    }

    public E removeLast() {
        if (size > 0) {
            E e = data[--size];
            data[size] = null;
            return e;
        }

        return null;
    }

    public boolean remove(E e) {
        for (int i = 0; i < size; i++) {
            E e2 = data[i];

            if (e == e2) {
                data[i] = data[--size]; // overwrite item to remove with last element
                data[size] = null; // null last element, so gc can do its work
                return true;
            }
        }

        return false;
    }

    public boolean contains(E e) {
        for (int i = 0; size > i; i++) {
            if (e == data[i]) {
                return true;
            }
        }
        return false;
    }

    public E get(int index) {
        return data[index];
    }

    public int size() {
        return size;
    }

    public int getCapacity() {
        return data.length;
    }

    public boolean isIndexWithinBounds(int index) {
        return index < getCapacity();
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void add(E e) {
        // is size greater than capacity increase capacity
        if (size == data.length) {
            grow();
        }

        data[size++] = e;
    }

    public void set(int index, E e) {
        if (index >= data.length) {
            grow(index * 2);
        }
        size = index + 1;
        data[index] = e;
    }

    public void clear() {
        // null all elements so gc can clean up
        for (int i = 0; i < size; i++) {
            data[i] = null;
        }

        size = 0;
    }

    private void grow() {
        int newCapacity = (data.length * 3) / 2 + 1;
        grow(newCapacity);
    }

    @SuppressWarnings("unchecked")
    private void grow(int newCapacity) {
        E[] oldData = data;
        data = (E[]) new Object[newCapacity];
        System.arraycopy(oldData, 0, data, 0, oldData.length);
    }
}
