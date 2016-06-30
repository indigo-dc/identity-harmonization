#!/bin/bash

VERSION=0.0.1
TOPDIR=`pwd`/rpm

mvn clean package

cp target/identity-harmonization-$VERSION.jar $TOPDIR/SOURCES
cp config/application.yml $TOPDIR/SOURCES

rpmbuild --define "_topdir ${TOPDIR}" -ba $TOPDIR/SPECS/identity-harmonization.spec

cp ${TOPDIR}/RPMS/x86_64/identity-harmonization-0.0.1-1.x86_64.rpm .
#cp ${TOPDIR}/RPMS/x86_64/identity-harmonization-$VERSION-1.el7.centos.x86_64.rpm .
