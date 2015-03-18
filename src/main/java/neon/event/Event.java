package neon.event;

import neon.util.reflection.ClassReflection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class Event<T extends EventListener> {

    private Dispatcher<T> dispatcher;

    public Event(Class<T> listenerType) {
        validateType(listenerType);
        this.dispatcher = new Dispatcher<>(listenerType);
    }

    private static void validateType(Class<? extends EventListener> listenerType) {
        for (Method method : listenerType.getDeclaredMethods()) {
            if (method.getReturnType() != void.class) {
                throw new IllegalArgumentException();
            }
        }
    }

    public void add(T listener) {
        dispatcher.listeners.add(listener);
    }

    public void remove(T listener) {
        dispatcher.listeners.remove(listener);
    }

    public void removeAll() {
        dispatcher.listeners.clear();
    }

    public boolean hasListeners() {
        return dispatcher.listeners.size() > 0;
    }

    public T dispatch() {
        return dispatcher.proxy;
    }

    private static class Dispatcher<T extends EventListener> implements InvocationHandler {

        final T proxy;
        final List<T> listeners = new ArrayList<>();

        Dispatcher(Class<T> type) {
            proxy = ClassReflection.newProxyInstance(type, this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            for (T l : new ArrayList<>(listeners)) {
                method.invoke(l, args);
            }
            return null;
        }
    }
}
