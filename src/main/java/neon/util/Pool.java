package neon.util;

import java.util.LinkedList;
import java.util.Queue;

public abstract class Pool<T> {

    public final int max;

    private final Queue<T> freeObjects;

    public Pool() {
        this(Integer.MAX_VALUE);
    }

    public Pool(int max) {
        freeObjects = new LinkedList<>();
        this.max = max;
    }

    protected abstract T newObject();

    public T obtain() {
        return freeObjects.isEmpty() ? newObject() : freeObjects.poll();
    }

    public void free(T object) {
        if (freeObjects.size() < max) {
            freeObjects.add(object);
        }
        if (object instanceof Disposable) {
            ((Disposable) object).dispose();
        }
    }

    public void freeAll(Iterable<T> objects) {
        Queue<T> freeObjects = this.freeObjects;
        int max = this.max;
        for (T object : objects) {
            if (freeObjects.size() < max) {
                freeObjects.add(object);
            }
            if (object instanceof Disposable) {
                ((Disposable) object).dispose();
            }
        }
    }

    public void clear() {
        freeObjects.clear();
    }

    public int getFreeObjectsCount() {
        return freeObjects.size();
    }

    public static interface Disposable {

        public void dispose();
    }
}
