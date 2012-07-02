#!/bin/bash
# Set the field seperator to a newline
IFS="
"
# Loop through the file
for line in `cat user.csv`;do
# Echo the line (echo could be changed to whatever command you want)
 kamctl add $line imt30imt30
done
