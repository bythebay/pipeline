#!/bin/bash
# This script will download the docker binaries for working on OSX 
# Author Benjamin Rizkowsky benoahriz@gmail.com 


OSXDIR="osx"
if [ -d ${OSXDIR} ]; then
  echo "dir already exists"
else 
  echo "creating directory ${OSXDIR}"
  mkdir ${OSXDIR}
fi

# docker-machine
curl -L "https://github.com/docker/machine/releases/download/v0.3.1/docker-machine_darwin-amd64" > ${OSXDIR}/docker-machine_darwin-amd64
curl -L "https://github.com/docker/machine/releases/download/v0.3.1/docker-machine_darwin-amd64.md5" > ${OSXDIR}/docker-machine_darwin-amd64.md5

#Boot2Docker
curl -L "https://github.com/boot2docker/boot2docker/releases/download/v1.7.1/boot2docker.iso" > ${OSXDIR}/boot2docker.iso


# docker-compose 
curl -L "https://github.com/docker/compose/releases/download/1.3.3/docker-compose-$(uname -s)-$(uname -m)" > ${OSXDIR}/docker-compose-Darwin-x86_64
md5 -r ${OSXDIR}/docker-compose-Darwin-x86_64 > ${OSXDIR}/docker-compose-Darwin-x86_64.md5

# docker binary 
curl -L "https://get.docker.com/builds/Darwin/x86_64/docker-1.7.1" > ${OSXDIR}/docker-1.7.1
md5 -r ${OSXDIR}/docker-1.7.1 > ${OSXDIR}/docker-1.7.1.md5

# kitematic
curl -L "https://github.com/kitematic/kitematic/releases/download/v0.8.0-rc4/Kitematic-0.8.0-rc4-Mac.zip" > ${OSXDIR}/Kitematic-0.8.0-rc4-Mac.zip

# Virtualbox
curl -L "http://download.virtualbox.org/virtualbox/5.0.0/VirtualBox-5.0.0-101573-OSX.dmg" > ${OSXDIR}/VirtualBox-5.0.0-101573-OSX.dmg
curl -L "http://download.virtualbox.org/virtualbox/5.0.0/Oracle_VM_VirtualBox_Extension_Pack-5.0.0-101573.vbox-extpack" > ${OSXDIR}/Oracle_VM_VirtualBox_Extension_Pack-5.0.0-101573.vbox-extpack
curl -L "https://www.virtualbox.org/download/hashes/5.0.0/MD5SUMS" > ${OSXDIR}/MD5SUMS.virtualbox

echo "Showing the md5 sums of the files"
find ${OSXDIR} -type f -exec md5 -r {} \;

echo "assuming you already have a docker-machine to save out the pipeline image for offlines usage"
echo "you will need to run docker pull bythebay/pipeline"
echo "then you need to run docker save bythebay/pipeline > pipelinebythebay.tar"






