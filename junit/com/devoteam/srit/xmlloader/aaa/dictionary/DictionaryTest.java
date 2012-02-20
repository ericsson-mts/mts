/*
 * DictionaryTest.java
 * JUnit based test
 *
 * Created on 23 avril 2007, 13:42
 */

package com.devoteam.srit.xmlloader.aaa.dictionary;

import com.devoteam.srit.xmlloader.core.Tester;
import junit.framework.*;

/**
 *
 * @author gpasquiers
 */
public class DictionaryTest extends TestCase {
    
    public DictionaryTest(String testName) {
        super(testName);
        try
        {
            if(Tester.getInstance() == null)
            Tester.buildInstance(Tester.Mode.MODE_TEXT);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void setUp() throws Exception {
        
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of getInstance method, of class com.devoteam.srit.sigloader.protocol.aaa.dictionary.Dictionary.
     */
    public void testGetInstance() throws Exception {
        System.out.println("getInstance");
        
        Dictionary result = Dictionary.getInstance();
        assertNotNull(result);
    }

    /**
     * Test of getApplication method, of class com.devoteam.srit.sigloader.protocol.aaa.dictionary.Dictionary.
     */
    public void testGetApplication() throws Exception {
        System.out.println("getApplication");
        
        String key = "Diameter Common Messages";
        Dictionary instance = Dictionary.getInstance();
        
        Application result = instance.getApplication(key);
        assertNotNull(result);
    }

    /**
     * Test of getVendorDefByName method, of class com.devoteam.srit.sigloader.protocol.aaa.dictionary.Dictionary.
     */
    public void testGetVendorDefByName() throws Exception {
        System.out.println("getVendorDefByName");
        
        String name = "TGPP";
        String applicationId = "Diameter Common Messages";
        Dictionary instance = Dictionary.getInstance();
        
        VendorDef result = instance.getVendorDefByName(name, applicationId);
        assertEquals(10415, result.get_code());
    }

    /**
     * Test of getVendorDefByCode method, of class com.devoteam.srit.sigloader.protocol.aaa.dictionary.Dictionary.
     */
    public void testGetVendorDefByCode() throws Exception {
        System.out.println("getVendorDefByCode");
        
        int code = 10415;
        String applicationId = "Diameter Common Messages";
        Dictionary instance = Dictionary.getInstance();
        
        VendorDef result = instance.getVendorDefByCode(code, applicationId);
        assertEquals("TGPP", result.get_vendor_id());
    }

    /**
     * Test of getTypeDefByName method, of class com.devoteam.srit.sigloader.protocol.aaa.dictionary.Dictionary.
     */
    public void testGetTypeDefByName() throws Exception {
        System.out.println("getTypeDefByName");
        
        String name = "OctetString";
        String applicationId = "Diameter Common Messages";
        Dictionary instance = Dictionary.getInstance();
        
        TypeDef result = instance.getTypeDefByName(name, applicationId);
        assertEquals("OctetString", result.get_type_name());
        
    }

    /**
     * Test of getCommandDefByName method, of class com.devoteam.srit.sigloader.protocol.aaa.dictionary.Dictionary.
     */
    public void testGetCommandDefByName() throws Exception {
        System.out.println("getCommandDefByName");
        
        String name = "Abort-Session";
        String applicationId = "Diameter Common Messages";
        Dictionary instance = Dictionary.getInstance();
        
        CommandDef result = instance.getCommandDefByName(name, applicationId);
        assertEquals(274, result.get_code());

    }

    /**
     * Test of getCommandDefByCode method, of class com.devoteam.srit.sigloader.protocol.aaa.dictionary.Dictionary.
     */
    public void testGetCommandDefByCode() throws Exception {
        System.out.println("getCommandDefByCode");
        
        int code = 274;
        String applicationId = "Diameter Common Messages";
        Dictionary instance = Dictionary.getInstance();
        
        CommandDef result = instance.getCommandDefByCode(code, applicationId);
        assertEquals("Abort-Session", result.get_name());
    }

    /**
     * Test of getAvpDefByCode method, of class com.devoteam.srit.sigloader.protocol.aaa.dictionary.Dictionary.
     */
    public void testGetAvpDefByCode() throws Exception {
        System.out.println("getAvpDefByCode");
        
        int code = 1;
        String applicationId = "Diameter Common Messages";
        Dictionary instance = Dictionary.getInstance();
        
        AvpDef expResult = null;
        AvpDef result = instance.getAvpDefByCode(code, applicationId);
        assertEquals("User-Name", result.get_name());
        assertEquals("UTF8String", result.get_type().get_type_name());
        
    }

    /**
     * Test of getAvpDefByName method, of class com.devoteam.srit.sigloader.protocol.aaa.dictionary.Dictionary.
     */
    public void testGetAvpDefByName() throws Exception {
        System.out.println("getAvpDefByName");
        
        {
            String name = "User-Name";
            String applicationId = "Diameter Common Messages";
            Dictionary instance = Dictionary.getInstance();

            AvpDef result = instance.getAvpDefByName(name, applicationId);
            assertEquals(1, result.get_code());
        }

        {
            String name = "Result-Code";
            String applicationId = "Diameter Common Messages";
            Dictionary instance = Dictionary.getInstance();

            AvpDef result = instance.getAvpDefByName(name, applicationId);
            assertEquals(268, result.get_code());
            assertEquals(2001, result.getEnumCodeByName("DIAMETER_SUCCESS"));
        }

        {
            String name = "Experimental-Result";
            String applicationId = "Diameter Common Messages";
            Dictionary instance = Dictionary.getInstance();

            AvpDef result = instance.getAvpDefByName(name, applicationId);
            assertEquals(297, result.get_code());
            assertEquals(298, result.getGroupedAvpDefByName("Experimental-Result-Code").get_code()) ;
            assertEquals(2001, result.getGroupedAvpDefByName("Experimental-Result-Code").getEnumCodeByName("DIAMETER_FIRST_REGISTRATION")) ;
        }
    }

    /**
     * Test of isInteger method, of class com.devoteam.srit.sigloader.protocol.aaa.dictionary.Dictionary.
     */
    public void testIsInteger() {
        System.out.println("isInteger");

        {
            String string = "123";

            boolean expResult = true;
            boolean result = Dictionary.isInteger(string);
            assertEquals(expResult, result);
        }
        
        {
            String string = "a123";

            boolean expResult = false;
            boolean result = Dictionary.isInteger(string);
            assertEquals(expResult, result);
        }

        {
            String string = null ;
            boolean expResult = false;
            
            boolean result = Dictionary.isInteger(string);
            assertEquals(expResult, result);
        }

    }    
}
