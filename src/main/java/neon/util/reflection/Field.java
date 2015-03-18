package neon.util.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public final class Field {

    private final java.lang.reflect.Field field;

    public Field(java.lang.reflect.Field field) {
        this.field = field;
    }

    public String getName() {
        return field.getName();
    }

    public Class getType() {
        return field.getType();
    }

    public Class getDeclaringClass() {
        return field.getDeclaringClass();
    }

    public void setAccessible(boolean accessible) {
        field.setAccessible(accessible);
    }

    public boolean isFinal() {
        return Modifier.isFinal(field.getModifiers());
    }

    public Class getElementType(int index) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            Type[] actualTypes = ((ParameterizedType) genericType).getActualTypeArguments();
            if (actualTypes.length - 1 >= index) {
                Type actualType = actualTypes[index];
                if (actualType instanceof Class) {
                    return (Class) actualType;
                } else if (actualType instanceof ParameterizedType) {
                    return (Class) ((ParameterizedType) actualType).getRawType();
                }
            }
        }
        return null;
    }

    public Object get(Object obj) throws ReflectionException {
        try {
            return field.get(obj);
        } catch (IllegalArgumentException e) {
            throw new ReflectionException("Object is not an instance of " + getDeclaringClass(), e);
        } catch (IllegalAccessException e) {
            throw new ReflectionException("Illegal access to field: " + getName(), e);
        }
    }

    public void set(Object obj, Object value) throws ReflectionException {
        try {
            field.set(obj, value);
        } catch (IllegalArgumentException e) {
            throw new ReflectionException("Argument not valid for field: " + getName(), e);
        } catch (IllegalAccessException e) {
            throw new ReflectionException("Illegal access to field: " + getName(), e);
        }
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return field.isAnnotationPresent(annotationType);
    }
}