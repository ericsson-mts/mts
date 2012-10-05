package dk.i1.diameter.node;
import java.net.InetAddress;
import java.util.Collection;

public abstract class Connection {
	NodeImplementation node_impl;
	public Peer peer;  //initially null
	public String host_id; //always set, updated from CEA/CER
	public ConnectionTimers timers;
	public ConnectionKey key;
	private int hop_by_hop_identifier_seq;
	
	public enum State {
		connecting,
		connected_in,  //connected, waiting for cer
		connected_out, //connected, waiting for cea
		tls,           //CE completed, negotiating TLS
		ready,         //ready
		closing,       //DPR sent, waiting for DPA
		closed
	}
	public State state;
	
	public Connection(NodeImplementation node_impl, long watchdog_interval, long idle_timeout) {
		this.node_impl = node_impl;
		timers = new ConnectionTimers(watchdog_interval,idle_timeout);
		key = new ConnectionKey();
		hop_by_hop_identifier_seq = new java.util.Random().nextInt();
		state = State.connected_in;
	}
	
	public synchronized int nextHopByHopIdentifier() {
		return hop_by_hop_identifier_seq++;
	}
	
	abstract InetAddress toInetAddress(); //todo: eliminate
	
	abstract void sendMessage(byte[] raw);
	
	abstract Object getRelevantNodeAuthInfo();
	
	abstract Collection<InetAddress> getLocalAddresses();
	
	abstract Peer toPeer();
	
	long watchdogInterval() {
		return timers.cfg_watchdog_timer;
	}
}
