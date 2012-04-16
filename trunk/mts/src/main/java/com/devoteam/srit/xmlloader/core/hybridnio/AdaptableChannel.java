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

package com.devoteam.srit.xmlloader.core.hybridnio;

import java.nio.channels.Channel;

/**
 * An interface to adaptable channels. Adaptable channels may add new
 * functionality, such as SSL support, to other adaptee channels that they adapt
 * to. As selectors accept channel implementations from the selector provider
 * only, registration to as selector must typically be done with the adaptee
 * channel, even if the adaptable one extends <code>SelectableChannel</code>.
 * 
 * @author Ilkka Priha
 */
public interface AdaptableChannel extends Channel
{
    /**
     * Gets the adaptee of this adaptable channel.
     * 
     * @return the adaptee channel.
     */
    public Channel getAdapteeChannel();
}
