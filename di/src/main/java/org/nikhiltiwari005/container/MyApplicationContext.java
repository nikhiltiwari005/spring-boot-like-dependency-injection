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
            Constructor<?> constructorToUse = null;

            // Find constructor with @Autowired, else default constructor
            for (Constructor<?> constructor : cls.getDeclaredConstructors()) {
                if (constructor.isAnnotationPresent(Autowired.class)) {
                    constructorToUse = constructor;
                    break;
                }
            }

            if (constructorToUse == null) {
                constructorToUse = cls.getDeclaredConstructor();
            }

            Object instance;
            if (constructorToUse.getParameterCount() == 0) {
                instance = constructorToUse.newInstance();
            } else {
                List<Object> args = new ArrayList<>();
                for (Class<?> paramType : constructorToUse.getParameterTypes()) {
                    Object dependency = beanMap.get(paramType);
                    if (dependency == null) {
                        throw new RuntimeException("Unsatisfied dependency for constructor: " + cls.getName());
                    }
                    args.add(dependency);
                }
                instance = constructorToUse.newInstance(args.toArray());
            }

            beanMap.put(cls, instance);
        }

        injectDependencies();
    }

    private void injectDependencies() throws Exception {
        for (Object bean : beanMap.values()) {
            Class<?> cls = bean.getClass();

            // Field injection
            for (Field field : cls.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    Object dependency = beanMap.get(field.getType());
                    if (dependency == null) {
                        throw new RuntimeException("Unsatisfied dependency for field: " + field.getName() + " in " + cls.getName());
                    }
                    field.setAccessible(true);
                    field.set(bean, dependency);
                }
            }

            // Method injection
            for (Method method : cls.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Autowired.class)) {
                    List<Object> args = new ArrayList<>();
                    for (Class<?> paramType : method.getParameterTypes()) {
                        Object dependency = beanMap.get(paramType);
                        if (dependency == null) {
                            throw new RuntimeException("Unsatisfied dependency for method: " + method.getName() + " in " + cls.getName());
                        }
                        args.add(dependency);
                    }
                    method.setAccessible(true);
                    method.invoke(bean, args.toArray());
                }
            }
        }
    }
}
