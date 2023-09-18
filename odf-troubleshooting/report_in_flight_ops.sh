#!/bin/bash

ffile=$1

if [[ -z $ffile || ! -f $ffile ]] ; then echo "Error: Cant find file $1, or not provided as an arg" ; exit 1 ; fi
if [[ $ffile != flight-dump* ]] ; then echo "Error: $1 is not a dump-session-ls file" ; exit 1 ; fi

# Guess PV file name,

cluster=$(oc config current-context|awk -F'/' '{print $2}'| awk -F '-' '{print $2"-"$3}')
pvfile="pvinfo-$cluster.json"
dumpfile=`echo $ffile | sed 's/flight-dump/ dump-session-ls/'`



cat $ffile |   jq '.ops[]  | "\(.initiated_at) , \(.type_data.client_info.client) , \(.description) , \(.type_data.flag_point)"' -r  | grep 'failed to rdlock' | while read line
do

  clientid=`echo $line | awk -F, '{print $2}'`
  echo $line
  echo "-from_session_dump"
  dumpinfo=`cat $dumpfile | jq '.[]| "\(.inst)  , \(.uptime) , \(.state) , \(.num_caps) , \(.client_metadata.root) , \(.client_metadata.hostname)" ' -r   | sort -k5 -V  |grep $clientid`

  echo $dumpinfo
  echo "-"
  # get volumename
  vol=`echo $dumpinfo | awk -F, '{print $5}' | awk -F/ '{print $4}'`
 cat $pvfile | jq  --arg vol $vol ' .items[] | select(.status.phase=="Bound")  |  select(.spec.csi.volumeAttributes.subvolumeName==$vol)  |  "\(.spec.csi.volumeAttributes.subvolumeName)  \(.spec.claimRef.namespace)  \(.spec.claimRef.name)" '

 inode=`echo $line | sed 's/.* #\(0x.*\) [0-9].*/\1/'`
 export INODE=`printf %d $inode`
 echo inode=$INODE

  
  `dirname $0`/report_inode_clients.sh < /dev/null 

 
echo


done



