/**
 * Copyright (c) 2006 The Norther Organization (http://www.norther.org).
 *
 * Tammi is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * Tammi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Tammi; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02111-1307 USA
 *
 * $Id: AdaptableChannel.java,v 1.7 2008/06/18 14:06:29 ipriha Exp $
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
