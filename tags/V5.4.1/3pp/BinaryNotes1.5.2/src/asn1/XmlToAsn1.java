/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package asn1;

import gp.utils.arrays.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 *
 * @author gansquer
 */
public class XmlToAsn1 {

    public XmlToAsn1() {
    }

    public static Document getDocumentXML(final String xmlFileName) {
        Document document = null;
        SAXReader reader = new SAXReader();
        try {
            document = reader.read(xmlFileName);
        }
        catch (DocumentException e) {
            System.out.println("fichier XML non présent");
        }
        return document;
    }

    public void initObject(Object objClass, Element root) throws InvocationTargetException, ClassNotFoundException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InstantiationException {
        // parsing XML
        List<Element> children = root.elements();
        for (Element element : children) {
            System.out.println("XML Nom: " + element.getName());
            if (!element.getTextTrim().isEmpty()) {
                System.out.println("XML Valeur :" + element.getTextTrim());
            }
            Class thisClass = objClass.getClass();
            System.out.println("CLASSE :" + thisClass.getName());
            Field field = this.findField(objClass, element);
            initField(objClass, element, field);
        }
    }

    public Object instanceClass(String Classe) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        String ClasseName = "asn1.";
        if (!Classe.contains(ClasseName)) {
            ClasseName = ClasseName + Classe;
        }
        else {
            ClasseName = Classe;
        }
        Class thisClass = Class.forName(ClasseName);
        System.out.println("INTROSPECTION : " + thisClass.toString());
        // get an instance
        Object iClass = thisClass.newInstance();
        return iClass;
    }

    public Field findField(Object objClass, Element element) {
        for (Field field : objClass.getClass().getDeclaredFields()) {
            if (element.getName().equals("instance")) {
                return field;
            }
            if (field.getName().equals(element.getName())) {
                System.out.println("FIELD : " + field.getName() + " est le field correspondant");
                return field;
            }
        }

        System.out.println("FIND FIELD : pas trouvé");
        return null;
    }

    public Object parseField(Element element, String type) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        System.out.println("TYPE: " + type);
        if (type.equals("java.lang.Boolean")) {
            return Boolean.valueOf(element.getTextTrim()).booleanValue();
        }
        else if (type.equals("java.lang.String")) {
            return element.getTextTrim();
        }
        else if (type.equals("java.lang.Integer")) {
            return Integer.parseInt(element.getTextTrim());
        }
        else if (type.equals("java.lang.Float")) {
            return Float.parseFloat(element.getTextTrim());
        }
        else if (type.equals("java.lang.Short")) {
            return Short.parseShort(element.getTextTrim());
        }
        else if (type.equals("java.lang.Long")) {
            return Long.parseLong(element.getTextTrim());
        }
        else if (type.equals("java.lang.Byte")) {
            return Byte.parseByte(element.getTextTrim());
        }
        else if (type.equals("byte[]")) {
            return Array.fromHexString(element.getTextTrim()).getBytes();
        }
        else {
            Object obj = Class.forName(type).newInstance();
            //return Class.forName(type).newInstance();
            Object objComplexClass = this.instanceClass(obj.getClass().getName());
            initObject(objComplexClass, element);
            return obj;
        }
    }

    public void initField(Object objClass, Element element, Field field) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        //si le champ est privé, pour y accéder
        field.setAccessible(true);

        //pour ne pas traiter les static
        if (field.toGenericString().contains("static")) {
            return;
        }
        if (field.getType().getCanonicalName().contains("Collection")) {
            // type DANS la collection
            System.out.println("Type collection : " + field.getGenericType());

            //Récupérer le type des élements de la collection
            Type[] elementParamTypeTab = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();

            //Exception si la collection n'a pas un seul argument
            if(elementParamTypeTab.length != 1){
                throw new RuntimeException("Message d'erreur");
            }

            Class collectionElementType = (Class) elementParamTypeTab[0];

            // creer la collection
            ArrayList<Object> listInstance = new ArrayList<Object>();

            // parcourir les enfants <instance> de element
            List<Element> children = element.elements("instance");
            for (Element elementInstance : children) {
                // pour chaque <instance>
                listInstance.add(parseField(elementInstance, collectionElementType.getCanonicalName()));
            }
            System.out.println("COLLECTION : set dans le field");
            // set la collection dans le field

            field.set(objClass, listInstance);
        }
        else {
            field.set(objClass, parseField(element, field.getType().getCanonicalName()));
        }
    }
}
