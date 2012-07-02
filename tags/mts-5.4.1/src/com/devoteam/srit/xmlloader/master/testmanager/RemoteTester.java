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
*//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.master.testmanager;

import com.devoteam.srit.xmlloader.core.Test;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.HashMap;

/**
 *
 * @author gpasquiers
 */
public interface RemoteTester
{
    public Test openTest(URI path, URI IMSLOADER_BIN, String name, String home, HashMap<String, String> initialParametersValues, boolean force) throws RemoteException;

    public void startTest(Class runnerClass) throws RemoteException;
    
    public void stopTest() throws RemoteException;

    public void resetStatPool() throws RemoteException;

    public StatPool getStatPool() throws RemoteException;
    
    public void addMultiplexedListener(final String channelUID, String testName, String testcaseName, String scenarioName) throws RemoteException;

    public void removeMultiplexedListener(String channelUID) throws RemoteException;
}
