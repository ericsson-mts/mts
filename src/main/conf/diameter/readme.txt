A mettre dans le dossier ethereal (pour win) 
<<libxml2.dll>> 
A mettre dans Ethereal/diameter 
<<ericssonIMT30Charging.xml>> <<ericssonIMT30Cx.xml>> 
Modifier Ethereal/diameter/dictionary.xml 
<?xml version="1.0" encoding="UTF-8"?> 
<!-- $Id: dictionary.xml 16640 2005-12-01 18:43:26Z etxrab $ --> 
<!DOCTYPE dictionary SYSTEM "dictionary.dtd" [ 
        <!ENTITY nasreq SYSTEM "nasreq.xml"> 
        <!ENTITY mobileipv4 SYSTEM "mobileipv4.xml"> 
        <!ENTITY chargecontrol SYSTEM "chargecontrol.xml"> 
        <!ENTITY sunping SYSTEM "sunping.xml"> 
        <!ENTITY imscxdx SYSTEM "imscxdx.xml"> 
        <!ENTITY TGPPSh SYSTEM "TGPPSh.xml"> 
        <!ENTITY ericssonIMT30Charging SYSTEM "ericssonIMT30Charging.xml"> 
        <!ENTITY ericssonIMT30Cx SYSTEM "ericssonIMT30Cx.xml"> 
]> 
<dictionary> 
        <base uri="http://www.ietf.org/rfc/rfc3588.txt"> 

        […] 
        &sunping; 
        &imscxdx; 
        &TGPPSh; 
        &ericssonIMT30Charging; 
        &ericssonIMT30Cx; 
</dictionary> 
