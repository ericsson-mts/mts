echo off
echo Generate TCAP

asn1_bn_compiler.bat tcap
asn1_java_compiler.bat tcap
asn1_templater.bat tcap

echo Generate MAP

asn1_bn_compiler.bat map
asn1_java_compiler.bat map
asn1_templater.bat map
