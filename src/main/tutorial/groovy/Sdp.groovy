class Sdp extends MTSScript {

	
	def generate_sdp_caller(sdpType) {
		log("generate_sdp_caller($sdpType)")
		def sdpStart="""\
v=0
o=[[Caller]_ID] 1[CallerSdpSessionId] 1[CallerSdpSessionVersion] IN IP4 [[Caller]_Host]
s=Phone-Call
c=IN IP4 [[Caller]_Host]
t=0 0
"""
		switch (sdpType) {
			case "8_18_101":
				//generate_sdp_caller: SDP insertion G711 G729 and RTPevent
				return getMTSParam(sdpStart+"""\
m=audio [[Caller]_RTPport] RTP/AVP 8 18 101
a=rtpmap:8 PCMA/8000
a=rtpmap:18 G729/8000
a=fmtp:18 annexb=no
a=rtpmap:101 telephone-event/8000
a=fmtp:101 0-15
a=ptime:20
a=sendrecv
""")

			case "8_101":
				//generate_sdp_caller: SDP insertion G711 and RTPevent
				return getMTSParam(sdpStart+"""\
m=audio [[Caller]_RTPport] RTP/AVP 8 101
a=rtpmap:8 PCMA/8000
a=rtpmap:101 telephone-event/8000
a=fmtp:101 0-15
a=ptime:20
a=sendrecv
""")

			case "8_only":
				//generate_sdp_caller: SDP insertion G711 only 
				return sdpStart+"""\
m=audio [[Caller]_RTPport] RTP/AVP 8
a=rtpmap:8 PCMA/8000
a=ptime:20
a=sendrecv
"""

			case "18_only":
				//generate_sdp_caller: SDP insertion G729 only
				return sdpStart+"""\
m=audio [[Caller]_RTPport] RTP/AVP 18
a=rtpmap:18 G729/8000
a=fmtp:18 annexb=no
a=ptime:20
a=sendrecv
"""

			case "8_18annexByes_101":
				//generate_sdp_caller: SDP insertion G711 G729 annexb=yes and RTPevent
				return """v=0
m=audio [[Caller]_RTPport] RTP/AVP 8 18 101
a=rtpmap:8 PCMA/8000
a=rtpmap:18 G729/8000
a=fmtp:18 annexb=yes
a=rtpmap:101 telephone-event/8000
a=fmtp:101 0-15
a=ptime:20
a=sendrecv
"""


			default:
				//generate_sdp_caller: Error: SDP type not supported !!! 
				//<exit failed="true"/>
				//System.exit(0)
				return null
		}
	}



	def generate_sdp_callee(sdpType) {
		def sdpStart="""\
v=0
o=[[Callee]_ID] 1[CalleeSdpSessionId] 1[CalleeSdpSessionVersion] IN IP4 [[Callee]_Host]
s=Phone-Call
c=IN IP4 [[Callee]_Host]
t=0 0
"""

		switch (sdpType) {
			case "8_101":
				//generate_sdp_callee: SDP insertion G711 and RTPevent
				return sdpStart+"""\
m=audio [[Callee]_RTPport] RTP/AVP 8 101
a=rtpmap:8 PCMA/8000
a=rtpmap:101 telephone-event/8000
a=fmtp:101 0-15
a=ptime:20
a=sendrecv
"""

			case "18_101":		
				//generate_sdp_callee: SDP insertion G729 and RTPevent 
				return sdpStart+"""\
m=audio [[Callee]_RTPport] RTP/AVP 18 101
a=rtpmap:18 G729/8000
a=fmtp:18 annexb=no
a=rtpmap:101 telephone-event/8000
a=fmtp:101 0-15
a=ptime:20
a=sendrecv
"""

			case "18annexByes_101":
				//generate_sdp_callee: SDP insertion G729 annexb=yes and RTPevent 
				return sdpStart+"""\
m=audio [[Callee]_RTPport] RTP/AVP 18 101
a=rtpmap:18 G729/8000
a=fmtp:18 annexb=yes
a=rtpmap:101 telephone-event/8000
a=fmtp:101 0-15
a=ptime:20
a=sendrecv
"""

			case "8_only":
				//generate_sdp_callee: SDP insertion G711 only 
				return sdpStart+"""\
m=audio [[Callee]_RTPport] RTP/AVP 8
a=rtpmap:8 PCMA/8000
a=ptime:20
a=sendrecv
"""

			default:
				//generate_sdp_callee: Error: SDP type not supported !!! 
				//<exit failed="true"/>
				//System.exit(0)
				return null
		}
	}


}