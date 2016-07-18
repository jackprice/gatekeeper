%define __jar_repack %{nil}

Name:                  gatekeeper-ssl
Version:               0.1.0
Release:               3.alpha
License:               ASL 2.0
URL:                   https://github.com/jackprice/gatekeeper
Summary:               A highly-available, distributed and secure SSL-termination manager

BuildArch:             noarch
BuildRequires:         java-devel >= 1:1.8.0, maven-local, systemd, systemd-units

Requires(pre):         shadow-utils
Requires:              java-headless >= 1:1.8.0

%description
Gatekeeper is a highly-available, distributed and secure SSL-termination manager.

%pre
getent group gatekeeper >/dev/null || groupadd -r gatekeeper
getent passwd gatekeeper >/dev/null || \
    useradd -r -g gatekeeper -d /opt/gatekeeper -s /sbin/nologin \
    -c "Gatekeeper service account" gatekeeper

%prep
mkdir -p ./src
cp -R /source/src/* ./src/
cp /source/pom.xml .

%build
mvn clean compile assembly:single

%install
cp -R /source/build/data/common/* $RPM_BUILD_ROOT
cp -R /source/build/data/systemd/* $RPM_BUILD_ROOT
cp target/gatekeeper-0.1.0-alpha-jar-with-dependencies.jar $RPM_BUILD_ROOT/opt/gatekeeper/java/gatekeeper.jar

%post
%systemd_post gatekeeper.service

%preun
%systemd_preun gatekeeper.service

%postun
%systemd_postun_with_restart gatekeeper.service

%files
/opt/gatekeeper/bin/gatekeeper
/opt/gatekeeper/java/gatekeeper.jar
%{_unitdir}/gatekeeper.service

%config /etc/gatekeeper/gatekeeper.yml

%dir /etc/gatekeeper
%dir /opt/gatekeeper/bin
%dir /opt/gatekeeper/java