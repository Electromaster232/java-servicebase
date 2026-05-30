package me.djelectro.servicebase;


import java.lang.reflect.InvocationTargetException;

public interface SubService<T extends ServiceParams> {

    void init(T params) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;
}
