import com.devoteam.srit.xmlloader.core.utils.Utils;
class Capture extends MTSScript {

	/**
	 * start wireshark capture
	 */
	def start() {
		//only start capture if enabled 
		if (capture_enabled) {
			debug("start capture $testcaseName")
			if (System.properties['os.name'].toLowerCase().contains('linux')) {
				"tcpdump -B 1 -k -f ip -b duration:300 -i rpcap://172.20.170.130/eth1 -w $CaptureDirName/${testcaseName}.pcap".execute()
			} else if (System.properties['os.name'].toLowerCase().contains('windows')) {
				//start capture on windows
				//"E:/MTS/mts_tests_nbi/capture.bat fileName".execute()
				"$CaptureExe -B 1 -k -f ip -b duration:300 -i rpcap://172.20.170.130/eth1 -w $CaptureDirName/${testcaseName}.pcap".execute()
			}
			//wait for wireshark start
			Utils.pauseMilliseconds(capture_delay*1000)
		}
	}

	/**
	 * stop wireshark capture
	 */
	def stop() {
		//only stop capture if enabled 
		if (capture_enabled) {
			debug("stop capture $testcaseName")
			if (System.properties['os.name'].toLowerCase().contains('linux')) {
				//@TODO:stop capture on linux
			} else if (System.properties['os.name'].toLowerCase().contains('windows')) {
				//stop capture on windows
				"taskkill /IM wireshark.exe /F".execute()
			}
		}
	}

}

