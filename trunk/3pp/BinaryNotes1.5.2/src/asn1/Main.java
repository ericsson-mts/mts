/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package asn1;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import org.bn.coders.IASN1PreparedElement;
import org.bn.coders.IASN1PreparedElementData;
import org.bn.CoderFactory;
import org.bn.IDecoder;
import org.bn.IEncoder;
import org.bn.coders.ber.BEREncoder;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 *
 * @author gansquer
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, IllegalArgumentException, InstantiationException, Exception {


        //ouvrir fichier xml
        String xmlFileName = "src/asn1/test_DATA.xml";
        //String xmlFileName = "D:/ASN1/test_DATA.xml"; autre fichier pour tester
        Document document = XmlToAsn1.getDocumentXML(xmlFileName);
        Element root = document.getRootElement();
        XmlToAsn1 xml_asn1 = new XmlToAsn1();

        //chercher la classe et l'instancier
        Object objClass = xml_asn1.instanceClass(root.getName());

        //ETAPE 1: transformer le XML en IASN1PreparedElement
        xml_asn1.initObject(objClass, root);
        IASN1PreparedElement elem = (IASN1PreparedElement) objClass;
        IASN1PreparedElementData elemData = elem.getPreparedData();

        if ((((Data) elem).simpleOctType == null))
        {
        	System.out.println("ERROR : simpleOctType = null");
        }

        if (!(((Data) elem).simpleOctType[0] == 59))
        {
        	System.out.println("ERROR : simpleOctType !=");
        }
        
        if ((((Data) elem).binary == null))
        {
        	System.out.println("ERROR : binary = null");
        }

        if ((((Data) elem).binary.value == null))
        {
        	System.out.println("ERROR : value = null");
        }
        
        if (!(((Data) elem).binary.value[0] == 1111))
        {
        	System.out.println("ERROR : value !=");
        }

        //ETAPE 2: transformer en byte[]

        //ETAPE VIRTUELLE : envoi sur réseau

        //ETAPE 3: parser les byte[] en IASN1PreparedElement

        //ETAPE 4: transformer les IASN1PreparedElement en XML
    }
}
