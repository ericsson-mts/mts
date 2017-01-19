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

import java.io.OutputStream;

import java.lang.reflect.*;
import org.bn.IEncoder;
import org.bn.annotations.*;
import org.bn.metadata.ASN1ElementMetadata;
import org.bn.metadata.ASN1Metadata;
import org.bn.types.*;

public abstract class Encoder<T> implements IEncoder<T>, IASN1TypesEncoder {
    
    public void encode(T object, OutputStream stream) throws Exception {
        ElementInfo elemInfo = new ElementInfo();
        elemInfo.setAnnotatedClass(object.getClass());
        //elemInfo.setASN1ElementInfo(object.getClass().getAnnotation(ASN1Element.class));
        int sizeOfEncodedBytes = 0;
        if(object instanceof IASN1PreparedElement) {
            sizeOfEncodedBytes = encodePreparedElement(object, stream, elemInfo);
        }
        else {
            elemInfo.setASN1ElementInfoForClass(object.getClass());
            sizeOfEncodedBytes = encodeClassType(object, stream, elemInfo);
        }
        
        // FHModif remove lines
        //if( sizeOfEncodedBytes == 0) {
        //   throw new IllegalArgumentException("Unable to find any supported annotation for class type: " + object.getClass().toString());
        //};
        
    }

    public int encodeClassType(Object object, OutputStream stream, ElementInfo elementInfo) throws Exception {
        int resultSize = 0;
        if(elementInfo.hasPreparedInfo()) {
            resultSize+=elementInfo.getPreparedInfo().getTypeMetadata().encode(this,object, stream, elementInfo);
        }        
        else
        if( object instanceof IASN1PreparedElement) {
            resultSize+=encodePreparedElement(object, stream, elementInfo);
        }
        else
        if( elementInfo.getAnnotatedClass().isAnnotationPresent(ASN1SequenceOf.class) ) {
            resultSize+=encodeSequenceOf(object, stream, elementInfo);
        }        
        else        
        if( elementInfo.getAnnotatedClass().isAnnotationPresent(ASN1Sequence.class) ) {
            resultSize+=encodeSequence(object, stream, elementInfo);
        }
        else
        if( elementInfo.getAnnotatedClass().isAnnotationPresent(ASN1Choice.class) ) {
            resultSize+=encodeChoice(object, stream, elementInfo);
        }
        else
        if( elementInfo.getAnnotatedClass().isAnnotationPresent(ASN1BoxedType.class) ) {
            resultSize+=encodeBoxedType(object, stream, elementInfo);
        }        
        else
        if( elementInfo.getAnnotatedClass().isAnnotationPresent(ASN1Enum.class) ) {
            resultSize+=encodeEnum(object, stream, elementInfo);
        }
        else
        if( elementInfo.getAnnotatedClass().isAnnotationPresent(ASN1Boolean.class) ) {            
            resultSize+=encodeBoolean(object, stream, elementInfo);
        }
        else
        if( elementInfo.getAnnotatedClass().isAnnotationPresent(ASN1Any.class) ) {
            resultSize+=encodeAny(object, stream, elementInfo);
        }
        else
        if( elementInfo.getAnnotatedClass().isAnnotationPresent(ASN1Integer.class) ) {
            resultSize+=encodeInteger(object, stream, elementInfo);
        }        
        else
        if( elementInfo.getAnnotatedClass().isAnnotationPresent(ASN1Real.class) ) {
            resultSize+=encodeReal(object, stream, elementInfo);
        }        
        else
        if( elementInfo.getAnnotatedClass().isAnnotationPresent(ASN1OctetString.class) ) {
            resultSize+=encodeOctetString(object, stream, elementInfo);
        }
        else
        if( elementInfo.getAnnotatedClass().isAnnotationPresent(ASN1BitString.class) || object.getClass().equals(BitString.class) ) {
            resultSize+=encodeBitString(object, stream, elementInfo);
        }
        else
        if( elementInfo.getAnnotatedClass().isAnnotationPresent(ASN1ObjectIdentifier.class) || object.getClass().equals(ObjectIdentifier.class) ) {
            resultSize+=encodeObjectIdentifier ( object, stream, elementInfo );
        }
        else
        if( elementInfo.getAnnotatedClass().isAnnotationPresent(ASN1String.class) ) {
            resultSize+=encodeString(object, stream, elementInfo);
        }
        else
        if( elementInfo.getAnnotatedClass().isAnnotationPresent(ASN1Null.class) ) {
            resultSize+=encodeNull(object, stream, elementInfo);        
        }
        else
        if( elementInfo.getAnnotatedClass().isAnnotationPresent(ASN1Element.class) ) {
            resultSize+=encodeElement(object, stream, elementInfo);
        }
        else
            resultSize+=encodeJavaElement(object, stream, elementInfo);
        return resultSize;
    }
    
