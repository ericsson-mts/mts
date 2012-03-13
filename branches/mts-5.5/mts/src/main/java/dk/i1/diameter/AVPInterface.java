/*
 * AVPInterface.java
 *
 * Created on 4 octobre 2007, 15:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dk.i1.diameter;

/**
 *
 * @author gpasquiers
 */
public class AVPInterface
{
    public static byte[] encode(AVP avp)
    {
        return avp.encode();
    }
}
