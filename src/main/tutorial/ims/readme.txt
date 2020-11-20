***********************************************************************************************************
						Project IMS
***********************************************************************************************************

List of directories :

1) Core : script for the xCSCF (Alexandre BOCQUEL)

2) Terminal : script for testing IMS as terminals (Alexandre BOCQUEL)

3) Library : script common used by Core and Terminal part (Alexandre BOQUEL)

4) Unitary : script for testing each IMS module unitary (Alexandre BOCQUEL) 

5) Load : scripts for load test of IMS core with registration and basic callflow (Yves PREMEL) 

***********************************************************************************************************

To configure the IMS client, in order to reach the IMS core :

1) put the contain of the host file into you C:\<Windows dir>\system32\drivers\etc\host file, set the parameter 
with PCSCF IP address
and restart all IMSloader instance to take these changes into account.

2) set in the sip.properties the javax.sip.OUTBOUND_PROXY parameter with the PCSCF IP address, port, transport 

3) set in the test.xml file the [Host-Server] parameter with the PCSCF IP address and the [Port-Server] 
one with PCSCF port

