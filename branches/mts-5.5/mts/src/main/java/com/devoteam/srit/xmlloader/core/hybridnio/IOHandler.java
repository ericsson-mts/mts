/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.hybridnio;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

/**
 *
 * @author gpasquiers
 */
public interface IOHandler
{
    public void init(SelectionKey selectionKey, SelectableChannel channel);

    public void inputReady();

    public void outputReady();

    public void connectReady();

    public void acceptReady();
}
