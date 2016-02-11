#!/bin/bash

# Script to instruct the Mac how to route packets to the
# software defined network where containers created via boot2docker
# reside.  This lets you casually directly to ports (ssh, http, etc. etc.)
# on those containers.

function ERROR(){ echo "ERROR: $*" ; }
function FAIL(){ echo "FAILING: $*" ; exit 1; }

if [[ 'Running' != $(docker-machine status default) ]]; then
  FAIL "Docker Machine not running";
fi

IP_OF_DOCKER_HOST=$(docker-machine ip default 2> /dev/null)
SDN_NET_FOR_CONTAINERS=$(docker-machine ssh default ip route show 2> /dev/null | awk '/docker0/{print $1}' )

echo "Establishing route to $SDN_NET_FOR_CONTAINERS via $IP_OF_DOCKER_HOST"

if netstat -rn | awk "\$2 == \"$IP_OF_DOCKER_HOST\" {exit(1);}" ; then
  sudo route -n add "$SDN_NET_FOR_CONTAINERS" "$IP_OF_DOCKER_HOST"
else
  ERROR "Already routing something to the docker VM ..."

  netstat -rn | awk '$2 == "192.168.99.1"||/Destination.*Refs/{print;}'

  FAIL "... so never mind."
fi
