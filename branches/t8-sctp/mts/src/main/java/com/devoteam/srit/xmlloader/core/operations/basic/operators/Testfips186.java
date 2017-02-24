package com.devoteam.srit.xmlloader.core.operations.basic.operators;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.DigestArray;
import gp.utils.arrays.SupArray;

public class Testfips186 {
	
	public static byte[] onesixty_add_mod(byte[] a, byte[] b)
    {
		//uint32_t s;
		int s;
		//int i, carry;
		byte carry;
		byte[] sum = new byte[20];
		
		/*	for(i=0; i<20; i++) {  */
		carry = 0;
		for (int i=19; i>=0; i--) 
		{		
			//s = a->p[i] + b->p[i] + carry;
			int intA = (int) (a[i]) & 0xff;
			int intB = (int) (b[i]) & 0xff;
			s = intA + intB + carry;
			//sum->p[i] = s & 0xff;
			sum[i] = (byte) (s & 0xff);
			//carry = s >> 8;
			carry = (byte) (s >> 8);
		}
		return sum;
    }
	
	
    static byte[] sha1(byte[] bytes) 
    {        
    	MessageDigest mDigest = null;
    	try
    	{
    		mDigest = MessageDigest.getInstance("SHA1");
    	}
    	catch (Exception e)
    	{
    		//nothing TODO
    	}
    	byte[] result = mDigest.digest(bytes);        
    	StringBuffer sb = new StringBuffer();        
    	for (int i = 0; i < result.length; i++) 
    	{            
    		sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));        
    	}                 
    	return result;    
    }
    
    /**
     * 
     * 
     * see http://www.opensource.apple.com/source/eap8021x/eap8021x-198/EAP8021X.fproj/fips186prf.c
     * 
     * @param args the arguments
     */
    public static void main(String[] args) 
    {

    	byte[] b = new byte[] {0x67, 0x45, 0x23, 0x10};
    	long l = (long)b[0] & 0xFF;
    	l += ((long)b[1] & 0xFF) << 8;
    	l += ((long)b[2] & 0xFF) << 16;
    	l += ((long)b[3] & 0xFF) << 24;
    	Long lo = (Long) l;
    	String strLo = lo.toHexString(lo);

    	String data = "BD029BBE7F51960BCF9EDB2B61F06F0FEB5A38B6";
		Array arrayData = Array.fromHexString(data);
		byte[] w0 = null;
    	try 
    	{
        	MessageDigest md = MessageDigest.getInstance("SHA");
    	     md.update(data.getBytes());
    	     MessageDigest tc1 = (MessageDigest) md.clone();
    	     w0 = tc1.digest();
    	} 
    	catch (Exception cnse) 
    	{
    	     //throw new DigestException("couldn't make digest of partial content");
    	}
	     Array arrayW0 = new DefaultArray(w0);    	 
    	
    	/*
    	__private_extern__
    	void fips186_2prf(uint8_t mk[20], uint8_t finalkey[160])
    	{
    		fr_SHA1_CTX context;
    		int j;
    		onesixty xval, xkey, w_0, w_1, sum, one;
    		uint8_t *f;
    		uint8_t zeros[64];
		*/
    		/*
    		 * let XKEY := MK,
    		 *
    		 * Step 3: For j = 0 to 3 do
    	         *   a. XVAL = XKEY
    	         *   b. w_0 = SHA1(XVAL)
    	         *   c. XKEY = (1 + XKEY + w_0) mod 2^160
    	         *   d. XVAL = XKEY
    	         *   e. w_1 = SHA1(XVAL)
    	         *   f. XKEY = (1 + XKEY + w_1) mod 2^160
    	         * 3.3 x_j = w_0|w_1
    		 *
    		 */
    	
    		//memcpy(&xkey, mk, sizeof(xkey));
    		String strXKEY = "BD029BBE7F51960BCF9EDB2B61F06F0FEB5A38B6";
    		Array xkey = Array.fromHexString(strXKEY);
    		
    		//String hello = "Hello";
    		//Array xkey = new DefaultArray(hello.getBytes());
    		//String strXKEY = "48656C6C6F";
    		
    		/* make the value 1 */
    		//memset(&one,  0, sizeof(one));
    		//one.p[19]=1;
    		String strONE =  "0000000000000000000000000000000000000001";
    		Array one = Array.fromHexString(strONE);
    		
    		//f=finalkey;
    		SupArray f = new SupArray();

    		for (int j=0; j<4; j++) 
    		{
    			/*   a. XVAL = XKEY  */
    			//xval = xkey;
    			Array xval = new DefaultArray(xkey.getBytes());

    			/*   b. w_0 = SHA1(XVAL)  */
    			//fr_SHA1Init(&context);    		
    			//memset(zeros, 0, sizeof(zeros));
    			//memcpy(zeros, xval.p, 20);
    			//fr_SHA1Transform(&context, zeros);
    			//fr_SHA1FinalNoLen(w_0.p, &context);
    			SupArray xvalPadding64 = new SupArray();
    			xvalPadding64.addLast(xval);
    			Array zeros44 = new DefaultArray(new byte[44]);
    			xvalPadding64.addLast(zeros44);
    			//DigestArray w_0 = new DigestArray(xval, "SHA1");
    			byte[] result = sha1(((Array) xvalPadding64).getBytes());
    			Array w_0 = new DefaultArray(result);
    			
    			/*   c. XKEY = (1 + XKEY + w_0) mod 2^160 */
    			//onesixty_add_mod(&sum,  &xkey, &w_0);
    			byte[] bytesSum = onesixty_add_mod(xkey.getBytes(), w_0.getBytes());
    			//onesixty_add_mod(&xkey, &sum,  &one);
    			bytesSum = onesixty_add_mod(bytesSum, one.getBytes());
    			xkey = new DefaultArray(bytesSum);

    			/*   d. XVAL = XKEY  */
    			//xval = xkey;
    			xval = new DefaultArray(xkey.getBytes());
    			
    			/*   e. w_1 = SHA1(XVAL)  */
    			//fr_SHA1Init(&context);
    			//memset(zeros, 0, sizeof(zeros));
    			//memcpy(zeros, xval.p, 20);
    			//fr_SHA1Transform(&context, zeros);
    			//fr_SHA1FinalNoLen(w_1.p, &context);
    			xvalPadding64 = new SupArray();
    			xvalPadding64.addLast(xval);
    			xvalPadding64.addLast(zeros44);
    			DigestArray w_1 = new DigestArray(xvalPadding64, "SHA1");

    			/*   f. XKEY = (1 + XKEY + w_1) mod 2^160 */
    			//onesixty_add_mod(&sum,  &xkey, &w_1);
    			bytesSum = onesixty_add_mod(xkey.getBytes(), w_1.getBytes());
    			//onesixty_add_mod(&xkey, &sum,  &one);
    			bytesSum = onesixty_add_mod(bytesSum, one.getBytes());
    			xkey = new DefaultArray(bytesSum);
    			
    			// 3.3 x_j = w_0|w_1
    			/* now store it away */
    			//memcpy(f, &w_0, 20);
    			//f += 20;
    			f.addLast(w_0);

    			//memcpy(f, &w_1, 20);
    			//f += 20;
    			f.addLast(w_1);
    		}
    		System.out.println(f.toString());
    	}
    	
    }

