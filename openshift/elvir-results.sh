#!/bin/bash


for pod in $(oc get pods |grep -v NAME |awk '{print $1}'); do 
	  echo "running stuff in $pod" 
	  oc exec $pod -- bash -c "wc -l /home/jboss/logs/$pod/*"; echo $m
done
