m=129
while [ "$m" -le 143 ]
  do
  n=0
  while [ "$n" -le 253 ]
  do
  n=`expr $n + 1`
  ip addr del 172.18.$m.$n/20 dev eth0
  done
m=`expr $m + 1`
done
