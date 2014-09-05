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
package org.bn.coders.ber;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import java.lang.reflect.*;
import java.util.Collection;
import java.util.LinkedList;
import org.bn.coders.*;
import org.bn.metadata.ASN1SequenceOfMetadata;
import org.bn.types.*;

public class BERDecoder extends Decoder {
    
    protected DecodedObject<Integer> decodeLength(InputStream stream) throws Exception {
        int result = 0 ;        
        int bt = stream.read() ;
        if(bt == -1)
            throw new IllegalArgumentException("Unexpected EOF when decoding!");
        
        int len =1 ;
        if (bt < 128 ) {
            result = bt ;
        }
        else {
            //for (int i = 256 - bt ; i > 0 ; i--) {
            // Decode length bug fix. Thanks to John 
            for (int i = bt - 128; i > 0 ; i--) {
                int fBt = stream.read() ;
                if(fBt == -1)
                    throw new IllegalArgumentException("Unexpected EOF when decoding!");
                result = result << 8 ;
                result = result | fBt ;
                len ++;
            }
        }
        return new DecodedObject<Integer>(result,len);
    }

    public DecodedObject decodeTag(InputStream stream) throws Exception {
        int result = 0 ;
        int bt = stream.read();
        if(bt == - 1)
            return null;
        result = bt ;
        int len = 1;
        int tagValue = bt & 31;
        if (tagValue == UniversalTag.LastUniversal) 
        {
                bt = 0x80;
                while ((bt&0x80) != 0 && len < 4) {
                    result <<= 8;
                    bt = stream.read();
                     if (bt != -1)
                     {
                         result |= bt;
                         len++;
                     }
                     else {
                        result >>= 8;
                        break;
                     }                     
                }
        }
        
        return new DecodedObject(result,len);
    }

    protected boolean checkTagForObject(DecodedObject decodedTag, int tagClass, int elementType, int universalTag, 
                                        ElementInfo elementInfo) {
    	if(decodedTag == null) {
    		return false;
    	}
        int definedTag = BERCoderUtils.getTagValueForElement(elementInfo,tagClass,elementType,universalTag).getValue();
        return definedTag == (Integer)decodedTag.getValue();
    }
    
    public DecodedObject decodeSequence(DecodedObject decodedTag,Class objectClass, 
                                           ElementInfo elementInfo, InputStream stream) throws Exception {
        boolean isSet = false;
        if(!CoderUtils.isSequenceSet(elementInfo)) {
	        if(checkTagForObject(decodedTag, TagClass.Universal, ElementType.Constructed, UniversalTag.Sequence,elementInfo)) {
	        }
	        else
	        	return null;	        
        }
        else {
	        if(checkTagForObject(decodedTag, TagClass.Universal, ElementType.Constructed, UniversalTag.Set,elementInfo)) {
	            isSet = true;
	        }
	        else
	        	return null;
        }

        DecodedObject<Integer> len = decodeLength(stream);
        int saveMaxAvailableLen = elementInfo.getMaxAvailableLen();
        elementInfo.setMaxAvailableLen(len.getValue());
        DecodedObject result = null;
        if(!isSet)
            result =  super.decodeSequence(decodedTag,objectClass,elementInfo,stream);
        else
            result =  decodeSet(decodedTag,objectClass,elementInfo,len.getValue(),stream);
        if(result.getSize()!= len.getValue())
            throw new  IllegalArgumentException ("Sequence '" + objectClass.toString() + "' size is incorrect! Must be: "+len.getValue()+". Received: "+result.getSize());
        result.setSize(result.getSize() + len.getSize());
        elementInfo.setMaxAvailableLen(saveMaxAvailableLen );
        return result;
    }
    