    protected int encodeJavaElement(Object object, OutputStream stream, ElementInfo info ) throws Exception {
        if(object.getClass().equals(String.class)) {
            return encodeString(object,stream,info);
        }
        else
        if(object.getClass().equals(Integer.class)) {
            return encodeInteger(object,stream,info);
        }
        else
        if(object.getClass().equals(Long.class)) {
            return encodeInteger(object,stream,info);
        }
        else
        if(object.getClass().equals(Double.class)) {
            return encodeReal(object,stream,info);
        }
        else
        if(object.getClass().equals(Boolean.class)) {
            return encodeBoolean(object,stream,info);
        }        
        else
        if(object.getClass().isArray()) {
            return encodeOctetString(object,stream,info);
        }
        else
            return 0;
    }

    public int encodePreparedElement(Object object, OutputStream stream, ElementInfo elementInfo) throws Exception {
        IASN1PreparedElement preparedInstance = (IASN1PreparedElement)object;
        elementInfo.setPreparedInstance(preparedInstance);
        ASN1ElementMetadata elementDataSave = null;
        if(elementInfo.hasPreparedASN1ElementInfo()) {
            elementDataSave = elementInfo.getPreparedASN1ElementInfo();
        }        
        elementInfo.setPreparedInfo(preparedInstance.getPreparedData());
        //elementInfo.setPreparedASN1ElementInfo(preparedInstance.getPreparedData().getASN1ElementInfo());
        if(elementDataSave!=null)
            elementInfo.setPreparedASN1ElementInfo(elementDataSave);
        IASN1PreparedElementData preparedInstanceData = preparedInstance.getPreparedData();
        ASN1Metadata metaData = preparedInstanceData.getTypeMetadata();
        return metaData.encode(this, object, stream, elementInfo);        
    }
     
    public Object invokeGetterMethodForField(Field field, Object object, ElementInfo elementInfo) throws Exception {
        if(elementInfo!=null && elementInfo.hasPreparedInfo()) {
            return elementInfo.getPreparedInfo().invokeGetterMethod(object, (java.lang.Object[])null);
        }
        else {    
            Method method = CoderUtils.findGetterMethodForField(field,object.getClass());
            return method.invoke(object, (java.lang.Object[])null);
        }
    }

    public boolean invokeSelectedMethodForField(Field field, Object object, ElementInfo elementInfo) throws Exception {
        if(elementInfo!=null && elementInfo.hasPreparedInfo()) {
            return (Boolean)elementInfo.getPreparedInfo().invokeIsSelectedMethod(object, (java.lang.Object[])null);
        }
        else {
            Method method = CoderUtils.findIsSelectedMethodForField(field,object.getClass());
            return (Boolean)method.invoke(object, (java.lang.Object[])null);
        }
    }
        
    public int encodeSequence(Object object, OutputStream stream, ElementInfo elementInfo) throws Exception {
        int resultSize = 0;

        Field[] fields = elementInfo.getFields(object.getClass());
        int fieldIdx = 0;
        for ( Field field : fields ) {
            resultSize += encodeSequenceField(object,fieldIdx++,field,stream,elementInfo);
        }
        return resultSize;
    }

    protected int encodeSequenceField(Object object, int fieldIdx, Field field, OutputStream stream, ElementInfo elementInfo) throws  Exception {
        int resultSize = 0;
        ElementInfo info = new ElementInfo();
        info.setAnnotatedClass(field);
        if(elementInfo.hasPreparedInfo()) {
            info.setPreparedInfo(elementInfo.getPreparedInfo().getFieldMetadata(fieldIdx));
        }
        else
            info.setASN1ElementInfoForClass(field);
        
        if(field.isSynthetic())
            return resultSize;
        // FHModif
        //if(CoderUtils.isNullField(field, info)) {
        //    return encodeNull(object,stream,elementInfo);
        //} else 
        //{
        Object invokeObjResult = invokeGetterMethodForField(field,object, info);
            
        if(invokeObjResult!=null) {
            resultSize += encodeClassType(invokeObjResult, stream, info);
        }
        else
            CoderUtils.checkForOptionalField(field, info);
        //}
        return resultSize;
    }
    
