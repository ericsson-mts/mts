package dk.i1.sctp;

public class sctp_event_subscribe {
	/**Receive sctp_sndrcvinfo per chunk.
	 *You definetely want this on a OneToManySCTPSocket.
	 */
	public boolean sctp_data_io_event;
	/**Receive association change notifications.
	 *You probably want this on a OneToManySCTPSocket.
	 *This results in 
	 *{@link SCTPNotificationAssociationChangeCommUp},
	 *{@link SCTPNotificationAssociationChangeCommLost},
	 *{@link SCTPNotificationAssociationChangeRestart},
	 *{@link SCTPNotificationAssociationChangeShutdownComplete} and
	 *{@link SCTPNotificationAssociationChangeCantStartAssociation}
	 *events being generated.
	 */
	public boolean sctp_association_event;
	/**Receive {@link SCTPNotificationPeerAddressChange} events.*/
	public boolean sctp_address_event;
	/**Receive {@link SCTPNotificationSendFailed} events.
	 * You probably want this enable if you can use the fact that the remote peer probably did not receive an in-flight message. (you probably also need to use {@link sctp_sndrcvinfo#sinfo_context})
	 */
	public boolean sctp_send_failure_event;
	public boolean sctp_peer_error_event;
	public boolean sctp_shutdown_event;
	public boolean sctp_partial_delivery_event;
	public boolean sctp_adaptation_layer_event;
	public boolean sctp_authentication_event;	// Devoteam 
	
	public sctp_event_subscribe() {
		sctp_data_io_event = false;
		sctp_association_event = false;
		sctp_address_event = false;
		sctp_send_failure_event = false;
		sctp_peer_error_event = false;
		sctp_shutdown_event = false;
		sctp_partial_delivery_event = false;
		sctp_adaptation_layer_event = false;
		sctp_authentication_event = false;		// Devoteam 
	}
}
