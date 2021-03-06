/* 
 * Copyright 2015 Rhythm & Hues Studios.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rhythm.louie;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cjohnson
 */
public class Classes {
    
    public static List<Class<?>> getAnnotatedSpecialized(List<String> prefixes, 
            Class<? extends Annotation> annotation, List<String> whitelist, 
            List<String> blacklist) throws IOException {
        return findTypesAnnotatedWith(prefixes, annotation, true, whitelist, blacklist);
    }
    
    public static List<Class<?>> getRecursiveTypesAnnotatedWith(String packageName, Class<? extends Annotation> annotation) throws IOException {
        List<String> pckgs = new ArrayList<String>();
        pckgs.add(packageName);
        return findTypesAnnotatedWith(pckgs, annotation, true, null, null);
    }

    public static List<Class<?>> getTypesAnnotatedWith(String packageName, Class<? extends Annotation> annotation) throws IOException {
        List<String> pckgs = new ArrayList<String>();
        pckgs.add(packageName);
        return findTypesAnnotatedWith(pckgs, annotation, false, null, null);
    }

    private static List<Class<?>> findTypesAnnotatedWith(List<String> packages, Class<? extends Annotation> annotation, boolean recursive, List<String> whitelist, List<String> blacklist) throws IOException {
        ClassPath cp = ClassPath.from(Thread.currentThread().getContextClassLoader());
        
        List<Class<?>> found = new ArrayList<Class<?>>();
        Set<ClassInfo> infos = new HashSet<ClassInfo>();
        if (packages == null || packages.isEmpty()) {
            infos = cp.getTopLevelClasses();
        } else if (recursive) {
            for (String pkg : packages) {
                infos.addAll(cp.getTopLevelClassesRecursive(pkg));
            }
        } else {
            for (String pkg : packages) {
                infos.addAll(cp.getTopLevelClasses(pkg));
            }
        }
        
        for (ClassInfo info : infos) {
            if (blacklist != null && blacklist.contains(info.getPackageName())) {
                if (whitelist != null) {
                    if (!whitelist.contains(info.getName())) {
                        continue; //blacklisted, but not whitelisted
                    }
                } else {
                    continue;
                }
            }
            try {
                Class<?> cl = info.load();
                Annotation ann = cl.getAnnotation(annotation);
                if (ann != null) {
                    found.add(cl);
                }
            } catch (Exception e) {
                LoggerFactory.getLogger(Classes.class).warn("Error Checking Class: "+info.getName(),e);
            }
        }
        
        return found;
    }
    
    /**
     * Finds the first class, superclass, or interface that has the annotation
     * 
     * Note: method is fairly expensive, as it is crawling the class hierarchy 
     * If you need the value often, it is recommended to cache the the value
     * 
     * @param <A>
     * @param cl the base class to search
     * @param ann
     * @return 
     */
    public static <A extends Annotation> Class<?> findAnnotatedClass(Class<?> cl, Class<A> ann) {
        if (cl.getAnnotation(ann)!=null) {
            return cl;
        }
        
        for (Class<?> iface : cl.getInterfaces()) {
            Class<?> found = findAnnotatedClass(iface, ann);
            if (found !=null) {
                return found;
            }
        }
        
        if (cl.getSuperclass() !=null && cl.getSuperclass() != Object.class) {
            return findAnnotatedClass(cl.getSuperclass(),ann);
        }
        return null;
    }
}
