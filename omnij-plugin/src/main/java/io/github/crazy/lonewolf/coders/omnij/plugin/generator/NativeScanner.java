package io.github.crazy.lonewolf.coders.omnij.plugin.generator;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class NativeScanner {

    public static List<Class<?>> getClassesWithAnnotation(ClassLoader classLoader, String packagePath, Class<?> annotationClass) throws Exception {
        List<Class<?>> annotatedClasses = new ArrayList<>();
        String path = packagePath.replace('.', '/');

        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }


        for (File directory : dirs) {
            annotatedClasses.addAll(findClasses(classLoader,directory, packagePath, annotationClass));
        }
        return annotatedClasses;
    }

    private static List<Class<?>> findClasses(ClassLoader classLoader,File directory, String packageName, Class<?> annotationClass) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        if (files == null) return classes;

        for (File file : files) {
            if (file.isDirectory()) {
                // Recursively scan subpackages
                classes.addAll(findClasses(classLoader,file, packageName + "." + file.getName(), annotationClass));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                Class<?> cls = Class.forName(className,false, classLoader);

                // Check if the target annotation is present
                if (cls.isAnnotationPresent((Class<? extends java.lang.annotation.Annotation>) annotationClass)) {
                    classes.add(cls);
                }
            }
        }
        return classes;
    }
}
