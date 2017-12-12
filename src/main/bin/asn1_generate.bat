echo off
echo Generate TCAP
echo ERROR XML=0/15,  BER=0/15,  DER=0/15, 
#asn1_bn_compiler.bat tcap
#asn1_java_compiler.bat tcap
asn1_templater.bat .tcap

echo Generate MAP
echo ERROR XML=0/225,  BER=1/225,  DER=1/225, 
echo Process class[49] = SendRoutingInfoRes.xml => BER(1) DER(1) KO : FAILED
#asn1_bn_compiler.bat map
#asn1_java_compiler.bat map
asn1_templater.bat .map

echo Generate CAP
echo ERROR XML=0/348,  BER=2/348,  DER=2/348, 
echo Process class[47] = SendRoutingInfoRes.xml => BER(1) BER(3) BER(5) DER(1) DER(3) DER(5) KO : FAILED
echo Process class[285] = CallGapArg.xml => BER(1) BER(3) BER(5) DER(1) DER(3) DER(5) KO : FAILED
#asn1_bn_compiler.bat cap
#asn1_java_compiler.bat cap
asn1_templater.bat .cap