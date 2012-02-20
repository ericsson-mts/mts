
  n=1
  while [ "$n" -le 253 ]
  do
  n=`expr $n + 1`
  ip addr del 172.18.$1.$n/24 dev eth0
  done
