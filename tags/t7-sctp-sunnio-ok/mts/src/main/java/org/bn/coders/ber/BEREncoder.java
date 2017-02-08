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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;

import org.bn.annotations.*;
import org.bn.coders.CoderUtils;
import org.bn.coders.DecodedObject;
import org.bn.coders.ElementInfo;
import org.bn.coders.ElementType;
import org.bn.coders.Encoder;
import org.bn.coders.TagClass;
import org.bn.coders.UniversalTag;
import org.bn.metadata.ASN1SequenceOfMetadata;
import org.bn.types.BitString;
import org.bn.types.ObjectIdentifier;
import org.bn.utils.ReverseByteArrayOutputStream;

import com.devoteam.srit.xmlloader.asn1.dictionary.ASNDictionary;
import com.devoteam.srit.xmlloader.asn1.dictionary.Embedded;

public class BEREncoder<T> extends Encoder<T> {
    
    public BEREncoder() {
    }
    
    public void encode(T object, OutputStream stream) throws Exception {
        ReverseByteArrayOutputStream reverseStream = new ReverseByteArrayOutputStream();        
        super.encode(object, reverseStream);
        reverseStream.writeTo(stream);        
    }
    
    public int encodeSequence(Object object, OutputStream stream, 
                                 ElementInfo elementInfo) throws Exception {
        int resultSize = 0;
        Field[] fields = elementInfo.getFields(object.getClass());
        
        for ( int i = 0;i<fields.length; i++) {
             Field field  = fields [ fields.length - 1 - i];
             resultSize+= encodeSequenceField(object, fields.length - 1 - i, field,stream,elementInfo);
        }
        if(!CoderUtils.isSequenceSet(elementInfo))
            resultSize += encodeHeader (BERCoderUtils.getTagValueForElement (elementInfo,TagClass.Universal, ElementType.Constructed, UniversalTag.Sequence), resultSize, stream );
        else
            resultSize += encodeHeader (BERCoderUtils.getTagValueForElement (elementInfo,TagClass.Universal, ElementType.Constructed, UniversalTag.Set), resultSize, stream );
        return resultSize;
    }

    public int encodeChoice(Object object, OutputStream stream, 
                               ElementInfo elementInfo)  throws Exception {
        int resultSize = 0;
        int sizeOfChoiceField =  super.encodeChoice ( object, stream , elementInfo );
        
        if((elementInfo.hasPreparedInfo() && elementInfo.hasPreparedASN1ElementInfo() && elementInfo.getPreparedASN1ElementInfo().hasTag())
           || (elementInfo.getASN1ElementInfo()!=null && elementInfo.getASN1ElementInfo().hasTag() )) {
                resultSize += encodeHeader (BERCoderUtils.getTagValueForElement (elementInfo,TagClass.ContextSpecific, ElementType.Constructed, UniversalTag.LastUniversal), sizeOfChoiceField, stream );
        }        
        resultSize+= sizeOfChoiceField;
        return resultSize;
    }    
        
    public int encodeEnumItem(Object enumConstant, Class enumClass, OutputStream stream, 
                                 ElementInfo elementInfo) throws Exception {
        int resultSize = 0;
        ASN1EnumItem enumObj = elementInfo.getAnnotatedClass().getAnnotation(ASN1EnumItem.class);
        int szOfInt = encodeIntegerValue(enumObj.tag(),stream);
        resultSize += szOfInt;
        resultSize += encodeLength(szOfInt, stream);
        resultSize += encodeTag(BERCoderUtils.getTagValueForElement(elementInfo,TagClass.Universal, ElementType.Primitive, UniversalTag.Enumerated),
            stream
        );
        return resultSize;
    }

    public int encodeBoolean(Object object, OutputStream stream, 
                                ElementInfo elementInfo) throws Exception {
        int resultSize = 1;
        stream.write( (Boolean)object ? 0xFF:0x00 );

        resultSize += encodeLength(1, stream);
        resultSize += encodeTag(BERCoderUtils.getTagValueForElement(elementInfo,TagClass.Universal, ElementType.Primitive, UniversalTag.Boolean),
            stream
        );
        return resultSize;
    }

    public int encodeAny(Object object, OutputStream stream, 
                            ElementInfo elementInfo) throws Exception {
        int resultSize = 0, sizeOfString = 0;
        byte[] buffer = (byte[])object;
        stream.write( buffer );
        sizeOfString = buffer.length;
        CoderUtils.checkConstraints(sizeOfString,elementInfo);
        resultSize += sizeOfString;
        return resultSize;
    }
    
    public int encodeIntegerValue(long value, OutputStream stream) throws Exception {
        int resultSize = CoderUtils.getIntegerLength(value);
        for (int i = 0 ; i < resultSize ; i++) {
            stream.write((byte)value);
            value =  value >> 8 ;
        }
        return resultSize;
    }

