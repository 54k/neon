package neon.util.reflection;

import neon.core.Component;
import neon.core.Node;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class ClassReflection {

    @SuppressWarnings("unchecked")
    public static <T> T newProxyInstance(Class<T> c, InvocationHandler invocationHandler) throws ReflectionException {
        try {
            return (T) Proxy.newProxyInstance(c.getClassLoader(), new Class[]{c}, invocationHandler);
        } catch (IllegalArgumentException | SecurityException e) {
            throw new ReflectionException(e.getMessage(), e);
        }
    }

    public static <T> T newInstance(Class<T> c) throws ReflectionException {
        try {
            return c.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ReflectionException("Could not instantiate instance of class: " + c.getName(), e);
        }
    }

    public static Field[] getDeclaredFields(Class c) {
        java.lang.reflect.Field[] fields = c.getDeclaredFields();
        Field[] result = new Field[fields.length];
        for (int i = 0, j = fields.length; i < j; i++) {
            result[i] = new Field(fields[i]);
        }
        return result;
    }

    public static Method[] getDeclaredMethods(Class c) {
        java.lang.reflect.Method[] methods = c.getDeclaredMethods();
        Method[] result = new Method[methods.length];
        for (int i = 0; i < methods.length; i++) {
            result[i] = new Method(methods[i]);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Iterable<Class<Component>> getComponentsFor(Class<? extends Node> nodeClass) {
        Set<Class<Component>> components = new HashSet<>();
        getComponentsFor0(components, nodeClass);
        Class<?>[] interfaces = nodeClass.getInterfaces();
        for (Class<?> parentNodeClass : interfaces) {
            if (Node.class.isAssignableFrom(parentNodeClass)) {
                getComponentsFor0(components, (Class<? extends Node>) parentNodeClass);
            }
        }
        return components;
    }

    @SuppressWarnings("unchecked")
    private static void getComponentsFor0(Collection<Class<Component>> components, Class<? extends Node> nodeClass) {
        for (Method method : getDeclaredMethods(nodeClass)) {
            if (!method.isValidNodeMethod()) {
                throw new IllegalArgumentException();
            }
            if (method.isComponentMethod()) {
                Class<?> returnType = method.getReturnType();
                Class<Component> componentClass = (Class<Component>) returnType;
                components.add(componentClass);
            }
        }
    }

    public static Class<?> getElementClass(Class<?> clazz, int index) {
        ParameterizedType genericSuperclass = (ParameterizedType) clazz.getGenericSuperclass();
        Type actualType = genericSuperclass.getActualTypeArguments()[index];
        return (Class<?>) actualType;
    }
}
