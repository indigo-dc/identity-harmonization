#!/bin/bash

mvn clean package

mkdir -p debian/usr/lib/identity-harmonization
cp target/identity-harmonization-0.0.1.jar debian/usr/lib/identity-harmonization

dpkg --build debian

mv debian.deb identity-harmonization-1.0.deb
