package cc.ricecx.ricestats.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author RiceCX
 *
 * This class is used to store the information of a method pair.
 * This has the object that this method belongs to alongside the method itself.
 */
public record MethodPair(Object object, Method method) {

    public void execute(Object args) throws InvocationTargetException, IllegalAccessException {
        method.invoke(object, args);
    }
}