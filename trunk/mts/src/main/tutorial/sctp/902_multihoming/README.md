# sctp multihoming test

2 hosts : a server, a client:
- the server opens a a sctp listenpoint bound to a subset of its available local interfaces addresses (sctp configuration listenpoint.LOCAL_HOST).
- the client creates a sctp channel bound to a subset of its available local interfaces addresses (openChannelSCTP localHost attribute).
- the client periodically send a message, the server echos.
- the user alters the network topology and checks in a network capture that the sctp stack adapts the IP route accordingly.

# VirtualBox based test implementation

the hosts are linux (debian) VirtualBox VMs.

The guest have 4 internal network (intnet1 to intnet4).
An internal network can be shared between different hosts that have to be interconnected.
Each internal network can also have DHCP server enabled.
See section *6.6. Internal networking* of the *VirtualBox User Manual* for detailed information.
The internal networks are configured to be on different subnets.

Each VM have 4 virtual network cards bound to the internal networks (intnet1 to intnet4).
Each host defines 4 corresponding interfaces (eth0 to eth3)

The scenario 902_multihoming_server is started on the 1st host.
The sctp listenpoint is bound to the eth1 and eth2 local addresses.

The scenario 902_multihoming_client is started on the 2nd host.
The sctp channel is bound to the eth1 and eth2 local addresses.

The user use the VirtualBox network cards gui to detach/re-attach the interfaces:
0. with all interfaces attached, messages should be routed through eth1 (intnet2)
1. detach client.eth1: messages should be routed through eth2 (intnet3)
2. attach client.eth1: messages should be routed through eth1 (intnet2)
3. detach client.eth2: messages should be routed through eth1 (intnet2)
4. detach client.eth1: the client connection should be broken

