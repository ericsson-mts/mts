package dk.i1.diameter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 64-bit floating point AVP
 */
public class AVP_Float64 extends AVP {
	public AVP_Float64(AVP a) throws InvalidAVPLengthException {
		super(a);
		// FH BUG
		//if(a.queryPayloadSize()!=4)
		if(a.queryPayloadSize()!=8)
			throw new InvalidAVPLengthException(a);
	}
	
	public AVP_Float64(int code, double value) {
		super(code,double2byte(value));
	}
	public AVP_Float64(int code, int vendor_id, double value) {
		super(code,vendor_id,double2byte(value));
	}
	
	public void setValue(double value) {
		setPayload(double2byte(value));
	}
	
	public double queryValue() {
		byte v[] = queryPayload();
		// FH BUG
		//ByteBuffer bb = ByteBuffer.allocate(4);
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.put(v);
		bb.rewind();
		return bb.getDouble();
	}
	
	static private final byte[] double2byte(double value) {
		// FH BUG
		//ByteBuffer bb = ByteBuffer.allocate(4);
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putDouble(value);
		bb.rewind();
		// FH BUG
		//byte v[] = new byte[4];
		byte v[] = new byte[8];
		bb.get(v);
		return v;
	}
}
