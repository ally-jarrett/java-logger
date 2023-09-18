#!/bin/bash
ns="openshift-storage"

toolpod=`oc get po -l app=rook-ceph-tools -n $ns -o jsonpath={.items[].metadata.name}`

if [[ ${toolpod}x == 'x' ]] ; then
  echo -e  "No tool pod so can't tell you which mds is active"
else  
 echo -e      "\n\n#####################################################################################"
 echo -e          "############### Ceph Cluster Health (  'ceph health detail' )    ####################"
 echo -e          "#####################################################################################\n"
 oc exec -it $toolpod -n $ns --  ceph health detail
 echo -e        "\n#####################################################################################"
 echo -e          "############### Ceph Cluster Health ( 'ceph -s' ) ###################################"
 echo -e          "#####################################################################################\n"
 oc exec -it $toolpod -n $ns --  ceph -s
 echo -e      "\n\n#####################################################################################"
 echo -e          "################ MDS/FS Health ( 'ceph fs status' )##################################"
 echo -e          "#####################################################################################\n"
 oc exec -it $toolpod -n $ns --  ceph fs status
 echo -e      "\n\n####################################################################################"
 echo -e          "################ MDS/FS Health ( 'ceph mds stat )' #################################"
 echo -e          "####################################################################################\n"
 oc exec -it $toolpod -n $ns --  ceph mds stat 
 echo -e      "\n\n#####################################################################################"
 echo -e          "################# OSD Health ('ceph osd stat')  #####################################"
 echo -e          "#####################################################################################\n"
 oc exec -it $toolpod -n $ns --  ceph osd stat
 echo
fi

 echo -e      "\n\n#####################################################################################"
 echo -e          "################# Get Ops in flight  ################################################"
 echo -e          "#####################################################################################\n"

for instance in "a" "b" ; do
  pod=`oc get po -l mds=ocs-storagecluster-cephfilesystem-$instance -n $ns -o jsonpath={.items[].metadata.name}`
  echo MDS Pod $pod
  oc exec -i $pod   -n $ns  -c mds -- ceph daemon mds.ocs-storagecluster-cephfilesystem-$instance dump_ops_in_flight | grep flag_point | sort | uniq -c
done


