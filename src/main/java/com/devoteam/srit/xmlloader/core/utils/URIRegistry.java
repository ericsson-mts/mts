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

package com.devoteam.srit.xmlloader.core.utils;

import java.io.File;
import java.net.URI;

/**
 *
 * @author gpasquiers
 */
public class URIRegistry
{
    /*
     * URI that represents the current running dir. (.../MTS/bin)
     */
    public static URI MTS_BIN_HOME = new File(System.getProperty("user.dir")).toURI();

    /*
     * URI that represents the current opened test.
     */
    public static URI MTS_TEST_HOME = null;

    /*
     * URI that represents the current home for resources of the current test.
     * If the home is not specified in the test (home="...") then it should
     * be equal to MTS_TST
     */
    public static URI MTS_CONFIG_HOME = MTS_BIN_HOME.resolve("../conf");
}
