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
/**
 * base class of the groovy source code declared in every groovy operation
 * This base class can be used to import common classes, to add common fields and methods,  to execute any action before/after the groovy script
 * 
 * @author mickael.jezequel@orange.com
 *
 */
abstract class CustomScript extends MTSScript {
	
	
    /**
     * this method is called when the groovy script is executed
     * here can go any common pre/post action
     */
    def run() {
        runCode()
    }
 
    // Abstract method as placeholder for
    // the actual groovy script code to run.
    abstract def runCode()
}
