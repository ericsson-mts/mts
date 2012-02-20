n=33960310000
m=0
file=userNoAS40k.csv
echo "inviter;NumberPhone" >$file
while [ "$m" -le 39999 ]
  do
p=`expr $m + $n`
echo "+$p;$p">>$file
m=`expr $m + 1`
done
