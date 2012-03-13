/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
     * URI that represents the current running dir. (.../IMSLoader/bin)
     */
    public static URI IMSLOADER_BIN = new File(System.getProperty("user.dir")).toURI();

    /*
     * URI that represents the current opened test.
     */
    public static URI IMSLOADER_TEST_HOME = null;

    /*
     * URI that represents the current home for resources of the current test.
     * If the home is not specified in the test (home="...") then it should
     * be equal to IMSLOADER_TST
     */
    public static URI IMSLOADER_RESOURCES_HOME = IMSLOADER_BIN.resolve("../conf");
}