    public int encodeInteger(Object object, OutputStream stream, 
                                ElementInfo elementInfo) throws Exception {
        int resultSize = 0;
        int szOfInt = 0;
        if(object instanceof Integer) {
            Integer value = (Integer) object;
            CoderUtils.checkConstraints(value,elementInfo);
            szOfInt = encodeIntegerValue(value,stream);
        }
        else 
        if(object instanceof Long) {
            Long value = (Long) object;
            CoderUtils.checkConstraints(value,elementInfo);
            szOfInt = encodeIntegerValue(value,stream);            
        }
        resultSize += szOfInt;        
        resultSize += encodeLength(szOfInt, stream);
        resultSize += encodeTag(BERCoderUtils.getTagValueForElement(elementInfo,TagClass.Universal, ElementType.Primitive, UniversalTag.Integer),
            stream
        );
        return resultSize;
    }
    
    public int encodeReal(Object object, OutputStream stream, 
                             ElementInfo elementInfo) throws Exception {
        int resultSize = 0;
        Double value = (Double) object;
        //CoderUtils.checkConstraints(value,elementInfo);
        int szOfInt = 0;
        long asLong = Double.doubleToLongBits(value);
        if(asLong == 0x7ff0000000000000L) { // positive infinity
            stream.write(0x40); // 01000000 Value is PLUS-INFINITY
        }
        else
        if(asLong == 0xfff0000000000000L) { // negative infinity            
            stream.write(0x41); // 01000001 Value is MINUS-INFINITY
        }        
        else 
        if(asLong!=0x0) {
            long exponent = ((0x7ff0000000000000L & asLong) >> 52) - 1023 - 52;
            long mantissa = 0x000fffffffffffffL & asLong;
            mantissa |= 0x10000000000000L; // set virtual delimeter
            
            // pack mantissa for base 2
            while((mantissa & 0xFFL) == 0x0) {
                mantissa >>= 8;
                exponent += 8; //increment exponent to 8 (base 2)
            }        
            while((mantissa & 0x01L) == 0x0) {
                mantissa >>= 1;
                exponent+=1; //increment exponent to 1
            }
             
             szOfInt+= encodeIntegerValue(mantissa,stream);
             int szOfExp = CoderUtils.getIntegerLength(exponent);
             szOfInt+= encodeIntegerValue(exponent,stream);
             
             int realPreamble = 0x80;
             realPreamble |= (byte)(szOfExp - 1);
             if( (asLong & 0x8000000000000000L) == 1) {
                 realPreamble|= 0x40; // Sign
             }
             stream.write(realPreamble );
             szOfInt+=1;
        }
        resultSize += szOfInt;
        resultSize += encodeLength(szOfInt, stream);
        resultSize += encodeTag(BERCoderUtils.getTagValueForElement(elementInfo,TagClass.Universal, ElementType.Primitive, UniversalTag.Real),
            stream
        );
        return resultSize;
    }

    public int encodeOctetString(Object object, OutputStream stream, 
                                    ElementInfo elementInfo) throws Exception {
        int resultSize = 0, sizeOfString = 0;
        // FHmodif to debug
        if (!(object instanceof byte[]))
        {
        	System.out.println("invalid cast for byte[] : " + elementInfo);
        }
        byte[] buffer = (byte[])object;
        stream.write( buffer );
        sizeOfString = buffer.length;

        resultSize += sizeOfString;
        CoderUtils.checkConstraints(sizeOfString,elementInfo);
        resultSize += encodeLength(sizeOfString, stream);
        // FHModif : set the ElementType to ElementType.Constructed for embedded objects
        String type = null;
        if ((elementInfo != null) && (elementInfo.getPreparedInstance() != null) &&
        	(elementInfo.getPreparedInstance().getClass() != null))
        {
        	type = elementInfo.getPreparedInstance().getClass().getCanonicalName();
        }
        int eltType = ElementType.Primitive;
        // FHModif begin 
    	// manage the embedded objects
    	Embedded embedded = ASNDictionary.getInstance().getEmbeddedByInitial(type);
		if (embedded != null) 
		{            
        	eltType = ElementType.Constructed;
        }
        // FHModif : end
        // FHModif : remove line 
        //resultSize += encodeTag(BERCoderUtils.getTagValueForElement(elementInfo,TagClass.Universal, ElementType.Primitive, UniversalTag.OctetString), stream);
        // FHModif : new line 
        resultSize += encodeTag(BERCoderUtils.getTagValueForElement(elementInfo,TagClass.Universal, eltType, UniversalTag.OctetString), stream);
        return resultSize;
    }

    public int encodeBitString(Object object, OutputStream stream, 
                                  ElementInfo elementInfo) throws Exception {
        int resultSize = 0, sizeOfString = 0;
        BitString str = (BitString)object;
        CoderUtils.checkConstraints(str.getLengthInBits(), elementInfo);
        
        byte[] buffer = str.getValue();
        stream.write( buffer );
        stream.write ( str.getTrailBitsCnt() );
        sizeOfString = buffer.length + 1;

        resultSize += sizeOfString;
        resultSize += encodeLength(sizeOfString, stream);
        resultSize += encodeTag(BERCoderUtils.getTagValueForElement(elementInfo,TagClass.Universal, ElementType.Primitive, UniversalTag.Bitstring),
            stream
        );
        return resultSize;
    }

