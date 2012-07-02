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
 * $Id: SecureChannel.java,v 1.7 2008/06/18 14:06:29 ipriha Exp $
 */

package com.devoteam.srit.xmlloader.core.hybridnio;

import java.io.IOException;
import java.nio.channels.Channel;

/**
 * An interface to secure channels. It is usually recommended to perform the
 * initial SSL handshake with the separate handshake method, although the
 * implementation may support embedded handshaking within the read and write
 * methods. The shutdown process may also be started separately but should be
 * called automatically by the close method of the implementation.
 * 
 * @author Ilkka Priha
 */
public interface SecureChannel extends Channel
{
    /**
     * Returns true if this channel is finished with handshaking.
     * 
     * @return true if finished, false otherwise.
     */
    public boolean finished();

    /**
     * Returns the number of encrypted bytes not yet flushed.
     * 
     * @return the number of encrypted bytes.
     */
    public int encrypted();

    /**
     * Returns the number of decrypted bytes not yet read.
     * 
     * @return the number of decrypted bytes.
     */
    public int decrypted();

    /**
     * Starts or continues handshaking with the specified operations.
     * 
     * @param ops the current ready operations set.
     * @return the interest set to continue or 0 if finished.
     * @throws IOException on I/O errors.
     */
    public int handshake(int ops) throws IOException;

    /**
     * Starts the shutdown sequence but does not close the channel.
     * 
     * @return true if finished, false otherwise.
     * @throws IOException on I/O errors.
     */
    public boolean shutdown() throws IOException;

    /**
     * Flushes remaining encrypted bytes if any.
     * 
     * @throws IOException on I/O errors.
     */
    public void flush() throws IOException;
}
