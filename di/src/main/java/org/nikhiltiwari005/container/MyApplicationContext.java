package org.nikhiltiwari005.container;

import org.nikhiltiwari005.annotations.Autowired;
import org.nikhiltiwari005.annotations.Component;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

public class MyApplicationContext {

    private Map<Class<?>, Object> beanMap = new HashMap<>();

    public MyApplicationContext(String basePackage) {
        try {
            Set<Class<?>> componentClasses = scanForComponents(basePackage);
            System.out.println("step 1");
            instantiateBeans(componentClasses);
            System.out.println("step 2");
            injectDependencies();
            System.out.println("step 3");
        } catch (Exception e) {
            throw new RuntimeException("DI context failed", e);
        }
    }

    public <T> T getBean(Class<T> clazz) {
        return clazz.cast(beanMap.get(clazz));
    }

    private Set<Class<?>> scanForComponents(String basePackage) throws IOException, ClassNotFoundException {
        String path = basePackage.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);
        Set<Class<?>> classes = new HashSet<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            File directory = new File(resource.getFile());
            if (directory.exists()) {
                findClasses(directory, basePackage, classes);
            }
        }
        return classes;
    }

    private void findClasses(File directory, String packageName, Set<Class<?>> classes) throws ClassNotFoundException {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                findClasses(file, packageName + "." + file.getName(), classes); // recursive call
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().replace(".class", "");
                Class<?> cls = Class.forName(className);
                if (cls.isAnnotationPresent(Component.class)) {
                    classes.add(cls);
                }
            }
        }
    }

    private void instantiateBeans(Set<Class<?>> classes) throws Exception {
        for (Class<?> cls : classes) {
            Object instance = cls.getDeclaredConstructor().newInstance();
            beanMap.put(cls, instance);
        }
    }

    private void injectDependencies() throws IllegalAccessException {
        for (Object bean : beanMap.values()) {
            for (Field field : bean.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    Class<?> dependencyType = field.getType();
                    Object dependency = beanMap.get(dependencyType);
                    field.setAccessible(true);
                    field.set(bean, dependency);
                }
            }
        }
    }
}
