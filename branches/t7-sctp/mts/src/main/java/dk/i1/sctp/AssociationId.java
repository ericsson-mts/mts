package dk.i1.sctp;

/**Encapsulates the ID of an association.
 * Association ID are guaranteed to be unique for a SCTPSocket for the lifetime of the association.
 * An association ID may be reused after the association has been shut down.
 * An association ID is not unique across multiple SCTPSockets
 */
public final class AssociationId {
	long id;
	public AssociationId(long id_) {	//Devoteam public
		this.id = id_;
	}
	public int hashCode() { return (int)id; }
	public boolean equals(Object o) { return ((AssociationId)o).id==id; }
	public String toString() { return String.valueOf(id); }
	
	/**The default association.
	 *Only used in special circumstances. It is best to read the SCPT socket API draft for details
	 *(Yes, this is the sctp_assoc_t '0')
	 */
	public static AssociationId default_ = new AssociationId(0);
};
