.SUFFIXES: .java .class

.java.class:
#	javac -g -cp .:examples $<
	javac -cp .:examples:../JavaSCTP/JavaSCTP.jar $<

P=dk/i1/diameter
CLASSES=$P/packunpack.class \
        $P/AVP.class \
        $P/AVP_Integer32.class \
        $P/AVP_Unsigned32.class \
        $P/AVP_Integer64.class \
        $P/AVP_Unsigned64.class \
        $P/AVP_OctetString.class \
        $P/AVP_Float32.class \
        $P/AVP_Float64.class \
        $P/AVP_Grouped.class \
        $P/AVP_UTF8String.class \
        $P/AVP_Time.class \
        $P/AVP_Address.class \
        $P/InvalidAVPLengthException.class \
        $P/InvalidAddressTypeException.class \
        $P/MessageHeader.class \
        $P/Message.class \
        $P/ProtocolConstants.class \
        $P/Utils.class \
        $P/node/Capability.class \
        $P/node/UnsupportedURIException.class \
        $P/node/EmptyHostNameException.class \
        $P/node/NodeValidator.class \
        $P/node/DefaultNodeValidator.class \
        $P/node/Peer.class \
        $P/node/MessageDispatcher.class \
        $P/node/DefaultMessageDispatcher.class \
        $P/node/ConnectionListener.class \
        $P/node/DefaultConnectionListener.class \
        $P/node/ConnectionKey.class \
        $P/node/InvalidSettingException.class \
        $P/node/StaleConnectionException.class \
        $P/node/NodeSettings.class \
        $P/node/ConnectionBuffers.class \
        $P/node/NormalConnectionBuffers.class \
        $P/node/ConnectionTimers.class \
        $P/node/Connection.class \
        $P/node/AVP_FailedAVP.class \
        $P/node/InvalidAVPValueException.class \
        $P/node/NotRoutableException.class \
        $P/node/NotAnAnswerException.class \
        $P/node/NotARequestException.class \
        $P/node/NotProxiableException.class \
        $P/node/NodeState.class \
        $P/node/NodeImplementation.class \
        $P/node/TCPConnection.class \
        $P/node/TCPNode.class \
        $P/node/RelevantSCTPAuthInfo.class \
        $P/node/SCTPConnection.class \
        $P/node/SCTPNode.class \
        $P/node/UnsupportedTransportProtocolException.class \
        $P/node/Node.class \
        $P/node/NodeManager.class \
        $P/node/SimpleSyncClient.class \
        $P/session/Session.class \
        $P/session/InvalidStateException.class \
        $P/session/SessionManager.class \
        $P/session/SessionAuthTimers.class \
        $P/session/BaseSession.class \
        $P/session/AASession.class \
        $P/session/ACHandler.class \
        examples/TestSession.class \
        examples/TestSessionTest.class \
        examples/load/TestSessionTest2.class \
	examples/load/TestSessionServer.class \
	examples/asr/asr.class \
	examples/cc/cc_test_client.class \
	examples/cc/cc_test_server.class \
	examples/relay/simple_relay.class \
	abnf/ABNFConverter.class \

.PHONY: all
all: $(CLASSES)

Diameter.jar: $(CLASSES)
	jar -cf $@ `find dk -name \*.class`

.PHONY: clean
clean:
	find . -name \*.class -exec rm {} \;
	rm -f Diameter.jar
	rm -rf doc/

.PHONY: doc
doc:
	javadoc -sourcetab 8 \
	        -d ./doc \
	        -classpath ../JavaSCTP/JavaSCTP.jar:. \
	        -windowtitle "Java Diameter API" \
	        -notimestamp \
	        -stylesheetfile stylesheet.css \
	        -overview overview.html \
	        dk.i1.diameter dk.i1.diameter.node dk.i1.diameter.session
.PHONY: fixdoc
fixdoc:
	rm -rf doc/src-html
	find doc/dk -name \*.html -exec ./remove-source-references.sh {} \;

BNAME=Diameter-$(shell cat version)
.PHONY: bindist
bindist: Diameter.jar
	ln -sf `basename $(shell pwd)` ../$(BNAME)
	cd .. && tar cvfz $(BNAME)/$(BNAME)-jars.tar.gz \
	    $(BNAME)/Diameter.jar \
	    $(BNAME)/version $(BNAME)/LICENSE \
	    $(BNAME)/README.sctp \
	    $(BNAME)/README.bindist
	rm -f ../$(BNAME)

.PHONY: misc
miscdist:
	ln -sf `basename $(shell pwd)` ../$(BNAME)
	cd .. && tar cvfz $(BNAME)/$(BNAME)-misc.tar.gz \
	    `find $(BNAME)/examples -name \*.java` \
	    `find $(BNAME)/abnf -name \*.java` \
	    $(BNAME)/version $(BNAME)/LICENSE \
	    $(BNAME)/README.misc
	rm -f ../$(BNAME)

.PHONY: srcdist
srcdist:
	ln -sf `basename $(shell pwd)` ../$(BNAME)
	cd .. && tar cvfz $(BNAME)/$(BNAME)-src.tar.gz \
	    $(BNAME)/Makefile \
	    `find $(BNAME)/dk -name \*.java` \
	    $(BNAME)/version $(BNAME)/LICENSE \
	    $(BNAME)/README.src
	rm -f ../$(BNAME)

.PHONY: docdist
docdist: doc fixdoc
	ln -sf `basename $(shell pwd)` ../$(BNAME)
	cd .. && tar cvfz $(BNAME)/$(BNAME)-doc.tar.gz \
	    $(BNAME)/doc \
	    $(BNAME)/version $(BNAME)/LICENSE \
	    $(BNAME)/README.doc
	rm -f ../$(BNAME)

.PHONY: dist
dist: bindist miscdist srcdist docdist
