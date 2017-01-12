FROM centos:7

# Build display configuration (for IZPack pipe install hack)
# cols=128
# lines=65

# Install MTS (Master GUI)
RUN \
  yum -y install wget java-1.8.0-openjdk lksctp-tools libpcap && \
  wget "http://downloads.sourceforge.net/project/mts-ericsson/mts-6.2.1_beta-standard.jar?r=http%3A%2F%2Fsourceforge.net%2Fprojects%2Fmts-ericsson%2Ffiles%2F%3Fsource%3Ddirectory&ts=1452853512&use_mirror=netix" -O /root/mts.jar && \
  ((sleep 0.1; for _ in {0..28}; do echo -ne '\n'; sleep 0.1; done; sleep 1; echo '1'; sleep 1; echo '/root/mts'; sleep 1; echo '1'; sleep 1; echo '1024'; sleep 1; echo '1'; sleep 1) | /usr/bin/java -jar /root/mts.jar) && \
  rm -f /root/mts.jar && \
  echo '/usr/bin' > /root/mts/bin/java_home

# Prepare XFCE desktop environment
ENV DISPLAY :1
ENV NO_VNC_HOME /root/noVNC
ENV VNC_COL_DEPTH 24
ENV VNC_RESOLUTION 1280x900
ENV COMMON_PW mts-ericsson

RUN \
  yum -y install epel-release && \
  yum clean all && \
  yum -y update && \
  yum clean all && \
  yum -y install sudo && \
  yum --enablerepo=epel -y -x gnome-keyring --skip-broken groups install "Xfce" && \
  yum -y groups install "Fonts" && \
  yum -y install tigervnc-server net-tools unzip openssh-clients openssh-server && \
  mkdir -p $NO_VNC_HOME/utils/websockify && \
  wget -qO- https://github.com/kanaka/noVNC/archive/master.tar.gz | tar xz --strip 1 -C $NO_VNC_HOME && \
  wget -qO- https://github.com/kanaka/websockify/archive/v0.7.0.tar.gz | tar xz --strip 1 -C $NO_VNC_HOME/utils/websockify && \
  chmod +x -v /root/noVNC/utils/*.sh

# Pre-load desktop environment
ADD .vnc /root/.vnc
ADD .config /root/.config
ADD Desktop /root/Desktop
ADD scripts /root/scripts

# Configure SSHd environment
RUN \
  ssh-keygen -f /etc/ssh/ssh_host_rsa_key -N '' -t rsa && \
  ssh-keygen -f /etc/ssh/ssh_host_ecdsa_key -N '' -t ecdsa && \
  ssh-keygen -f /etc/ssh/ssh_host_ed25519_key -N '' -t ed25519

# Set root password (for SSH login)
RUN \
  echo $COMMON_PW | passwd root --stdin

RUN chmod +x  /root/scripts/*.sh /root/.vnc/xstartup /etc/xdg/xfce4/xinitrc
RUN /bin/dbus-uuidgen > /etc/machine-id

# Expose network service ports & start
EXPOSE 22
EXPOSE 5901
EXPOSE 6901

ENTRYPOINT ["/root/scripts/startup.sh"]
CMD ["--tail-log"]
