#!/usr/bin/env bash

set -e

GATEKEEPER_ROOT=${GATEKEEPER_ROOT:-$(dirname $(cd $(dirname ${BASH_SOURCE[0]}) && pwd))}

DOCKER_VOLUMES="-v ${GATEKEEPER_ROOT}:/source -v ${GATEKEEPER_ROOT}/build/rpm:/root/rpmbuild"
DOCKER_ARGS="-w=/root/rpmbuild/SPECS"

function debug
{
    tput setaf 7
    echo -n "[ $(date '+%H:%M:%S') ] "
    echo $@
    tput sgr0
}

function info
{
    tput setaf 2
    echo -n "[ $(date '+%H:%M:%S') ] "
    echo $@
    tput sgr0
}

function warn
{
    tput setaf 3
    echo -n "[ $(date '+%H:%M:%S') ] "
    echo $@
    tput sgr0
}

function error
{
    tput setaf 1
    echo -n "[ $(date '+%H:%M:%S') ] "
    echo $@
    tput sgr0
}

info "Using ${GATEKEEPER_ROOT} as GATEKEEPER_ROOT"

debug docker build ${GATEKEEPER_ROOT}/build/rpm
docker build ${GATEKEEPER_ROOT}/build/rpm | tee >(grep "Successfully built " | cut -f 3 -d' ' > DOCKER_IMAGE)

DOCKER_IMAGE=$(cat DOCKER_IMAGE)

info "Using docker image $DOCKER_IMAGE"

info "Cleaning build directories"
find ${GATEKEEPER_ROOT}/build/rpm/BUILD -mindepth 1 -maxdepth 1 -exec rm -rf "{}" \;

info "Running rpmlint"
debug docker run ${DOCKER_VOLUMES} ${DOCKER_ARGS} ${DOCKER_IMAGE} rpmlint gatekeeper-ssl.spec
docker run ${DOCKER_VOLUMES} ${DOCKER_ARGS} ${DOCKER_IMAGE} rpmlint gatekeeper-ssl.spec

info "Running rpmbuild"
debug docker run ${DOCKER_VOLUMES} ${DOCKER_ARGS} ${DOCKER_IMAGE} rpmbuild -ba gatekeeper-ssl.spec
docker run ${DOCKER_VOLUMES} ${DOCKER_ARGS} ${DOCKER_IMAGE} rpmbuild -ba gatekeeper-ssl.spec