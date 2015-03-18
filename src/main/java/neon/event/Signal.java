package neon.event;

import java.util.ArrayList;
import java.util.List;

public final class Signal<T> {

    private List<SignalListener<T>> listeners;

    public Signal() {
        listeners = new ArrayList<>();
    }

    public void add(SignalListener<T> listener) {
        listeners.add(listener);
    }

    public void remove(SignalListener<T> listener) {
        listeners.remove(listener);
    }

    public void removeAll() {
        listeners.clear();
    }

    public boolean hasListeners() {
        return listeners.size() > 0;
    }

    public void dispatch(T object) {
        for (SignalListener<T> listener : new ArrayList<>(listeners)) {
            listener.receive(this, object);
        }
    }
}
