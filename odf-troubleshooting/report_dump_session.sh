#!/bin/bash

dfile=$1
if [[ -z $dfile || ! -f $dfile ]] ; then echo "Error: Cant find file $1, or not provided as an arg" ; exit 1 ; fi
if [[ $dfile != dump-session-ls* ]] ; then echo "Error: $1 is not a dump-session-ls file" ; exit 1 ; fi

cluster=$(oc config current-context|awk -F'/' '{print $2}'| awk -F '-' '{print $2"-"$3}')
pvfile="pvinfo-$cluster.json"

cat $dfile | jq '.[] | "\(.inst)  , \(.uptime) , \(.state) , \(.num_caps) , \(.client_metadata.root) , \(.client_metadata.hostname)" ' -r |  sort -k4 -t, -n  | tail -100 | while read line ; do

  echo $line
  # get volumename
  vol=`echo $line | awk -F, '{print $5}' | awk -F/ '{print $4}'`


  if [[ ${vol}x != "x" ]] ; then 
     cat $pvfile | jq  --arg vol $vol ' .items[] | select(.status.phase=="Bound")  |  select(.spec.csi.volumeAttributes.subvolumeName==$vol)  |  "\(.spec.csi.volumeAttributes.subvolumeName)  \(.spec.claimRef.namespace)  \(.spec.claimRef.name)" '
  fi

  echo

done

