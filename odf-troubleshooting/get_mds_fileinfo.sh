#!/bin/bash

#for instance in "a" "b"
ns="openshift-storage"

toolpod=`oc get po -l app=rook-ceph-tools -n $ns -o jsonpath={.items[].metadata.name}`

if [[ ${toolpod}x == 'x' ]] ; then
  echo "No tool pod so can't tell you which mds is active"
else
  echo -e "MDS Pod States\n--------------"
  oc exec  -it $toolpod -n $ns  -- ceph fs dump 2>&1 | grep storagecluster-cephfilesystem-[ab] | cut -f1-3 -d\ 
  echo
fi


echo -e  "which MDS to collect data from ( a b , just hit return for both a and b) : \c"
read mdsi
if [[ $mdsi == "" ]] ; then
  mdsi="a b"
elif [[ ! ( $mdsi == 'a' || $mdsi == 'b' ) ]];  then
  echo "Exiting: \"$mdsi\" is not a mds instance, expecting a or b"
  exit 99
fi


frmt=$(date +%d-%m_%H-%M)

cluster=$(oc config current-context|awk -F'/' '{print $2}'| awk -F '-' '{print $2"-"$3}')
echo get cluster pv info
#oc get pv -o json > pvinfo-$cluster-$frmt.json
oc get pv -o json > pvinfo-$cluster.json

#for instance in "a" "b"
for instance in  $mdsi
do
  pod=`oc get po -l mds=ocs-storagecluster-cephfilesystem-$instance -o jsonpath={.items[].metadata.name} -n $ns`

  echo "Get Session dump for $pod"
  oc exec -it $pod -c mds -n $ns --  ceph daemon mds.ocs-storagecluster-cephfilesystem-$instance session ls  > dump-session-ls-$instance-$cluster-$frmt.json
#cat /tmp/session-$instance  > dump-session-ls-$instance-$cluster-$frmt.json

  echo "Get Perf dump for $pod"
  oc exec -it $pod -c mds -n $ns --  ceph daemon mds.ocs-storagecluster-cephfilesystem-$instance perf dump > dump-perf-$instance-$cluster-$frmt.json


  echo "Get Flight dump for $pod"
  oc exec -it $pod -c mds -n $ns -- ceph daemon mds.ocs-storagecluster-cephfilesystem-$instance dump_ops_in_flight > flight-dump-$instance-$cluster-$frmt.json

  echo -e "\n\n"

done


exit 0
