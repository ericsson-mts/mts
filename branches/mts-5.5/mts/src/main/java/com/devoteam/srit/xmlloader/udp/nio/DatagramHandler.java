/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.udp.nio;

import java.nio.channels.SelectionKey;

/**
 *
 * @author gpasquiers
 */
public interface DatagramHandler
{
    public void init(SelectionKey selectionKey);

    public void inputReady();

    public void outputReady();
}
