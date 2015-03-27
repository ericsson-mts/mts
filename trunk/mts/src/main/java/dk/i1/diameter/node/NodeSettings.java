package dk.i1.diameter.node;
import java.util.Random;

/**
 * Configuration for a node.
 * NodeSettings contain the settings for a node including node ID, capabilities, etc.
 * A NodeSettings instance is required for initializing {@link Node} and {@link NodeManager}
 * <p>Example for constructing a NodeSettings instance:
 * <pre>
 *  Capability capability = new Capability();
 *  capability.addAuthApp(...);
 *  capability.addAcctApp(...);
 *  
 *  NodeSettings node_settings;
 *  try {
 *      node_settings  = new NodeSettings(
 *          "somehost.example.net", "example.net",
 *          0, //vendor-id. 0 is not a valid value.
 *          capability,
 *          3868,
 *          "ExampleNet gateway", 0x01000000);
 *  } catch (InvalidSettingException ex) {
 *      System.out.println(ex.toString());
 *      return;
 *  }
 *</pre>
 * Does and donts:
 * <ul>
 *    <li>The Diameter host ID will normally be the same as the fully-qualified
 *        domain name of your host. But make this configurable because if there
 *        are two nodes running on the host they must have distinct IDs.
 *        NodeSettings will reject attempts to use a non-qualified name.
 *    </li>
 *    <li>The realm should be the non-first part of host ID</li>
 *    <li>Use your own vendor ID. Don't make up a value. Apply for one at IANA (it is free).
 *        Check if your organization does not already have one
 *        (<a href="http://www.iana.org/assignments/enterprise-numbers">www.iana.org/assignments/enterprise-numbers</a>).
 *        Do not make this configurable.
 *    <li>You must add the applications to the capability before constructing
 *        a NodeSettings.
 *    </li>
 *    <li>You will normally specify 3868 as the port even for "client programs".
 *        Remember to make this configurable so two nodes can run on the same host.
 *    </li>
 *    <li>The product name should be a stable name, and only change if the product
 *        changes and not if some marketing guy invents a new name.
 *        Do not make this configurable.
 *    </li>
 *    <li>Firmware revision is up to you to decide. You should increment this for each release.</li>
 * </ul>
 */
public class NodeSettings {
	private String host_id;
	private String realm;
	private int vendor_id;
	private Capability capabilities;
	private int port;
	private String product_name;
	private int firmware_revision;
	private long watchdog_interval;
	private long idle_close_timeout;
	private Boolean use_tcp;
	private Boolean use_sctp;
	
	/**
	 * Constructor for NodeSettings.
	 * @param host_id The Diameter host identity.
	 * @param vendor_id Your IANA-assigned "SMI Network Management Private Enterprise Code"
	 * @param realm The Diameter realm.
	 * @param capabilities The capabilities of this node.
	 * @param port The port to listen on. Use 0 to specify that this node should not listen for incoming connections.
	 * @param product_name The name of this product eg. "FooBar gateway"
	 * @param firmware_revision The "firmware" revision ie. the version of you product. Use 0 to specify none.
	 */
	public NodeSettings(String host_id, String realm,
	                    int vendor_id,
			    Capability capabilities,
			    int port,
			    String product_name, int firmware_revision)
		throws InvalidSettingException
	{
		int i;
		if(host_id==null)
			throw new InvalidSettingException("null host_id");
		i = host_id.indexOf('.');
		if(i==-1)
			throw new InvalidSettingException("host_id must contains at least 2 dots");
		if(host_id.indexOf('.',i+1)==-1)
			throw new InvalidSettingException("host_id must contains at least 2 dots");
		this.host_id = host_id;
		
		i = realm.indexOf('.');
		if(i==-1)
			throw new InvalidSettingException("realm must contain at least 1 dot");
		this.realm = realm;
		
		if(vendor_id==0)
			throw new InvalidSettingException("vendor_id must not be non-zero. (It must be your IANA-assigned \"SMI Network Management Private Enterprise Code\". See http://www.iana.org/assignments/enterprise-numbers)");
		this.vendor_id = vendor_id;
		
		if(capabilities.isEmpty())
			throw new InvalidSettingException("Capabilities must be non-empty");
		this.capabilities = capabilities;
		if(port<0 || port>65535)
			throw new InvalidSettingException("listen-port must be 0..65535");
		this.port = port;
		
		if(product_name==null)
			throw new InvalidSettingException("product-name cannot be null");
		this.product_name = product_name;
		this.firmware_revision = firmware_revision;
		this.watchdog_interval = 30*1000;
		this.idle_close_timeout = 7*24*3600*1000;
	}
	
