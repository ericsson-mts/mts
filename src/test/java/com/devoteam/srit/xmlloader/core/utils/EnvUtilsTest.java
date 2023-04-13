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

import com.devoteam.srit.xmlloader.core.utils.filesystem.LocalFSInterface;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static junit.framework.TestCase.assertEquals;

public class EnvUtilsTest {

    @Before
    public void setUp() {
        Config.reset();
        URIRegistry.MTS_CONFIG_HOME = new File("src/main/conf/").toURI();
        SingletonFSInterface.setInstance(new LocalFSInterface());
    }

    @Test
    public void testWithDefaultValues() {
        URIRegistry.MTS_CONFIG_HOME = new File("src/main/conf/").toURI();
        Config config = Config.getConfigByName("tester.properties");
        assertEquals("${", EnvUtils.getPrefix(config));
        assertEquals("}", EnvUtils.getSuffix(config));
        assertEquals('$', (char) EnvUtils.getEscapeCharacter(config));
    }

    @Test
    public void testWithCustomProperties() {
        URIRegistry.MTS_CONFIG_HOME = new File("src/test/resources/conf/").toURI();
        Config config = Config.getConfigByName("tester.properties");
        assertEquals("#[", EnvUtils.getPrefix(config));
        assertEquals("]", EnvUtils.getSuffix(config));
        assertEquals('#', (char) EnvUtils.getEscapeCharacter(config));
    }

    @Test
    public void testResolveSystemPropertiesWithDefaultValues() {
        URIRegistry.MTS_CONFIG_HOME = new File("src/main/conf/").toURI();
        String path = "${user.home}/env/test";
        System.getProperties().setProperty("user.home", "myHomeDirectory");
        String expectedResult = "myHomeDirectory/env/test";
        assertEquals(expectedResult, EnvUtils.resolveSystemOrEnvProperties(path));
    }

    @Test
    public void testResolveEnvPropertiesWithDefaultValues() {
        URIRegistry.MTS_CONFIG_HOME = new File("src/main/conf/").toURI();
        String pathEnv = System.getenv("PATH");
        String myPath = "My path : ${PATH}";
        String expectedResult = "My path : " + pathEnv;
        assertEquals(expectedResult, EnvUtils.resolveSystemOrEnvProperties(myPath));
    }

    @Test
    public void testResolveSystemPropertiesWitCustomProperties() {
        URIRegistry.MTS_CONFIG_HOME = new File("src/test/resources/conf/").toURI();
        System.getProperties().setProperty("user.home", "myHomeDirectory");

        String path1 = "${user.home}/env/test";
        assertEquals("${user.home}/env/test", EnvUtils.resolveSystemOrEnvProperties(path1));

        String path2 = "#[user.home]/env/test";
        String expectedResult = "myHomeDirectory/env/test";
        assertEquals(expectedResult, EnvUtils.resolveSystemOrEnvProperties(path2));
    }

    @Test
    public void testEscapeCharacter() {
        URIRegistry.MTS_CONFIG_HOME = new File("src/main/conf/").toURI();
        String path = "${user.home}/${user.directory}/test";
        System.getProperties().setProperty("user.home", "myHomeDirectory");
        System.getProperties().setProperty("user.directory", "myUserDirectory");

        String expectedResult = "myHomeDirectory/myUserDirectory/test";
        assertEquals(expectedResult, EnvUtils.resolveSystemOrEnvProperties(path));

        path = "$${user.home}/$${user.directory}/test";
        expectedResult = "${user.home}/${user.directory}/test";
        assertEquals(expectedResult, EnvUtils.resolveSystemOrEnvProperties(path));
    }
 }
