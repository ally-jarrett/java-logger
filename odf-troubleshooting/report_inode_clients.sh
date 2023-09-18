#!/bin/bash
#for instance in "a" "b"

if [[  -z $INODE ]] ; then
 inode=$1
else
  inode=$INODE
fi
cluster=$(oc config current-context|awk -F'/' '{print $2}'| awk -F '-' '{print $2"-"$3}')


for instance in "a" "b"
do
  pod=`oc get po -l mds=ocs-storagecluster-cephfilesystem-$instance -o jsonpath={.items[].metadata.name} -n openshift-storage`
  
  oc exec -i -c mds -n openshift-storage $pod  -- ceph daemon mds.ocs-storagecluster-cephfilesystem-$instance dump inode $inode | jq '"inode: \(.ino) ,\(.ctime), filePath: \(.path), versionlock: \(.versionlock.state) , authlock: \(.authlock.state) , linklock: \(.linklock.state) , filelock: \(.filelock.state) , xattrlock: \(.xattrlock.state) , snaplock: \(.snaplock.state) , nestlock: \(.nestlock.state) , flocklock: \(.flocklock.state ) , policylock: \(.policylock.state) , client_id: \(.client_caps[].client_id)" ' -r

  echo -e "\n\n"

done




