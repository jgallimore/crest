/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tomitribe.crest;

import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClasspathArchive;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.util.JarLocation;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Xbean implements Iterable<Class<?>> {

    final Set<Class<?>> classes = new HashSet<Class<?>>();

    public Xbean() {
        this(Xbean.defaultArchive());
    }

    public Xbean(Archive archive) {
        final AnnotationFinder finder = new AnnotationFinder(archive);

        for (Method method : finder.findAnnotatedMethods(Command.class)) {
            classes.add(method.getDeclaringClass());
        }
    }

    private static Archive defaultArchive() {
        try {
            final File file = JarLocation.jarLocation(Main.class);
            return ClasspathArchive.archive(Main.class.getClassLoader(), file.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Iterator<Class<?>> iterator() {
        return classes.iterator();
    }
}
