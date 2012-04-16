#!/bin/bash

file_numplan_orig="plan.csv"

file_numplan_tmp="plan.csv.tmp"

tail -n+2 $file_numplan_orig > $file_numplan_tmp

file_numsip_accounts="numsip_accounts.conf"

dir_sip_to_out="sip_to_out"
dir_in_to_sip="in_to_sip"
dir_sip_to_caller="sip_to_caller"

dir=$dir_sip_to_out
rm -rf $dir && mkdir $dir

dir=$dir_sip_to_caller
rm -rf $dir && mkdir $dir

dir=$dir_in_to_sip
rm -rf $dir && mkdir $dir

file=$file_numsip_accounts
rm -f $file
touch $file

# read numplan.csv line per line
while read line
do
	#if the line is not empty
	if [ "$line" != "" ]
	then
		#read short_number and long_number
		sip=`echo $line | cut -d";" -f1`
		in=`echo $line | cut -d";" -f3`
		out=`echo $line | cut -d";" -f4`
		caller=`echo $line | cut -d";" -f5`


		echo -n $in > "$dir_sip_to_out/$sip"
		echo -n $caller > "$dir_sip_to_caller/$sip"
		
		out=$(echo $out|sed 's/\//_/g') # replace / with a _ 
		echo -n $sip > "$dir_in_to_sip/$out"

		echo "[$sip](default)" >> $file_numsip_accounts
	fi
done < $file_numplan_tmp

rm -f $file_numplan_tmp

