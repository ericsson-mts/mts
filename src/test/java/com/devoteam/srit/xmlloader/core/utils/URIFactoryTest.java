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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;

public class URIFactoryTest {

    @Before
    public void setUp() {
        Config.reset();
        URIRegistry.MTS_CONFIG_HOME = new File("src/main/conf/").toURI();
        SingletonFSInterface.setInstance(new LocalFSInterface());
    }

    @Test
    public void testResolveURI(){

        String fileName = "../function";
        URI relativeTo = URIRegistry.MTS_BIN_HOME;
        URI result = URIFactory.resolveURI(fileName, relativeTo);
        Assert.assertEquals(URIRegistry.MTS_BIN_HOME.resolve(fileName), result);

        fileName = EnvUtils.resolveSystemOrEnvProperties("${user.home}/function");
        result = URIFactory.resolveURI(fileName, relativeTo);
        Assert.assertEquals(new File(fileName).toURI(), result);
    }
}
