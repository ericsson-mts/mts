m=0
while [ "$m" -le 255 ]
  do

  n=0
  while [ "$n" -le 254 ]
  do
  n=`expr $n + 1`
  ip addr add 172.21.$m.$n/16 dev eth0
  done
m=`expr $m + 1`
done
ip addr del 172.21.0.1
ip addr del 172.21.255.255
ip link set arp on dev eth0

