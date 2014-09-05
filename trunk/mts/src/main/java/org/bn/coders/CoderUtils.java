/*
 Copyright 2006-2011 Abdulla Abdurakhmanov (abdulla@latestbit.com)
 Original sources are available at www.latestbit.com

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.bn.coders;

import java.io.UnsupportedEncodingException;

import java.lang.reflect.*;

import java.util.SortedMap;
import java.util.TreeMap;

import org.bn.annotations.*;
import org.bn.annotations.constraints.*;
import org.bn.metadata.*;
import org.bn.types.*;

public class CoderUtils {
    public static int getIntegerLength(int value) {
        long mask = 0x7f800000L;
        int sizeOfInt = 4;
        if (value < 0) {
            while (((mask & value) == mask) && (sizeOfInt > 1)) {
              mask = mask >> 8 ;
              sizeOfInt-- ;
            }
        }
        else {
          while (((mask & value) == 0) && (sizeOfInt > 1)) {
            mask = mask >> 8 ;
            sizeOfInt -- ;
          }
        }
        return sizeOfInt;
    }

    public static int getIntegerLength(long value) {
        long mask = 0x7f80000000000000L;
        int sizeOfInt = 8;
        if (value < 0) {
            while (((mask & value) == mask) && (sizeOfInt > 1)) {
              mask = mask >> 8 ;
              sizeOfInt-- ;
            }
        }
        else {
          while (((mask & value) == 0) && (sizeOfInt > 1)) {
            mask = mask >> 8 ;
            sizeOfInt -- ;
          }
        }
        return sizeOfInt;
    }

    public static int getPositiveIntegerLength(int value) {
        if (value < 0) {
            long mask = 0x7f800000L;
            int sizeOfInt = 4;        
            while (((mask & ~value) == mask) && (sizeOfInt > 1)) {
              mask = mask >> 8 ;
              sizeOfInt-- ;
            }
            return sizeOfInt;
        }
        else
            return getIntegerLength(value);
    }
    
    public static int getPositiveIntegerLength(long value) {
        if (value < 0) {
            long mask = 0x7f80000000000000L;
            int sizeOfInt = 8;        
            while (((mask & ~value) == mask) && (sizeOfInt > 1)) {
              mask = mask >> 8 ;
              sizeOfInt-- ;
            }
            return sizeOfInt;
        }
        else
            return getIntegerLength(value);
    }
    
    public static BitString defStringToOctetString(String bhString) {
        if(bhString.length() < 4)
            return new BitString(new byte[0]);
        if(bhString.lastIndexOf('B')==bhString.length()-1)
            return bitStringToOctetString(bhString.substring(1,bhString.length()-2));
        else
            return hexStringToOctetString(bhString.substring(1,bhString.length()-2));
    }

    private static BitString bitStringToOctetString(String bhString) {        
        boolean hasTrailBits = bhString.length()%2!=0;
        int trailBits = 0;
        byte[] resultBuf = new byte[bhString.length()/8 + (hasTrailBits?1:0)];
        int currentStrPos = 0;
        for(int i=0;i<resultBuf.length;i++) {
            byte bt = 0x00;
            int bitCnt = currentStrPos;
            while(bitCnt<currentStrPos+8 && bitCnt< bhString.length()) {
                if(bhString.charAt(bitCnt)!='0')
                    bt |=  ( 0x01 << (7- (bitCnt-currentStrPos)));
                bitCnt++;
            }
            currentStrPos+=8;            
            if(bitCnt!=currentStrPos)
                trailBits = 8 - (currentStrPos - bitCnt);
            // hi byte
            resultBuf[i] = bt;
        }
        BitString result = new BitString (resultBuf,trailBits);        
        return result;
    }

    private static BitString hexStringToOctetString(String bhString) {
           boolean hasTrailBits = bhString.length()%2!=0;
           BitString result = new BitString (new byte[bhString.length()/2 + (hasTrailBits ? 1:0)], hasTrailBits? 4:0);
           final byte hex[] = {0, 1, 2,3, 4, 5, 6, 7, 8, 9, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xA, 0xB, 0xC, 0xD, 0xE,0xF};
           
           for(int i=0;i<result.getLength();i++) {
               // high byte
               result.getValue()[i] = (byte)(hex[((int)(bhString.charAt(i*2)) - 0x30)] << 4);
               if(!hasTrailBits || (hasTrailBits && i<result.getLength()-1))                
                result.getValue()[i] |= (byte)(hex[((int)(bhString.charAt(i*2+1)) - 0x30)] & 0x0F);
           }
           return result;
    }    
    
    public static SortedMap<Integer,Field> getSetOrder(Class<?> objectClass){
        SortedMap<Integer, Field> fieldOrder = new TreeMap<Integer,Field>();
        int tagNA = -1;        
        for ( Field field : objectClass.getDeclaredFields() ) {
            ASN1Element element = field.getAnnotation(ASN1Element.class);
            if(element!=null) {
                if(element.hasTag())
                    fieldOrder.put(element.tag(),field);
                else
                    fieldOrder.put(tagNA--,field);
            }
        }
        return fieldOrder;
    }
    
    public static  int getStringTagForElement(ElementInfo elementInfo) {
        int result = UniversalTag.PrintableString;
        if(elementInfo.hasPreparedInfo()) {            
            result = ((ASN1StringMetadata)elementInfo.getPreparedInfo().getTypeMetadata()).getStringType();
        }
        else
        if(elementInfo.getAnnotatedClass().isAnnotationPresent(ASN1String.class)) {
            ASN1String value = elementInfo.getAnnotatedClass().getAnnotation(ASN1String.class);
            result = value.stringType();
        }
        else 
        if(elementInfo.getParentAnnotated()!=null && elementInfo.getParentAnnotated().isAnnotationPresent(ASN1String.class)) {
            ASN1String value = elementInfo.getParentAnnotated().getAnnotation(ASN1String.class);
            result = value.stringType();
        }
        
        return result;
    }
    
    public static void checkConstraints(long value, ElementInfo elementInfo) throws Exception {
        if(elementInfo.hasPreparedInfo()) {
            if(elementInfo.getPreparedInfo().hasConstraint())
                if(!elementInfo.getPreparedInfo().getConstraint().checkValue(value))
                    throw new Exception("Value of '"+elementInfo.getAnnotatedClass().toString()+"' out of bounds");
        }
        else {
            if(elementInfo.getAnnotatedClass().isAnnotationPresent(ASN1ValueRangeConstraint.class)) {
                ASN1ValueRangeConstraint constraint = elementInfo.getAnnotatedClass().getAnnotation(ASN1ValueRangeConstraint.class);
                if(value> constraint.max() || value<constraint.min() )
                    throw new Exception("Value of '"+elementInfo.getAnnotatedClass().toString()+"' out of bounds");
            }
            if(elementInfo.getAnnotatedClass().isAnnotationPresent(ASN1SizeConstraint.class)) {
                ASN1SizeConstraint constraint = elementInfo.getAnnotatedClass().getAnnotation(ASN1SizeConstraint.class);
                if(value!= constraint.max())
                    throw new Exception("Value of '"+elementInfo.getAnnotatedClass().toString()+"' out of bounds");
            }        
        }
    }
    
    /*public static boolean isImplements(Class<?> objectClass, Class<?> interfaceClass) {        
        return objectClass.isAnnotationPresent(ASN1PreparedElement.class);
        /*for(Class<?> item: objectClass.getInterfaces()) {
            if(item.equals(interfaceClass)) {
                return true;
            }
        }
        return false;/
    }*/
    
    public static boolean isAnyField(Field field, ElementInfo elementInfo) {
        boolean isAny = false;
        if(elementInfo.hasPreparedInfo()) {
            isAny = elementInfo.getPreparedInfo().getTypeMetadata() instanceof ASN1AnyMetadata;
        }
        else
            isAny = field.isAnnotationPresent(ASN1Any.class);        
        return isAny;
    }

    public static boolean isNullField(Field field, ElementInfo elementInfo) {
        boolean isNull = false;
        if(elementInfo.hasPreparedInfo()) {
            isNull = elementInfo.getPreparedInfo().getTypeMetadata() instanceof ASN1NullMetadata;
        }
        else {
            isNull = field.isAnnotationPresent(ASN1Null.class);
        }        
        return isNull;
    }
        
    
    public static boolean isOptionalField(Field field, ElementInfo elementInfo) {
        if(elementInfo.hasPreparedInfo()) {
            if(elementInfo.hasPreparedASN1ElementInfo())
                return elementInfo.getPreparedASN1ElementInfo().isOptional() || elementInfo.getPreparedASN1ElementInfo().hasDefaultValue() ;
            return false;
        }
        else
        if( field.isAnnotationPresent(ASN1Element.class) ) {
            ASN1Element info = field.getAnnotation(ASN1Element.class);
            if(info.isOptional() || info.hasDefaultValue())
                return true;
        }        
        return false;
    }
    
    public static boolean isOptional(ElementInfo elementInfo) {
        boolean result = false;
        if(elementInfo.hasPreparedInfo()) {
            result = elementInfo.getPreparedASN1ElementInfo().isOptional() || elementInfo.getPreparedASN1ElementInfo().hasDefaultValue() ;
        }
        else
            result= elementInfo.getASN1ElementInfo()!=null && elementInfo.getASN1ElementInfo().isOptional();
        return result;
    }
    
    
    public static void checkForOptionalField(Field field, ElementInfo elementInfo) throws Exception {
        if( isOptionalField(field, elementInfo) )
                return;
        throw new  IllegalArgumentException ("The mandatory field '" + field.getName() + "' does not have a value!");
    }
        
        
    public static boolean isSequenceSet(ElementInfo elementInfo) {
        boolean isEqual = false;
        if(elementInfo.hasPreparedInfo()) {
            isEqual = ((ASN1SequenceMetadata)elementInfo.getPreparedInfo().getTypeMetadata()).isSet();
        }
        else {
            ASN1Sequence seq = elementInfo.getAnnotatedClass().getAnnotation(ASN1Sequence.class);
            isEqual = seq.isSet();
        }        
        return isEqual;
    }

    public static boolean isSequenceSetOf(ElementInfo elementInfo) {
        boolean isEqual = false;
        if(elementInfo.hasPreparedInfo()) {
            isEqual = ((ASN1SequenceOfMetadata)elementInfo.getPreparedInfo().getTypeMetadata()).isSetOf();
        }
        else {
            ASN1SequenceOf seq = elementInfo.getAnnotatedClass().getAnnotation(ASN1SequenceOf.class);
            isEqual = seq.isSetOf();
        }        
        return isEqual;
    }
    
    public static Method findMethodForField(String methodName, Class<?> objectClass, Class<?> paramClass ) throws NoSuchMethodException {
        try {
            return objectClass.getMethod(methodName, new Class[] {paramClass});
        }
        catch(NoSuchMethodException ex) {
            Method[] methods = objectClass.getMethods();
            for(Method method : methods) {
                if(method.getName().equalsIgnoreCase(methodName)) {
                    return method;
                }
            }
            throw ex;
        }
    }      
    
    public static Method findSetterMethodForField(Field field, Class<?> objectClass, Class<?> paramClass) throws NoSuchMethodException {
        String methodName = "set"+field.getName().toUpperCase().substring(0,1)+field.getName().substring(1);
        return findMethodForField(methodName, objectClass, paramClass);
    }
    
    public static Method findDoSelectMethodForField(Field field, Class<?> objectClass, Class<?> paramClass) throws NoSuchMethodException {
        String methodName = "select"+field.getName().toUpperCase().substring(0,1)+field.getName().substring(1);
        return findMethodForField(methodName, objectClass, paramClass);
    }
    
    
    public static Method findGetterMethodForField(Field field, Class<?> objectClass) throws NoSuchMethodException {
        String getterMethodName = "get"+field.getName().toUpperCase().substring(0,1)+field.getName().substring(1);
        return objectClass.getMethod(getterMethodName,(java.lang.Class[])null);
    }

    public static Method findIsSelectedMethodForField(Field field, Class<?> objectClass) throws NoSuchMethodException {
        String methodName = "is"+field.getName().toUpperCase().substring(0,1)+field.getName().substring(1)+"Selected";
        return objectClass.getMethod(methodName,(java.lang.Class[])null);
    }    
    
    public static boolean isMemberClass(Class<?> objectClass, ElementInfo elementInfo) {
        //return objectClass.isMemberClass();
        if(elementInfo.hasPreparedInfo()) {
            return elementInfo.getPreparedInfo().isMemberClass();
        }
        else
            return objectClass.isMemberClass();
    }

    public static byte[] ASN1StringToBuffer(Object obj, ElementInfo elementInfo) throws UnsupportedEncodingException {
        byte[] strBuf = null;
        int stringTag = getStringTagForElement(elementInfo); 
        if(stringTag == UniversalTag.UTF8String) {
            strBuf = obj.toString().getBytes("utf-8");
        } else if (stringTag == UniversalTag.BMPString) {
            strBuf = obj.toString().getBytes("UnicodeBigUnmarked");
        }
        else {
            strBuf = obj.toString().getBytes();
        }
	return strBuf;
    }
    
    public static String bufferToASN1String(byte[] byteBuf, ElementInfo elementInfo) throws UnsupportedEncodingException {
        String result = null;
        int stringTag = getStringTagForElement(elementInfo); 
        if(stringTag == UniversalTag.UTF8String) {        
            result = new String(byteBuf, "utf-8");
        }
        else if (stringTag == UniversalTag.BMPString) {
            result = new String(byteBuf, "UnicodeBigUnmarked");
        }
        else {
            result = new String(byteBuf);
        }
        return result;
    }

    public static Class<?> getCollectionType(ElementInfo elementInfo) {
        ParameterizedType tp = (ParameterizedType)elementInfo.getGenericInfo();
	return getCollectionType(tp);
    }

    public static Class<?> getCollectionType(ParameterizedType tp) {

	Type tpParam = tp.getActualTypeArguments()[0];
	Class<?> paramType = null;
	if(tpParam instanceof GenericArrayType) {
		paramType = (Class<?>)((GenericArrayType)tpParam).getGenericComponentType();
		if(paramType.equals(byte.class)) {
			paramType = byte[].class;
		}
	}
	else
            paramType = (Class<?>)tp.getActualTypeArguments()[0];
	return paramType;

    }

}
