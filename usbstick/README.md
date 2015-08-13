#UsbStick Builder

##Prep the stick

1. run ```./download-deps.sh```
2. copy the folder usbstick onto usbstick

3. pull the pipeline image from docker hub.  This requires you already have docker setup.

```sh
docker pull bythebay/pipeline
docker save bythebay/pipeline > pipelinebythebay.tar
```


#Install docker locally using usbstick
##OSX
Run this pkg 

```sh
osx/DockerToolbox-1.8.0a.pkg
```

After you have installed the docker binaries using the .pkg file you can create the docker-machine you need.

This script will do everything for you.

```sh
./install-pipeline.sh
```

After the script creates a docker-machine called pipelinebythebay with 5gb of ram allocated you will need to setup your shell environment by running 

```sh
eval "$(docker-machine env pipelinebythebay)"
```


###Of note

The installer will work offline but will throw an error at the end.


##Windows

Run this exe

```sh
windows/DockerToolbox-1.8.0a.exe
```

## Linux

If you are running a newer version of ubuntu the install is as simple as. This also assumes you have curl.


```sh
curl -sSL https://get.docker.com/ | sh
```

#Install using the ova method.

```sh
VBoxManage import bythebay-pipeline.ova
```