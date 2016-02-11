FROM centos:7

# Build display configuration (for IZPack pipe install hack)
# cols=128
# lines=65

RUN \
  yum -y install wget java-1.8.0-openjdk lksctp-tools libpcap && \
  wget "http://downloads.sourceforge.net/project/mts-ericsson/mts-6.2.1_beta-standard.jar?r=http%3A%2F%2Fsourceforge.net%2Fprojects%2Fmts-ericsson%2Ffiles%2F%3Fsource%3Ddirectory&ts=1452853512&use_mirror=netix" -O /root/mts.jar && \
  ((sleep 0.1; for _ in {0..28}; do echo -ne '\n'; sleep 0.1; done; sleep 1; echo '1'; sleep 1; echo '/root/mts'; sleep 1; echo '1'; sleep 1; echo '1024'; sleep 1; echo '1'; sleep 1) | /usr/bin/java -jar /root/mts.jar) && \
  rm -f /root/mts.jar && \
  echo '/usr/bin' > /root/mts/bin/java_home

WORKDIR "/root/mts/bin/"

CMD ["./startSlave.sh"]
