/*
 * Copyright 2023 Ericsson, https://www.ericsson.com/en
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.devoteam.srit.xmlloader.core.utils;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookupFactory;

public class EnvUtils {

    static final String ENV_SYSTEM_PROPERTY_PREFIX = "env.SYSTEM_PROPERTY_PREFIX";
    static final String ENV_SYSTEM_PROPERTY_SUFFIX = "env.SYSTEM_PROPERTY_SUFFIX";
    static final String ENV_SYSTEM_PROPERTY_ESCAPE_CHAR = "env.SYSTEM_PROPERTY_ESCAPE_CHAR";

    public static String resolveSystemOrEnvProperties(String filePath) {
        Config config = Config.getConfigByName("tester.properties");
        return new StringSubstitutor(
                s -> {
                    String val = StringLookupFactory.INSTANCE.systemPropertyStringLookup().lookup(s);
                    return val != null ? val : StringLookupFactory.INSTANCE.environmentVariableStringLookup().lookup(s);
                },
                getPrefix(config),
                getSuffix(config),
                getEscapeCharacter(config))
                .replace(filePath);
    }

    static String getPrefix(Config config) {
        String property = config.getString(ENV_SYSTEM_PROPERTY_PREFIX, "");
        return property.isEmpty() ? StringSubstitutor.DEFAULT_VAR_START : property;
    }

    static String getSuffix(Config config) {
        String property = config.getString(ENV_SYSTEM_PROPERTY_SUFFIX, "");
        return property.isEmpty() ? StringSubstitutor.DEFAULT_VAR_END : property;
    }

    static Character getEscapeCharacter(Config config) {
        String property = config.getString(ENV_SYSTEM_PROPERTY_ESCAPE_CHAR, "");
        return property.isEmpty() ? StringSubstitutor.DEFAULT_ESCAPE : property.charAt(0);
    }
}