    public int encodeString(Object object, OutputStream stream, 
                               ElementInfo elementInfo) throws Exception {
        int resultSize = 0, sizeOfString = 0;
        byte[] strBuf = CoderUtils.ASN1StringToBuffer(object, elementInfo);
        stream.write( strBuf );
        sizeOfString = strBuf.length;

        resultSize += sizeOfString;
        CoderUtils.checkConstraints(sizeOfString,elementInfo);
        resultSize += encodeLength(sizeOfString, stream);
        resultSize += encodeTag(BERCoderUtils.getTagValueForElement(elementInfo,TagClass.Universal, ElementType.Primitive, CoderUtils.getStringTagForElement(elementInfo)),
            stream
        );
        return resultSize;
    }

    public int encodeSequenceOf(Object object, OutputStream stream, 
                                   ElementInfo elementInfo) throws Exception {    
        int resultSize = 0;
        Object[] collection = ((java.util.Collection<Object>)object).toArray();
        int sizeOfCollection = 0;
        for(int i=0;i<collection.length;i++) {
            Object obj = collection[collection.length - 1 - i];
            ElementInfo info = new ElementInfo();
            info.setAnnotatedClass(obj.getClass());
            info.setParentAnnotated(elementInfo.getAnnotatedClass());
            if(elementInfo.hasPreparedInfo()) {
                ASN1SequenceOfMetadata seqOfMeta = (ASN1SequenceOfMetadata)elementInfo.getPreparedInfo().getTypeMetadata();
                info.setPreparedInfo( seqOfMeta.getItemClassMetadata() );
            }
            sizeOfCollection+=encodeClassType(obj,stream,info);
        }
        resultSize += sizeOfCollection;
        CoderUtils.checkConstraints(collection.length,elementInfo);
        resultSize += encodeLength(sizeOfCollection, stream);
        
        if(!CoderUtils.isSequenceSetOf(elementInfo)) {
            resultSize += encodeTag(BERCoderUtils.getTagValueForElement(elementInfo,TagClass.Universal, ElementType.Constructed, UniversalTag.Sequence),
                stream
            );        
        }
        else {
            resultSize += encodeTag(BERCoderUtils.getTagValueForElement(elementInfo,TagClass.Universal, ElementType.Constructed, UniversalTag.Set),
                stream
            );            
        }
        return resultSize;
    }

    protected int encodeHeader(DecodedObject<Integer> tagValue, int contentLen, OutputStream stream) throws Exception {
        int resultSize = encodeLength(contentLen, stream);
        resultSize += encodeTag(tagValue, stream);
        return resultSize;
    }
    
    protected int encodeTag(DecodedObject<Integer> tagValue, OutputStream stream) throws Exception {
        int resultSize = tagValue.getSize();
        int value = tagValue.getValue();
        for (int i = 0 ; i < tagValue.getSize() ; i++) {
            stream.write((byte)value);
            value =  value >> 8 ;            
        }
        return resultSize;
    }

    protected int encodeLength(int length, OutputStream stream) throws IOException {
        int resultSize = 0;
        
        if (length < 0) {
            throw new IllegalArgumentException() ;
        }
        else 
        if (length < 128) {
            stream.write(length);
            resultSize++;
        }
        else 
        if (length < 256) {
            stream.write(length);
            stream.write((byte)0x81);
            resultSize+=2;
        }
        else 
        if (length < 65536) {
            stream.write((byte)(length)) ;
            stream.write((byte)(length >> 8));
            stream.write((byte)0x82);
            resultSize+=3;
        }
        else 
        if (length < 16777126) {
            stream.write((byte)(length));
            stream.write((byte)(length >> 8)) ;
            stream.write((byte)(length >> 16)) ;
            stream.write((byte)0x83);
            resultSize+=4;
        }
        else {
            stream.write((byte)(length));
            stream.write((byte)(length >> 8));
            stream.write((byte)(length >> 16));
            stream.write((byte)(length >> 24));
            stream.write((byte)0x84);
            resultSize+=5;
        } 
        return resultSize;
    }
    
    public int encodeNull(Object object, OutputStream stream, 
                             ElementInfo elementInfo) throws Exception  {
        stream.write( 0 );
        int resultSize = 1;
        resultSize += encodeTag(BERCoderUtils.getTagValueForElement(elementInfo,TagClass.Universal, ElementType.Primitive, UniversalTag.Null),
            stream
        );
        return resultSize;
    }

    public int encodeObjectIdentifier(Object object, OutputStream stream, 
                                      ElementInfo elementInfo) throws Exception {
    	 ObjectIdentifier oid = (ObjectIdentifier)object;
         int[] ia = oid.getIntArray();
         byte[] buffer = BERObjectIdentifier.Encode(ia);
         stream.write(buffer, 0, buffer.length);
         int resultSize = buffer.length;
         resultSize += encodeLength(resultSize, stream);
         resultSize += encodeTag(
             BERCoderUtils.getTagValueForElement(elementInfo, TagClass.Universal, ElementType.Primitive, UniversalTag.ObjectIdentifier),
             stream
         );
         return resultSize;
    }
}
