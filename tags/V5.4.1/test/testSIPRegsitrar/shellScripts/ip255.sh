
  n=1
  while [ "$n" -le 253 ]
  do
  n=`expr $n + 1`
  ip addr add 172.18.$1.$n/24 dev eth0
  done


