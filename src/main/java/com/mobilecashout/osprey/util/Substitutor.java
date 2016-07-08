/*
 * Copyright 2016 Innovative Mobile Solutions Limited and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobilecashout.osprey.util;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Substitutor {
    private final StrSubstitutor substitutor;
    private final TreeMap<String, String> values = new TreeMap<>();

    public Substitutor(HashMap<String, String> variables) {
        values.putAll(variables);
        for (Map.Entry<String, String> env : System.getenv().entrySet()) {
            values.put(String.format("env_%s", env.getKey()).toLowerCase(), env.getValue());
        }
        this.substitutor = new StrSubstitutor(values, "{", "}");
    }

    public String replace(String text) {
        return this.substitutor.replace(text);
    }

    public Substitutor add(String key, Object value) {
        values.put(key, String.valueOf(value));
        return this;
    }

    public Substitutor addAll(HashMap<String, String> variables) {
        values.putAll(variables);
        return this;
    }

    public void list(Logger logger) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        for (Map.Entry<String, String> value : values.entrySet()) {
            printWriter.println(String.format("● {%s}: %s", value.getKey(), value.getValue()));
        }
        logger.info("Currently available variables:\n" + writer.toString().trim());
    }

    public boolean contains(String key) {
        return values.containsKey(key);
    }

    public String get(String key) {
        return values.get(key);
    }
}
