#!/bin/bash

VERSION=2.0

mvn clean package

mkdir -p debian/var/lib/identity-harmonization/config/
cp config/application.yml debian/var/lib/identity-harmonization/config
cp target/identity-harmonization-$VERSION.jar debian/var/lib/identity-harmonization

dpkg --build debian

mv debian.deb identity-harmonization-$VERSION.deb