    protected DecodedObject decodeSet(DecodedObject decodedTag,Class objectClass, 
                                      ElementInfo elementInfo, Integer len,InputStream stream) throws Exception {
        Object set = createInstanceForElement(objectClass,elementInfo);
        initDefaultValues(set, elementInfo);
        int maxSeqLen = elementInfo.getMaxAvailableLen();
        int sizeOfSet = 0;
        
        DecodedObject<?> fieldTag = null;
        
        if(maxSeqLen==-1 || maxSeqLen>0) {
        	fieldTag = decodeTag(stream);
        }
        
        if(fieldTag!=null)
            sizeOfSet+=fieldTag.getSize();

        Field[] fields = elementInfo.getFields(objectClass);

        boolean fieldEncoded = false; 
        do {
            
            for(int i=0; i<fields.length; i++) {        
                Field field = fields[i];
                DecodedObject obj = decodeSequenceField(fieldTag,set,i, field,stream,elementInfo, false);
                if(obj!=null) {
                    fieldEncoded = true;
                    sizeOfSet +=obj.getSize();                
                    boolean isAny = false;
                    if(i+1==fields.length-1) {
                        ElementInfo info = new ElementInfo();
                        info.setAnnotatedClass(fields[i+1]);        
                        info.setMaxAvailableLen(elementInfo.getMaxAvailableLen());
                        info.setGenericInfo(field.getGenericType());
                        if(elementInfo.hasPreparedInfo()) {
                            info.setPreparedInfo(elementInfo.getPreparedInfo().getFieldMetadata(i+1));
                        }
                        else
                            info.setASN1ElementInfoForClass(fields[i+1]);                
                        isAny = CoderUtils.isAnyField(fields[i+1], info);
                    }

                    if(maxSeqLen!=-1) {
                        elementInfo.setMaxAvailableLen(maxSeqLen - sizeOfSet);              
                    }                
                    
                    if(!isAny) {
                    	if(i<fields.length-1) {
	                        if(maxSeqLen==-1 || elementInfo.getMaxAvailableLen()>0) {                        	                    		
		                        fieldTag = decodeTag(stream);
		                        if(fieldTag!=null)
		                            sizeOfSet += fieldTag.getSize();
		                        else {
		                            break;
		                        }
	                        }
	                        else
	                        	fieldTag = null;
                    	}
                    	else
                    		break;
                    }
                }
            }    
        }
        while(sizeOfSet < len && fieldEncoded);

        return new DecodedObject(set,sizeOfSet);
    }    
    
    
    public DecodedObject decodeEnumItem(DecodedObject decodedTag, Class objectClass, Class enumClass, 
                                           ElementInfo elementInfo, 
                                    InputStream stream) throws Exception {
        if(!checkTagForObject(decodedTag, TagClass.Universal, ElementType.Primitive, UniversalTag.Enumerated, elementInfo))
            return null;
        return decodeIntegerValue(stream);
    }

    public DecodedObject decodeBoolean(DecodedObject decodedTag, Class objectClass, 
                                          ElementInfo elementInfo, 
                                   InputStream stream) throws Exception {
        if(!checkTagForObject(decodedTag, TagClass.Universal, ElementType.Primitive, UniversalTag.Boolean, elementInfo))
            return null;
        DecodedObject<Integer> intVal = decodeIntegerValue(stream);
        DecodedObject result = new DecodedObject(false , intVal.getSize());
        if(intVal.getValue()!=0)
            result.setValue(true);
        return result;
    }

    public DecodedObject decodeAny(DecodedObject decodedTag, Class objectClass, 
                                      ElementInfo elementInfo, 
                               InputStream stream) throws Exception {
        int bufSize = elementInfo.getMaxAvailableLen();
        if(bufSize==0)
            return null;
        ByteArrayOutputStream anyStream = new ByteArrayOutputStream(1024);
        if(bufSize<0)
            bufSize = 1024;
        int len = 0;
        if(bufSize>0) {
            byte[] buffer = new byte[bufSize];
            
            int readed = stream.read(buffer);
            while( readed > 0) {
                anyStream.write(buffer,0,readed);
                len+=readed;
                if(elementInfo.getMaxAvailableLen()>0)
                    break;
                readed = stream.read(buffer);            
            }
        }
        CoderUtils.checkConstraints(len,elementInfo);
        return new DecodedObject(anyStream.toByteArray(),len);
    }

    public DecodedObject decodeNull(DecodedObject decodedTag, Class objectClass, 
                                       ElementInfo elementInfo, 
                                InputStream stream) throws Exception {
        if(!checkTagForObject(decodedTag, TagClass.Universal, ElementType.Primitive, UniversalTag.Null, elementInfo))
            return null;
        stream.read ( ); // ignore null length
        DecodedObject result = new DecodedObject(objectClass.newInstance(),1);
        return result;
    }

    public DecodedObject decodeInteger(DecodedObject decodedTag, Class objectClass, 
                                          ElementInfo elementInfo, 
                                   InputStream stream) throws Exception {
        if(!checkTagForObject(decodedTag, TagClass.Universal, ElementType.Primitive, UniversalTag.Integer, elementInfo))
            return null;
        if(objectClass.equals(Integer.class)) {
            DecodedObject<Integer> result =  decodeIntegerValue(stream);
            CoderUtils.checkConstraints(result.getValue(),elementInfo);
            return result;
        }
        else {
            DecodedObject<Long> result =  decodeLongValue(stream);
            CoderUtils.checkConstraints(result.getValue(),elementInfo);
            return result;
        }        

    }

