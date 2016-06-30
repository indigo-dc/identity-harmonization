%define __jar_repack 	%{nil}
%define _tmppath	%{_topdir}/tmp
%define buildroot	%{_topdir}/build-rpm-root

%define name		identity-harmonization
%define jarversion	0.0.1

Name:		%{name}
Version:	0.0.1
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
mkdir -p %{buildroot}/var/lib/%{name}/config
mkdir -p %{buildroot}/etc/systemd/system
cp %{_topdir}/SOURCES/application.yml %{buildroot}/var/lib/%{name}/config
cp %{_topdir}/SOURCES/%{name}-%{jarversion}.jar %{buildroot}/var/lib/%{name}
cp %{_topdir}/SOURCES/%{name}.service %{buildroot}/etc/systemd/system

%files
/var/lib/%{name}/config/application.yml
/var/lib/%{name}/%{name}-%{jarversion}.jar
/etc/systemd/system/%{name}.service

%changelog

%post
/usr/bin/id -u idh > /dev/null 2>&1
if [ $? -eq 1 ]; then
  adduser --system --user-group idh
fi

if [ -f /var/lib/%{name}/%{name}-%{jarversion}.jar ]; then
  chmod +x /var/lib/%{name}/%{name}-%{jarversion}.jar
fi

chown -R idh:idh /var/lib/%{name}

systemctl start %{name}.service
systemctl enable %{name}.service
