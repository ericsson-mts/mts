/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.hybridnio;

/**
 *
 * @author gpasquiers
 */
public interface HybridSocketInputHandler
{
    /**
     *
     * @param hybridSocket
     * @return true if it wants to continue receiving notifications
     */
    public boolean handle(HybridSocket hybridSocket);

    public void init(HybridSocket hybridSocket);
}
