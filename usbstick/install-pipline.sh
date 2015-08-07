#!/bin/bash
# This script will install the docker binaries for working on OSX as well
# as creating a docker machine and importing the tar docker image
# Author Benjamin Rizkowsky benoahriz@gmail.com 

  md5_checker()
{
  if builtin command -v md5 > /dev/null; then
    if [[ $# -eq 1 ]]; then
      if [ "$(md5 -q $1)" == "$(awk '{print $1}' $1.md5)" ]; then
        echo "$1 Hash matches great! route 1"
        echo "$(md5 -q $1) == $(awk '{print $1}' $1.md5)"
      else 
        echo "Hash does not match :("
        exit 1
      fi
    elif  [[ $# -eq 2 ]]; then
      if [ "$(md5 -q $1)" == "$(awk '{print $1}' $2)" ]; then
        echo "$1 Hash matches great! route 2"
        echo "$(md5 -q $1) == $(awk '{print $1}' $2)"
      else 
        echo "Hash does not match :("
        exit 1
      fi
     elif  [[ $# -gt 2 ]]; then 
        echo "uh oh! too many arguments! route 3"
     fi 
  else
    echo "md5 not found on path"
    return 1
  fi
    return 0
}

ask_yes_or_no() {
    read -p "$1 ([y]es or [N]o): "
    case $(echo $REPLY | tr '[A-Z]' '[a-z]') in
        y|yes) echo "yes" ;;
        *)     echo "no" ;;
    esac
}

if [[ "no" == $(ask_yes_or_no "This will overwrite your current docker binaries with known to work with versiosn. Are you sure?") ]]
then
    echo "Skipped."
    exit 0
fi

OSXDIR="osx"
IMAGEHASH="0875d176e1c6"

if [[ $(vboxmanage --version) ]]; then
	echo "looks like you have a version of virtualbox installed already."
else 
    echo "you might want to install virtualbox first the DMG is included on this usbstick"
    ls -al ${OSXDIR}/*.dmg
    exit 1
fi	

echo "Virtualbox version is $(vboxmanage --version)"
echo "If you are having problems with virtualbox you may need to uninstall or upgrade."
echo "See this script  if needed VirtualBox_Uninstall.tool.sh"

echo "version of docker-machine should be at least version 0.3.1"
if [ -f /usr/local/bin/docker-machine ]; then
  if [[ $(md5_checker "/usr/local/bin/docker-machine" "${OSXDIR}/docker-machine_darwin-amd64.md5") ]]; then
    echo "Looks like you already have the right version of docker-machine installed :) Moving along."
    else
      echo "copying binary into place /usr/local/bin/docker-machine"
      cat ${OSXDIR}/docker-machine_darwin-amd64 > /usr/local/bin/docker-machine
  fi
else
  echo "copying binary into place /usr/local/bin/docker-machine"
  cat ${OSXDIR}/docker-machine_darwin-amd64 > /usr/local/bin/docker-machine
fi 

echo "making docker-machine executable"
chmod +x /usr/local/bin/docker-machine

echo "check the version of docker-machine"
echo "#########################################"
echo "#########################################"
echo "#########################################"
echo "#########################################"
docker-machine -v || echo "somethings wrong"

echo "install the latest docker client binary for osx Docker version 1.7.1, build 786b29d"
if [ -f /usr/local/bin/docker ]; then
  if [[ $(md5_checker "/usr/local/bin/docker" "${OSXDIR}/docker-1.7.1.md5") ]]; then
    echo "Looks like you already have the right version of docker installed :) Moving along."
    else
      echo "copying binary into place /usr/local/bin/docker"
      cat ${OSXDIR}/docker-1.7.1 > /usr/local/bin/docker
  fi
else
  echo "copying binary into place /usr/local/bin/docker"
  cat ${OSXDIR}/docker-1.7.1 > /usr/local/bin/docker
fi 

echo "making docker executable"
chmod +x /usr/local/bin/docker
echo "#########################################"
echo "#########################################"
echo "#########################################"
echo "#########################################"
docker -v || echo "somethings wrong"

echo "install the latest docker-compose binary"
cat ${OSXDIR}/docker-compose-Darwin-x86_64 > /usr/local/bin/docker-compose

echo "making docker-compose executable"
chmod +x /usr/local/bin/docker-compose
echo "#########################################"
echo "#########################################"
echo "#########################################"
echo "#########################################"
docker-compose -v || echo "somethings wrong"

echo "#########################################"
echo "#########################################"
echo "#########################################"
echo "#########################################"
echo "create a docker machine for the pipeline"
echo "you need to have at least 8096mb of ram available for your vm"

VIRTUALBOX_BOOT2DOCKER_URL=file:///$(pwd)/${OSXDIR}/boot2docker.iso
export VIRTUALBOX_BOOT2DOCKER_URL
echo "VIRTUALBOX_BOOT2DOCKER_URL = ${VIRTUALBOX_BOOT2DOCKER_URL}"

VIRTUALBOX_MEMORY_SIZE="8192"
export VIRTUALBOX_MEMORY_SIZE
echo "VIRTUALBOX_MEMORY_SIZE = ${VIRTUALBOX_MEMORY_SIZE}"


docker-machine create -d virtualbox pipelinebythebay 2>/dev/null|| echo "Sweet! you already have a pipelinebythebay docker-machine!"
eval $(docker-machine env pipelinebythebay)
# docker-machine env pipelinebythebay

 
if [[ $(docker images |grep ${IMAGEHASH}) ]]; then
	echo "already have the image ${IMAGEHASH}"
else
	echo "docker load < pipelinebythebay.tar"
	docker load < pipelinebythebay.tar
fi

docker images |grep ${IMAGEHASH} && echo "looks like you successfully loaded the docker image :)"

echo "If you need to delete your existing docker machine"
echo "docker-machine stop pipelinebythebay && docker-machine rm pipelinebythebay"

echo "To run this image type the following"
echo "docker run -it 0875d176e1c6 /bin/bash"
docker tag -f ${IMAGEHASH} bythebay/pipeline
echo "alternatively docker run -it bythebay/pipeline /bin/bash"

docker-machine ls |grep pipelinebythebay
echo "#########################################"
echo "#########################################"
echo "#########################################"
echo "#########################################"
echo "the ip address of your virtual machine is $(docker-machine ip pipelinebythebay) this is handy if you have services running on it that you need to access."

echo "#########################################"
echo "#########################################"
echo "#########################################"
echo "#########################################"
echo "if you want to copy prepare a usbstick, this is only what I use on my mac YMMV"
echo "rsync -r --delete --progress ../ /volumes/KINGSTON"

echo "#########################################"
echo "#########################################"
echo "#########################################"
echo "#########################################"
echo "execute this after you run this script since you need the DOCKER_HOST vars to persist."
echo "eval \$(docker-machine env pipelinebythebay)"