    public DecodedObject decodeReal(DecodedObject decodedTag, Class objectClass, 
                                       ElementInfo elementInfo, 
                                   InputStream stream) throws Exception {
        if(!checkTagForObject(decodedTag, TagClass.Universal, ElementType.Primitive, UniversalTag.Real, elementInfo))
            return null;
        DecodedObject<Integer> len = decodeLength(stream);
        int realPreamble = stream.read();
        
        Double result = 0.0D;
        int szResult = len.getValue();
        if( (realPreamble & 0x40) == 1) {
            // 01000000 Value is PLUS-INFINITY
            result = Double.POSITIVE_INFINITY;
        }
        if( (realPreamble & 0x41) == 1) {
            // 01000001 Value is MINUS-INFINITY
            result = Double.NEGATIVE_INFINITY;
            szResult+=1;
        }
        else 
        if(len.getValue()>0){
            int szOfExp = 1 + (realPreamble & 0x3);
            int sign = realPreamble & 0x40;
            int ff = (realPreamble & 0x0C) >> 2;
            DecodedObject<Long> exponentEncFrm = decodeLongValue(stream, new DecodedObject<Integer>(szOfExp));
            long exponent = exponentEncFrm.getValue();
            DecodedObject<Long> mantissaEncFrm = decodeLongValue(stream, new DecodedObject<Integer>(szResult - szOfExp - 1));
            // Unpack mantissa & decrement exponent for base 2
            long mantissa = mantissaEncFrm.getValue() << ff;
            while((mantissa & 0x000ff00000000000L) == 0x0) {
                exponent-=8;
                mantissa <<= 8;
            }
            while((mantissa & 0x0010000000000000L) == 0x0) {
                exponent-=1;
                mantissa <<= 1;
            }            
            mantissa &= 0x0FFFFFFFFFFFFFL;
            long lValue = (exponent+1023+52) << 52;
            lValue|= mantissa;
            if(sign == 1) {
                lValue|=0x8000000000000000L;
            }
            result = Double.longBitsToDouble(lValue);
        }
        return new DecodedObject(result,len.getValue()+len.getSize());
    }
    
    public DecodedObject decodeChoice(DecodedObject decodedTag, Class objectClass, 
                                         ElementInfo elementInfo, 
                                   InputStream stream) throws Exception {   
        
        if((elementInfo.hasPreparedInfo() && elementInfo.hasPreparedASN1ElementInfo() && elementInfo.getPreparedASN1ElementInfo().hasTag())
           || (elementInfo.getASN1ElementInfo()!=null && elementInfo.getASN1ElementInfo().hasTag() )) {
            if(!checkTagForObject(decodedTag, TagClass.ContextSpecific, ElementType.Constructed, UniversalTag.LastUniversal, elementInfo))
                return null;
            DecodedObject<Integer> lenOfChild = decodeLength(stream);
            DecodedObject childDecodedTag = decodeTag(stream);
            DecodedObject<Object> result =  super.decodeChoice(childDecodedTag, objectClass, elementInfo, stream);
            result.setSize(result.getSize()+ childDecodedTag.getSize()+lenOfChild.getSize());
            return result;
        }
        else
            return super.decodeChoice(decodedTag, objectClass, elementInfo, stream);
    }

    protected DecodedObject<Integer> decodeIntegerValue(InputStream stream) throws Exception {
        DecodedObject<Long> lVal = decodeLongValue(stream);
        DecodedObject<Integer> result = new DecodedObject<Integer>( (int)((long)lVal.getValue()), lVal.getSize() );
        return result;    
    }
    
    protected DecodedObject<Long> decodeLongValue(InputStream stream) throws Exception {
        DecodedObject<Integer> len =  decodeLength(stream);
        return decodeLongValue(stream,len);    
    }
    
    public DecodedObject<Long> decodeLongValue(InputStream stream, 
                                               DecodedObject<Integer> len) throws Exception {
        DecodedObject<Long> result = new DecodedObject<Long>();
        long value =0;
        for(int i=0;i<len.getValue();i++) {
            int bt = stream.read();
            if (bt == -1 ) {
                throw new IllegalArgumentException("Unexpected EOF when decoding!");
            }
            
            if( i == 0 && (bt & (byte)0x80)!=0) {
                bt = bt - 256;
            }
            
            value = (value << 8) | bt ;
        }
        result.setValue(value);
        result.setSize(len.getValue() +  len.getSize());
        return result;    
    }
    
