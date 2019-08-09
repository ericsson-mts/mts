echo off
echo Generate TCAP
#asn1_bn_compiler.bat tcap
# BUG1 la directive UNIVERSAL n'est pas bien prise en compte : fichier ExternalPDU.java modifier 
# TagClass.ContextSpecific => TagClass.Universal
# COMPIL1 la methode initWithDefaults n'est pas bien construite : fichiers AARE_apdu.java, AARQ_apdu.java, AADT_apdu.java public 
#void initWithDefaults() {
#            BitString param_Protocol_version = new BitString();
#            setProtocol_version(param_Protocol_version);    
#        }
#
# 
#asn1_java_compiler.bat tcap
asn1_templater.bat .tcap.
echo ERROR XML=0/15,  BER=0/15,  DER=0/15, 

echo Generate MAP
#asn1_bn_compiler.bat map
#asn1_java_compiler.bat map
asn1_templater.bat .map.
echo ERROR XML=0/225,  BER=1/225,  DER=1/225, 
echo Process class[49] = SendRoutingInfoRes.xml => BER(1) DER(1) KO : FAILED

echo Generate CAP
#asn1_bn_compiler.bat cap
#asn1_java_compiler.bat cap
asn1_templater.bat .cap.
echo ERROR XML=0/348,  BER=2/348,  DER=2/348, 
echo Process class[47] = SendRoutingInfoRes.xml => BER(1) BER(3) BER(5) DER(1) DER(3) DER(5) KO : FAILED
echo Process class[285] = CallGapArg.xml => BER(1) BER(3) BER(5) DER(1) DER(3) DER(5) KO : FAILED

echo Generate S1AP
#asn1_bn_compiler.bat S1AP
#asn1_java_compiler.bat cap
asn1_templater.bat .cap
echo ERROR XML=0/123,  BER=6/123,  DER=6/123, 
echo Process class[6] = TargetID.xml => BER(1) BER(2) BER(3) BER(4) BER(5) DER(1) DER(2) DER(3) DER(4) DER(5) KO : FAILED 10, 0.265 s, 37.73585 /s.
echo Process class[18] = SONConfigurationTransfer.xml => BER(1) BER(3) BER(5) DER(1) DER(3) DER(5) KO : FAILED 10, 0.312 s, 32.05128 /s.
echo Process class[53] = E_RABItem.xml => BER(1) BER(2) BER(3) BER(4) DER(1) DER(2) DER(3) DER(4) KO : FAILED 10, 0.063 s, 158.73016 /s.
echo Process class[94] = WarningAreaList.xml => BER(1) BER(2) BER(4) BER(5) DER(1) DER(2) DER(4) DER(5) KO : FAILED 10, 0.14 s, 71.42857 /s.
echo Process class[103] = E_RABFailedToSetupItemHOReqAck.xml => BER(1) BER(2) BER(3) BER(4) DER(1) DER(2) DER(3) DER(4) KO : FAILED 10, 0.047 s, 212.76596 /s.
echo Process class[123] = BroadcastCompletedAreaList.xml => BER(1) java.lang.IllegalArgumentException: Unexpected EOF when decoding!
echo java.lang.IllegalArgumentException: Unexpected EOF when decoding!
echo        at org.bn.coders.ber.BERDecoder.decodeLength(BERDecoder.java:49)
echo        at org.bn.coders.ber.BERDecoder.decodeSequence(BERDecoder.java:113)
echo        at org.bn.metadata.ASN1SequenceMetadata.decode(ASN1SequenceMetadata.java:59)
echo        at org.bn.coders.Decoder.decodePreparedElement(Decoder.java:162)
echo        at org.bn.coders.Decoder.decodeClassType(Decoder.java:47)
echo        at org.bn.coders.ber.BERDecoder.decodeSequenceOf(BERDecoder.java:449)
echo        at org.bn.metadata.ASN1SequenceOfMetadata.decode(ASN1SequenceOfMetadata.java:69)
echo        at org.bn.metadata.ASN1BoxedTypeMetadata.decode(ASN1BoxedTypeMetadata.java:121)
echo        at org.bn.coders.Decoder.decodePreparedElement(Decoder.java:162)
echo        at org.bn.coders.Decoder.decodeClassType(Decoder.java:47)
echo        at org.bn.coders.Decoder.decodeChoice(Decoder.java:374)
echo        at org.bn.coders.ber.BERDecoder.decodeChoice(BERDecoder.java:337)
echo        at org.bn.metadata.ASN1ChoiceMetadata.decode(ASN1ChoiceMetadata.java:50)
echo        at org.bn.coders.Decoder.decodePreparedElement(Decoder.java:162)
echo        at org.bn.coders.Decoder.decode(Decoder.java:37)
echo        at com.devoteam.srit.xmlloader.asn1.BN_ASNMessage.decode(BN_ASNMessage.java:121)
echo        at com.devoteam.srit.xmlloader.asn1.TestANS1Object.testProcessBIN(TestANS1Object.java:437)
echo        at com.devoteam.srit.xmlloader.asn1.TestANS1Object.testProcessAllIndexBIN(TestANS1Object.java:409)
echo        at com.devoteam.srit.xmlloader.asn1.TestANS1Object.testProcess(TestANS1Object.java:288)
echo        at com.devoteam.srit.xmlloader.asn1.TestANS1Object.main(TestANS1Object.java:218)
DER(1) DER(2) DER(4) DER(5) KO : FAILED 10, 1.144 s, 8.741259 /s.
