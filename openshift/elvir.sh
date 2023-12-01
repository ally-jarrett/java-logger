#!/bin/bash

for pod in $(oc get pods |grep -v NAME |awk '{print $1}'); do 
	  oc exec $pod -- bash -c "date; curl -X POST 'http://localhost:8080/logs/generate?runtime=120&polltime=5'"
done