	/**Returns the configured host ID*/
	public String hostId() {
		return host_id;
	}
	
	/**Returns the configured realm*/
	public String realm() {
		return realm;
	}
	
	/**Returns the configured vendor ID*/
	public int vendorId() {
		return vendor_id;
	}
	
	/**Returns the configured capabilities*/
	public Capability capabilities() {
		return capabilities;
	}
	
	/**Returns the configured listen port. 0 if not listening*/
	public int port() {
		return port;
	}
	
	/**Returns the product name*/
	public String productName() {
		return product_name;
	}
	
	/**Returns the firmware revision*/
	public int firmwareRevision() {
		return firmware_revision;
	}
	
	/**Returns the desired DWR interval (in milliseconds). The default
	 * interval is 30 seconds as per RFC3539 section 3.4.1
	 * @since 0.9.3
	 */
	public long watchdogInterval() {
		return watchdog_interval;
	}
	
	/**Sets the desired DWR/DWA interval. The default interval is 30 seconds
	 * as per RFC3539 section 3.4.1
	 * @param interval DWR interval in milliseconds
	 * @throws InvalidSettingException If the interval is less than 6000 milliseconds
	 * @since 0.9.3
	 */
	public void setWatchdogInterval(long interval) throws InvalidSettingException {
		if(interval<6*1000)
			throw new InvalidSettingException("watchdog interval must be at least 6 seconds. RFC3539 section 3.4.1 item 1");
		this.watchdog_interval = interval;
	}
	
	/**Returns the idle timeout (in milliseconds)
	 * @since 0.9.3
	 */
	public long idleTimeout() {
		return idle_close_timeout;
	}
	
	/**Sets the idle close timeout. The default idle timeout is 7 days,
	 * after which the connection will closed with reason='busy' unless
	 * there has been non-watchdog traffic on the connection.
	 * @param timeout Timeout in milliseconds. If 0 then idle timeout is disabled and connections will be kept open.
	 * @throws InvalidSettingException If timeout is negative.
	 * @since 0.9.3
	 */
	public void setIdleTimeout(long timeout) throws InvalidSettingException {
		if(timeout<0)
			throw new InvalidSettingException("idle timeout cannot be negative");
		this.idle_close_timeout = timeout;
	}
	
	/**Returns the setting for using TCP.
	 * @return A boolean object, or null if not set.
	 * @since 0.9.5
	 */
	public Boolean useTCP() {
		return use_tcp;
	}
	/** Change the setting for using TCP
	 * Sets the setting to the spciefied value, which can be null.
	 * When the setting is:
	 <dl>
	 <dt>true</dt><dd>then the stack will create a TCP sub-node.</dd>
	 <dt>false</dt><dd>then the stack will not create a TCP sub-node.</dd>
	 <dt>null</dt><dd>then the stack will use the a property instead (see {@link Node} for details}.</dd>
	 </dl>
	 * @param use_tcp New TCP use setting. Can be null.
	 * @since 0.9.5
	 */
	public void setUseTCP(Boolean use_tcp) {
		this.use_tcp = use_tcp;
	}

	/**Returns the setting for using SCTP.
	 * @return A boolean object, or null if not set.
	 * @since 0.9.5
	 */
	public Boolean useSCTP() {
		return use_sctp;
	}
	/** Change the setting for using SCTP
	 * Sets the setting to the spciefied value, which can be null.
	 * When the setting is:
	 <dl>
	 <dt>true</dt><dd>then the stack will create a SCTP sub-node.</dd>
	 <dt>false</dt><dd>then the stack will not create a SCTP sub-node.</dd>
	 <dt>null</dt><dd>then the stack will use the a property instead (see {@link Node} for details}.</dd>
	 </dl>
	 * @param use_sctp New SCTP use setting. Can be null.
	 * @since 0.9.5
	 */
	public void setUseSCTP(Boolean use_sctp) {
		this.use_sctp = use_sctp;
	}
}
