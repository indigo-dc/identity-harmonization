%define __jar_repack 	%{nil}
%define _tmppath	%{_topdir}/tmp
%define buildroot	%{_topdir}/hello-rpm-root

Name:		identity-harmonization	
Version:	1.0
Release:	1%{?dist}
Summary:	Identity-Harmonization Service.

Group:		Applications/Web
License:	apache2
URL:		https://github.com/indigo-dc/identity-harmonization

Requires:	jre >= 1.8

%description
Identity-Harmonization Service.
Standalone Spring Boot application version.

%prep

%build

%install
mkdir -p %{buildroot}/usr/local/bin
mkdir -p %{buildroot}/usr/lib/identity-harmonization
cp %{_topdir}/SOURCES/identity-harmonization %{buildroot}/usr/local/bin/identity-harmonization
cp %{_topdir}/SOURCES/identity-harmonization-0.0.1.jar %{buildroot}/usr/lib/identity-harmonization

%files
/usr/local/bin/identity-harmonization
/usr/lib/identity-harmonization/identity-harmonization-0.0.1.jar

%changelog

