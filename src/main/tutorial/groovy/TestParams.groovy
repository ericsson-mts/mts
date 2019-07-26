/**
 * this class contains the parameters declared at the test level
 *
 */
class TestParams extends MTSScript {

	/**
	 * run is executed when the script is loaded, ie when the MTS test is opened or reloaded
	 */
	def run() {

		
		// -------------------------------------
		// String parameters examples
		// -------------------------------------
		UUI="04746573742022707572706f73652d757569222064752031332d30322d32303135;encoding=hex;content=isdn-uui;purpose=isdn-uui"

		CallerPrivacy="none"
		CalleePrivacy="none"

		CallerCodecs="8_18_101"
		CalleeCodecs="8_101"

		MTS_PortSIP=editable("5060")
		Remote_PortSIP=editable("5060")
		/**
		 This is equivalent to the following MTS params:
		 <parameter name="[UUI]" operation="set" value="04746573742022707572706f73652d757569222064752031332d30322d32303135;encoding=hex;content=isdn-uui;purpose=isdn-uui" />
		 <parameter name="[CallerPrivacy]" operation="set" value="none" />
		 <parameter name="[CalleePrivacy]" operation="set" value="none" />
		 <parameter name="[CallerCodecs]" operation="set" value="8_18_101"/>
		 <parameter name="[CalleeCodecs]" operation="set" value="8_101"/>
		 <parameter name="[MTS_PortSIP]"  editable="true" operation="set" value="5060" />
		 <parameter name="[Remote_PortSIP]"  editable="true" operation="set" value="5060" />
		 */
 
		// -------------------------------------
		// other parameters examples
		// -------------------------------------
		//parameter([name:"FileWave",operation:"file.readwave",value:"../../resources/vb_court.wav",value2:"20"])
		/**
		 This is equivalent to the following MTS params:
		 <parameter name="FileWave" operation="file.readwave" value="resources/vb_court.wav" value2="20"/>
		 */
		
		// -------------------------------------
		// Conditional configuration example
		// -------------------------------------
		def example=false

		if (example) {
			// -------------------------------------
			// List parameters examples
			// -------------------------------------
			media_list=["G711","G722"]
			/**
			This is equivalent to the following MTS params:

			<parameter name="[media_list]" operation="list.create" />
			<parameter name="[media_list(0)]" operation="set" value="G711"/>
			<parameter name="[media_list(1)]" operation="set" value="G722"/>
			*/
		
			LastSDPMediaFormat="8"
		}


		// -------------------------------------
		// bloc parameters examples
		// -------------------------------------
		setJenkinsParams()
		//setPOParams()

	}
	
	/**
	 * specific configuration for Patrice machine
	 */
	def setPOParams() {
		//Variables concernant TPO 11
		MTS_Tpo11ID="MTS_Tpo11"
		MTS_Tpo11Host=editable("172.20.170.161")
		MTS_Tpo11Nb=editable("+33399121020")
		MTS_Tpo11NbFromSP=editable("+33399121020")
		MTS_Tpo11NNb=editable("0399121020")
		MTS_Tpo11Domain=editable("mts-tpo11.bzh")
		MTS_Tpo11Proxy=editable("172.20.180.138")
		MTS_Tpo11RTPport=editable("11000")
		MTS_Tpo11PANI=editable("630821111")

		MTS_Tpo11FromAtaNb=editable("0399121020")

		//Variables concernant TPO 12
		MTS_Tpo12ID="MTS_Tpo12"
		MTS_Tpo12Host="172.20.170.162"
		MTS_Tpo12Nb="+33399121030"
		MTS_Tpo12NbFromSP="+33399121030"
		MTS_Tpo12NNb="0399121030"   //NNb = NationalNumber
		MTS_Tpo12Domain="mts-tpo12.bzh"
		MTS_Tpo12Proxy="172.20.180.139"
		MTS_Tpo12RTPport="12000"
		MTS_Tpo12PANI="630821212"
		MTS_Tpo12FromAtaNb="0399121030"
		MTS_Tpo12ViaCsNb="+33296101698"
		
		//Variables concernant PFS 4
		MTS_Pfs4ID="MTS_Pfs4"
		MTS_Pfs4Host="10.194.127.204"
		MTS_Pfs4_NativeNb="+33296105245"
		MTS_Pfs4_PortedInNb="+33296105241"
		//MTS_Pfs4_ZABPQmcdu="+33296105245"
		rn_Pfs4="20984"
		MTS_Pfs4Proxy="172.20.197.108"
		//MTS_Pfs4Nb="+33[MTS_Pfs4_ZABPQmcdu]"
		MTS_Pfs4Nb=MTS_Pfs4_NativeNb
		MTS_Pfs4RTPport="11400"

		capture_enabled=editable(false)
		//delay before test start in order to wait for capture startup
		capture_delay=10
		//capture directory
		CaptureDirName="E:/MTS/traces/"
		//path to capture executable
		CaptureExe="E:/MTS/MTS_Tests_Auto/Wireshark.exe"
	}
	
	/**
	 * specific configuration for Continuous Integration machine with jenkins
	 */
	def setJenkinsParams() {
		//-------------------------------------
		//Variables concernant TPO 11
		//-------------------------------------
		MTS_Tpo11ID="MTS_Tpo11"
		MTS_Tpo11Host=editable("172.20.170.175")
		MTS_Tpo11Nb=editable("+33399121050")
		MTS_Tpo11NbFromSP=editable("+33399121050")
		MTS_Tpo11NNb=editable("0399121050")
		MTS_Tpo11Domain=editable("mts-tpomtsauto1.bzh")
		MTS_Tpo11Proxy=editable("172.20.14.217")
		MTS_Tpo11RTPport=editable("31000")
		MTS_Tpo11PANI=editable("6308221050")
		MTS_Tpo11FromAtaNb=editable("0399121050")

		//-------------------------------------
		//Variables concernant TPO 12
		//-------------------------------------
		MTS_Tpo12ID="MTS_Tpo12"
		MTS_Tpo12Host="172.20.170.176"
		MTS_Tpo12Nb="+33399121052"
		MTS_Tpo12NbFromSP="+33399121052"
		MTS_Tpo12NNb="0399121052"   //NNb = NationalNumber
		MTS_Tpo12Domain="mts-tpomtsauto2.bzh"
		MTS_Tpo12Proxy="172.20.14.218"
		MTS_Tpo12RTPport="32000"
		MTS_Tpo12PANI="6308221052"
		MTS_Tpo12FromAtaNb="0399121052"
		
		capture_enabled=editable(false)
		//delay before test start in order to wait for capture startup
		capture_delay=10
		//capture directory
		CaptureDirName="/tmp"
		//path to capture executable
		CaptureExe="/bin/wireshark"
	}



}