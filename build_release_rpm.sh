#!/bin/bash

TOPDIR=`pwd`/rpm

mvn clean package

cp target/identity-harmonization-0.0.1.jar $TOPDIR/SOURCES

rpmbuild --define "_topdir ${TOPDIR}" -ba $TOPDIR/SPECS/identity-harmonization.spec

cp ${TOPDIR}/RPMS/x86_64/identity-harmonization-1.0-1.el7.centos.x86_64.rpm .