    public DecodedObject decodeOctetString(DecodedObject decodedTag, Class objectClass, 
                                              ElementInfo elementInfo, 
                                       InputStream stream) throws Exception {
        if(!checkTagForObject(decodedTag, TagClass.Universal, ElementType.Primitive, UniversalTag.OctetString, elementInfo))
            return null;
        DecodedObject<Integer> len = decodeLength(stream);
        CoderUtils.checkConstraints(len.getValue(),elementInfo);
        byte[] byteBuf = new byte[ len.getValue()];
        stream.read(byteBuf);
        return new DecodedObject(byteBuf, len.getValue() + len.getSize());
    }

    public DecodedObject decodeBitString(DecodedObject decodedTag, Class objectClass, 
                                            ElementInfo elementInfo, 
                                       InputStream stream) throws Exception {
        if(!checkTagForObject(decodedTag, TagClass.Universal, ElementType.Primitive, UniversalTag.Bitstring, elementInfo))
            return null;
        DecodedObject<Integer> len = decodeLength(stream);
        int trailBitCnt = stream.read();
        CoderUtils.checkConstraints(len.getValue()*8-trailBitCnt,elementInfo);
        byte[] byteBuf = new byte[ len.getValue()-1];        
        
        stream.read(byteBuf);                
        return new DecodedObject( new BitString( byteBuf, trailBitCnt) , len.getValue() + len.getSize());
    }

    public DecodedObject decodeString(DecodedObject decodedTag, Class objectClass, 
                                         ElementInfo elementInfo, 
                                  InputStream stream) throws Exception {
        if(!checkTagForObject(decodedTag, TagClass.Universal, ElementType.Primitive, CoderUtils.getStringTagForElement(elementInfo), elementInfo))
            return null;
        DecodedObject<Integer> len = decodeLength(stream);
        CoderUtils.checkConstraints(len.getValue(),elementInfo);
        byte[] byteBuf = new byte[len.getValue()];
        stream.read(byteBuf);
        String result = CoderUtils.bufferToASN1String (byteBuf, elementInfo);
        return new DecodedObject(result, len.getValue() + len.getSize());
    }

    public DecodedObject decodeSequenceOf(DecodedObject decodedTag, Class objectClass, 
                                             ElementInfo elementInfo, 
                                      InputStream stream) throws Exception {
    	if(!CoderUtils.isSequenceSetOf( elementInfo )) { 
	        if(!checkTagForObject(decodedTag, TagClass.Universal, ElementType.Constructed, UniversalTag.Sequence, elementInfo))
	            return null;
    	}
    	else {
	        if(!checkTagForObject(decodedTag, TagClass.Universal, ElementType.Constructed, UniversalTag.Set, elementInfo))
	            return null;    		
    	}
        Collection result = new LinkedList();
        DecodedObject<Integer> len = decodeLength(stream);
        if(len.getValue()!=0) {
            int lenOfItems = 0;
            int cntOfItems = 0;
	    Class paramType = CoderUtils.getCollectionType(elementInfo);

            do {
                ElementInfo info = new ElementInfo();
                info.setAnnotatedClass(paramType);
                info.setParentAnnotated(elementInfo.getAnnotatedClass());
                if(elementInfo.hasPreparedInfo()) {
                    ASN1SequenceOfMetadata seqOfMeta = (ASN1SequenceOfMetadata)elementInfo.getPreparedInfo().getTypeMetadata();
                    info.setPreparedInfo( seqOfMeta.getItemClassMetadata() );
                }
            
                DecodedObject itemTag = decodeTag(stream);
                DecodedObject item=decodeClassType(itemTag,paramType,info,stream);
                if(item!=null) {
                    lenOfItems+=item.getSize()+itemTag.getSize();
                    result.add(item.getValue());
                    cntOfItems++;
                }
            }
            while(lenOfItems < len.getValue());
            CoderUtils.checkConstraints ( cntOfItems ,elementInfo );
        }
        return new DecodedObject(result, len.getValue() + len.getSize());
    }    
    
    
    public DecodedObject decodeObjectIdentifier(DecodedObject decodedTag, 
                                                Class objectClass, 
                                                ElementInfo elementInfo, 
                                                InputStream stream) throws Exception {
        if (!checkTagForObject(decodedTag, TagClass.Universal, ElementType.Primitive, UniversalTag.ObjectIdentifier, elementInfo))
            return null;
        DecodedObject<Integer> len = decodeLength(stream);
        byte[] byteBuf = new byte[len.getValue()];
        stream.read(byteBuf, 0, byteBuf.length);
        String dottedDecimal = BERObjectIdentifier.Decode(byteBuf);
        return new DecodedObject<Object>(new ObjectIdentifier(dottedDecimal));
    }
}
