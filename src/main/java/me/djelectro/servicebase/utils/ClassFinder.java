package me.djelectro.servicebase.utils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassFinder {

    public static Set<Class<?>> findAllClassesInPackage(String packageName) {
        String path = packageName.replace('.', '/');

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(path);

            Set<Class<?>> classes = new HashSet<>();

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();

                if ("file".equals(resource.getProtocol())) {
                    classes.addAll(findClassesInDirectory(resource, packageName));
                } else if ("jar".equals(resource.getProtocol())) {
                    classes.addAll(findClassesInJar(resource, path));
                }
            }

            return classes;
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan package: " + packageName, e);
        }
    }

    private static Set<Class<?>> findClassesInDirectory(URL resource, String packageName) throws MalformedURLException {
        Set<Class<?>> classes = new HashSet<>();

        String decodedPath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8);
        File directory = new File(decodedPath);

        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }

        for (File file : files) {
            String fileName = file.getName();

            if (file.isDirectory()) {
                classes.addAll(findClassesInDirectory(
                        file.toURI().toURL(),
                        packageName + "." + fileName
                ));
            } else if (fileName.endsWith(".class")) {
                String className = packageName + "."
                        + fileName.substring(0, fileName.length() - 6);

                addClass(classes, className);
            }
        }

        return classes;
    }

    private static Set<Class<?>> findClassesInJar(URL resource, String packagePath) throws IOException {
        Set<Class<?>> classes = new HashSet<>();

        JarURLConnection connection = (JarURLConnection) resource.openConnection();

        try (JarFile jarFile = connection.getJarFile()) {
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.startsWith(packagePath)
                        && entryName.endsWith(".class")
                        && !entryName.contains("$")) {

                    String className = entryName
                            .replace('/', '.')
                            .substring(0, entryName.length() - 6);

                    addClass(classes, className);
                }
            }
        }

        return classes;
    }

    private static void addClass(Set<Class<?>> classes, String className) {
        try {
            classes.add(Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load class: " + className, e);
        }
    }
}
