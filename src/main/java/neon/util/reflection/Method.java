package neon.util.reflection;

import neon.core.Component;
import neon.core.Entity;

import java.lang.reflect.InvocationTargetException;

public final class Method {

    private final java.lang.reflect.Method method;

    public Method(java.lang.reflect.Method method) {
        this.method = method;
    }

    public boolean isValidNodeMethod() {
        return isComponentMethod() || isGetEntityMethod() || isObjectMethod();
    }

    public boolean isComponentMethod() {
        return Component.class.isAssignableFrom(getReturnType());
    }

    public boolean isGetEntityMethod() {
        return getName().equals("getEntity") && getReturnType() == Entity.class;
    }

    public boolean isObjectMethod() {
        return isEqualsMethod() || isHashCodeMethod() || isToStringMethod();
    }

    public boolean isEqualsMethod() {
        return getName().equals("equals") && getReturnType() == boolean.class;
    }

    public boolean isHashCodeMethod() {
        return getName().equals("hashCode") && getReturnType() == int.class;
    }

    public boolean isToStringMethod() {
        return getName().equals("toString") && getReturnType() == String.class;
    }

    public String getName() {
        return method.getName();
    }

    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    public Object invoke(Object obj, Object... args) {
        try {
            return method.invoke(obj, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new ReflectionException(e);
        }
    }
}
