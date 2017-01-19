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

package com.devoteam.srit.xmlloader.core.log;

public interface TextListenerProvider
{
    /**
     * This interface defines a TextListener provider. This class returns an
     * implementation of the TextListener interface.
     *
     * The returned TextListener has to be coherent towards the passed key.
     *
     * This key can take the following values:
     *   instance of a ScenarioRunner
     *  (instance of a TestcaseRunner)
     *   null : application (global) logger
     * @param key
     * @return
     */
    public TextListener provide(TextListenerKey key);


    /**
     * Free resources used by the TextListener disigned by the "key".
     * @param key
     */
    public void dispose(TextListenerKey key);
}
