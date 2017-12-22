
class Control extends AssertScript {

  def control(control) {
    //dynamically call the right function
	"$control"()
  }

/**
 * Control realized when TPO receives a call from TPO
 * SDP codec offer, Privacy header, From URI, Contact header, PAI, annexb=no, UUI, PANI.......
 */
  def control_on_TPOtoTPO_callee() {
 
	//Checking that  G711, G729 and  rtpevent (101) are in the SDP offer
	testAndCheckLater{assert LastSDPMediaFormat.matches(/^(8|18|101)$/)  }
	testAndCheckLater{assert getMTSParam("[LastSDPMediaFormat]").matches(/^(8|18|101)$/)  }
	//Checking that  privacy=none
	testAndCheckLater{assert lastPrivacy.equals(CalleePrivacy)  }
	//Cheching that  FromUri is not anonymous
	testAndCheckLater{assert (lastFromUri.contains(getMTSParam("sip:[[Caller]Nb]@[[Caller]_Host];user=phone"))
			  ||	 lastFromUri.contains(getMTSParam("sip:[[Caller]Nb]@[[Callee]_remoteHost];user=phone")))  }
	//Cheching that  Contact Header is correct
	testAndCheckLater{assert lastContactURI.contains(getMTSParam("sip:[[Caller]Nb]@[[Callee]_remoteHost]"))  }
	//Cheching that  PAI is correct
	testAndCheckLater{assert lastPAssertedIdentity.contains(getMTSParam("[[Caller]Nb]"))  }
	//Checking that annexb=no in  SDP offer
	testAndCheckLater{assert !LastSDPMediaAttribut.matches("a=fmtp:18 annexb=no|")  }
	//Checking User to User Info
	testAndCheckLater{assert lastUUI.equalsIgnoreCase(getMTSParam("[UUI]"))  }
	//Checking PANI
	testAndCheckLater{assert !lastPAccessNetworkInfo.contains(getMTSParam("[[Caller]_PANI]"))  }
		
	
	//testAndCheckLater{assert getMTSParam("[[Caller]_tel]")!="0296000000"  }
	//testAndCheckLater{assert getMTSParam("[[Caller]_tel]")=="0296123456"  }
	//testAndCheckLater{assert getMTSParam("[[Caller]_tel]")=="0296000000"  }
	
	//fail now if any of the previous tests failed
	checkResults()
  }


}