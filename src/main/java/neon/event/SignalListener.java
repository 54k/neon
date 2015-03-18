package neon.event;

public interface SignalListener<T> {

    public void receive(Signal<T> signal, T object);
}