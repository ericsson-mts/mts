/* 
 * Copyright 2012 Devoteam http://www.devoteam.com
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * 
 * This file is part of Multi-Protocol Test Suite (MTS).
 * 
 * Multi-Protocol Test Suite (MTS) is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License.
 * 
 * Multi-Protocol Test Suite (MTS) is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Multi-Protocol Test Suite (MTS).
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.devoteam.srit.xmlloader.core.pluggable;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluggableComponentRegistry
{
    private static List<Class> classes = null;
    
    public static List<Class> getPluggableComponents(Class wantedClass)
    {
        if(classes == null)
        {
            classes = new LinkedList();
            List<Class> list = find();
            for(Class aClass:list)
            {
                classes.add(aClass);
            }
        }
        
        List<Class> wantedClasses = new LinkedList<Class>();
        for(Class aClass:classes)
        {

            try
            {
                aClass.asSubclass(wantedClass);
                wantedClasses.add(aClass);
            }
            catch(Throwable t)
            {
                
            }
        }

        return wantedClasses;
    }
    
    private static void addClass(Class classe)
    {
        classes.add(classe);
    }
    
    private static List<Class> find()
    {
        List<String> classes = new Vector<String>(10, 10);
        try
        {
            // get the system classpath
            //String classpath = "../lib/";//System.getProperty("java.class.path", "");
            String classpath = System.getProperty("java.class.path", "");
            if (classpath.equals(""))
            {
                System.err.println("ClassFinder error: classpath is not set");
            }

            StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator);

            while (st.hasMoreTokens())
            {
                String token = st.nextToken();
                File classpathElement = new File(token);
                if(classpathElement.exists())
                {
                    classes.addAll(classpathElement.isDirectory() ? loadClassesFromDir(classpathElement) : loadClassesFromJar(classpathElement));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        LinkedList<Class> correctClasses = new LinkedList<Class>();
        
        for(String aclass:classes)
        {
            if(!aclass.toLowerCase().contains("pluggable")) continue;
            try
            {
                Class theClass = ClassLoader.getSystemClassLoader().loadClass(aclass.substring(0, aclass.length() - 6).replace("\\", ".").replace("/", "."));
                theClass.asSubclass(PluggableComponent.class);
                if(!Modifier.isAbstract(theClass.getModifiers()) && !Modifier.isInterface(theClass.getModifiers()))
                {
                    correctClasses.add(theClass);
                }
            }
            catch(Throwable t)
            {
            }
        }
        return correctClasses;
    }

    private static List<String> loadClassesFromJar(File jarFile)
    {
        List<String> files = new Vector<String>(10, 10);
        try
        {
            if (jarFile.getName().endsWith(".jar"))
            {
                Enumeration<JarEntry> fileNames;
                fileNames = new JarFile(jarFile).entries();
                JarEntry entry = null;
                while (fileNames.hasMoreElements())
                {
                    entry = fileNames.nextElement();
                    if (entry.getName().endsWith(".class"))
                    {
                        files.add(entry.getName());
                    }
                }
            }
        }
        catch (IOException e)
        {

            e.printStackTrace();
        }

        return files;
    }

    private static List<String> loadClassesFromDir(File classpathElement)
    {
        List<String> allEntries = loadClassesFromDir(classpathElement, null);
        return allEntries;
    }

    private static List<String> loadClassesFromDir(File classpathElement, String prefix)
    {
        List<String> allEntries = new Vector<String>();  //this will contain all entries in this directory
        //these are all the class files directly in this directory
        List<String> classEntries = Arrays.asList(classpathElement.list(new CLASSFilter()));
        //add the prefix to all entries
        if (prefix != null)
        {
            for (int i = 0, c = classEntries.size(); i < c; i++)
            {
                allEntries.add(prefix + "." + classEntries.get(i));
            }
        }
        //now get a list of all directories within the current directory
        List<File> allSubdirectories = Arrays.asList(classpathElement.listFiles(new DirectoryFilter()));
        //loop through all of them and load their classes, adding this directory to the prefix list.
        for (int i = 0, c = allSubdirectories.size(); i < c; i++)
        {
            allEntries.addAll(loadClassesFromDir(allSubdirectories.get(i), (prefix == null ? "" : prefix + ".") + allSubdirectories.get(i).getName()));
        }
        return allEntries;
    }

    static class DirectoryFilter implements FileFilter
    {
        public boolean accept(File dir)
        {
            return dir.isDirectory();
        }
    }
    
    static class CLASSFilter implements FilenameFilter
    {

        public boolean accept(File dir, String name)
        {
            return (name.endsWith(".class"));
        }
    }
}

