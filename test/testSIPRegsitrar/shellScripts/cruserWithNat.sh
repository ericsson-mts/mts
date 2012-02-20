m=0
n=1
firstImpu=33960200000
indexImpu=0
file=userWithNat60k.csv

echo "inviter;NumberPhone;privateIp" >$file
while [ "$indexImpu" -le 59999 ]
do
	p=`expr $indexImpu + $firstImpu`
	privIP=172.16.$m.$n
	n=`expr $n + 1`
	if [ "$n" = "254" ]; then
 		n=1
		m=`expr $m + 1`
	fi
	echo "+$p;$p;$privIP">>$file
	indexImpu=`expr $indexImpu + 1`
done