    protected boolean isSelectedChoiceItem(Field field, Object object, ElementInfo info) throws Exception {
        if(invokeSelectedMethodForField(field,object,info)) {
            return true;
        }
        else
            return false;
    }
    
    protected ElementInfo getChoiceSelectedElement(Object object, ElementInfo elementInfo) throws Exception {
        ElementInfo info = null;                
        
        Field[] fields = elementInfo.getFields(object.getClass());

        int fieldIdx = 0;
        for ( Field field : fields ) {            
            if(!field.isSynthetic()) {
                info = new ElementInfo();
                info.setAnnotatedClass(field);
                if(elementInfo.hasPreparedInfo()) {
                    info.setPreparedInfo(elementInfo.getPreparedInfo().getFieldMetadata(fieldIdx));
                }
                else
                    info.setASN1ElementInfoForClass(field);
            
                if(isSelectedChoiceItem(field,object,info)) {
                    break;
                }
                else {
                    info = null;
                }
            }
            fieldIdx++;
        }
        if(info==null) {
            throw new  IllegalArgumentException ("The choice '" + object.toString() + "' does not have a selected item!");
        }        
        return info;
    }

    public int encodeChoice(Object object, OutputStream stream, ElementInfo elementInfo)  throws Exception {
        int resultSize = 0;
        ElementInfo info = getChoiceSelectedElement(object, elementInfo);
        Object invokeObjResult = invokeGetterMethodForField((Field)info.getAnnotatedClass(),object, info);
        resultSize+=encodeClassType(invokeObjResult, stream, info);
        return resultSize;
    }
    
        
    public int encodeEnum(Object object, OutputStream stream, ElementInfo elementInfo) throws Exception  {
        int resultSize = 0;
        Field field = object.getClass().getDeclaredField("value");
        Object result = invokeGetterMethodForField( field, object, null);
       
        Class enumClass = null;
        for(Class cls : object.getClass().getDeclaredClasses()) {
            if(cls.isEnum()) {
                for(Field enumItem: cls.getDeclaredFields()) {
                    if(enumItem.isAnnotationPresent(ASN1EnumItem.class)) {
                        if(enumItem.getName().equals(result.toString())) {
                            elementInfo.setAnnotatedClass(enumItem);
                            break;
                        }
                    }
                }
                enumClass = cls;
                break;
            }
        }        
        resultSize+=encodeEnumItem(result, enumClass, stream, elementInfo);
        return resultSize;
    }
    
    public int encodeElement(Object object, OutputStream stream, ElementInfo elementInfo) throws Exception  {
        elementInfo.setAnnotatedClass(object.getClass());
        return encodeClassType(object,stream,elementInfo);
    }
    
    public int encodeBoxedType(Object object, OutputStream stream, ElementInfo elementInfo) throws Exception  {
        Field field = object.getClass().getDeclaredField("value");
        elementInfo.setAnnotatedClass(field);
        
        if(elementInfo.getASN1ElementInfo()==null) {
            elementInfo.setASN1ElementInfoForClass(field);
        }
        else {
        	if(!elementInfo.getASN1ElementInfo().hasTag()) {
        		ASN1Element fieldInfo = field.getAnnotation(ASN1Element.class);
        		if(fieldInfo!=null && fieldInfo.hasTag()) {
	        		ASN1ElementMetadata elData = new ASN1ElementMetadata(
	        			elementInfo.getASN1ElementInfo().name(),
	        			elementInfo.getASN1ElementInfo().isOptional(),
	        			fieldInfo.hasTag(),
	        			fieldInfo.isImplicitTag(),
	        			fieldInfo.tagClass(),
	        			fieldInfo.tag(),
	        			elementInfo.getASN1ElementInfo().hasDefaultValue()        				
	        		);
	        		elementInfo.setPreparedASN1ElementInfo(elData);
        		};
        	}
        }
        if(field.isAnnotationPresent(ASN1Null.class)) {
            return encodeNull(object,stream,elementInfo);
        }
        else {
            
            return encodeClassType(invokeGetterMethodForField(field,object,elementInfo), stream, elementInfo);
        }
    }
}
