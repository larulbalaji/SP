#!/bin/bash
cat StandardDemo-BundleImport.csv|grep "^Add Role,business"|gawk -F, '{print $3}' > BusRoleList.csv
cat StandardDemo-BundleImport.csv|grep "^Add Role,it"|gawk -F, '{print $3}' > ITRoleList.csv

