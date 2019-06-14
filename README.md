# MTS: Multiprotocol Test Tool![build status](https://travis-ci.org/ericsson-mts/mts.svg?branch=master)

MTS (Multi-protocol Test Suite) is a multi-protocol testing tool specially designed for telecom IP-based architectures (see above "Features" section for more details).

## MTS meets needs like :
* Functional, non-regression or protocol tests => ‘Sequential’ mode
* Load, performance endurance stress tests => ‘Parallel’ mode (master/slave)
* Simulates equipment => client, server or both sides
* System supervision => capture mode (like wireshark)

## Product characteristics :
* Definition of tests case in XML files : test and scenarios input files
* Graphical (very convivial) or command line (for test automation) user interfaces
* Easy to use : logging management and rich statistics presentation.
* Pure software solution => support only IP based protocol
* Written in java => supports many famous platforms : Windows and Linux supported
* Open Source product since begin 2012 with GPLV3 license => free to use

## Developers :
* Compile and use / test : ```mvn package```
* Generate installer : ```mvn install```
