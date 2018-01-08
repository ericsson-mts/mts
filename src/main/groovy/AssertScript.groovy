/* 
 * Copyright 2017 Orange http://www.orange.com
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
import org.codehaus.groovy.runtime.powerassert.PowerAssertionError;
/**
 * base class of the groovy classes used in MTS for test assertion.
 * AssertScript offers some utility function for test assertion
 *
 * @author mickael.jezequel@orange.com
 *
 */
abstract class AssertScript extends MTSScript {

    /**
     * this method is called when the groovy script is executed
     * here can go any common pre/post action
     */
    def run() {
        // declare the failed test counter
        _counter=0	
    }
	
    /**
     * reset failed tests counter
     *  
     * @return
     */
    def initCheck() {
        _counter=0;
    }
	
    /**
     * evaluate the test assertion but do not fail immediately, only increment failed test counter
     **/
    def testAndCheckLater={cl->
        try {
            cl()
        } catch (PowerAssertionError pae) {
            _counter++
            error(pae.getMessage())
        }
    }
	
    /**
     * fail now if the failed test counter if not empty
     */
    def checkResults() {
        if (_counter>0) {
            throw new AssertionError("$_counter test(s) failed")
        }
        _counter=0;
    }


}
