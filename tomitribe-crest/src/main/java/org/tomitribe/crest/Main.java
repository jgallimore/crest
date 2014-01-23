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

import org.tomitribe.crest.api.StreamingOutput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    final Map<String, Cmd> commands = new HashMap<String, Cmd>();

    public Main() {
        this(new SystemPropertiesDefaultsContext(), new Xbean());
    }

    public Main(DefaultsContext defaultsContext, Class<?>... classes) {
        this(defaultsContext, Arrays.asList(classes));
    }

    public Main(DefaultsContext defaultsContext, Iterable<Class<?>> classes) {
        for (Class clazz : classes) {
            this.commands.putAll(Commands.get(clazz, defaultsContext));
        }

        installHelp(defaultsContext);
    }

    public Main(final Iterable<Class<?>> classes) {
        this(new SystemPropertiesDefaultsContext(), classes);
    }

    private void add(Cmd cmd) {
        commands.put(cmd.getName(), cmd);
    }

    private void installHelp(DefaultsContext dc) {
        final Map<String, Cmd> stringCmdMap = Commands.get(new Help(Main.this.commands), dc);
        for (Cmd cmd : stringCmdMap.values()) {
            add(cmd);
        }
    }

    public static void main(String... args) throws Exception {
        final Main main = new Main();
        try {
            final Object result = main.exec(args);
            if (result instanceof StreamingOutput) {
                ((StreamingOutput) result).write(System.out);
            } else if (result instanceof String) {
                System.out.println(result);
            }
        } catch (CommandFailedException e) {
            e.getCause().printStackTrace();
            System.exit(-1);
        } catch (Exception alreadyHandled) {
            System.exit(-1);
        }
    }

    public Object exec(String... args) throws Exception {
        final List<String> list = processSystemProperties(args);

        final String command = (list.size() == 0) ? "help" : list.remove(0);
        args = list.toArray(new String[list.size()]);

        final Cmd cmd = commands.get(command);

        if (cmd == null) {
            System.err.println("Unknown command: " + command);
            System.err.println();
            commands.get("help").exec();
            throw new IllegalArgumentException();
        }

        return cmd.exec(args);
    }

    public static List<String> processSystemProperties(String[] args) {
        final List<String> list = new ArrayList<String>();

        // Read in and apply the properties specified on the command line
        for (String arg : args) {
            if (arg.startsWith("-D")) {

                final String name = arg.substring(arg.indexOf("-D") + 2, arg.indexOf("="));
                final String value = arg.substring(arg.indexOf("=") + 1);

                System.setProperty(name, value);
            } else {
                list.add(arg);
            }
        }

        return list;
    }

}
