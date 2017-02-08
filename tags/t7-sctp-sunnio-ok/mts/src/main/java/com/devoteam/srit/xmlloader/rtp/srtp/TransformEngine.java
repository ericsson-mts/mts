/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package com.devoteam.srit.xmlloader.rtp.srtp;

/**
 * TransformEngine defines how to get PacketTransformer for RTP/RTCP packets.
 * We can use a single PacketTransformer for both RTP/RTCP packets and also we
 * can use two different PacketTransformers for RTP and RTCP. 
 * 
 * @author Bing SU (nova.su@gmail.com)
 */
public interface TransformEngine
{
    /**
     * Get the PacketTransformer for RTP packets
     *
     * @return the PacketTransformer for RTP packets
     */
    public PacketTransformer getRTPTransformer();

    /**
     * Get the PacketTransformer for RTCP packets
     *
     * @return the PacketTransformer for RTCP packets
     */
    public PacketTransformer getRTCPTransformer();
}
