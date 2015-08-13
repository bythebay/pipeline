#UsbStick Builder

##Prep the stick or just download dependencies
A quick note:  The paths to everything are relative.  This means you should be inside the 'usbstick' folder when executing any of the scripts.


1. run ```./download-deps.sh```
2. copy the folder usbstick onto usbstick

3. pull the pipeline image from docker hub.  This requires you already have docker setup.

```sh
docker pull bythebay/pipeline
docker save aeab7cca3c8c > pipelinebythebay.tar
```


#Install docker locally using usbstick
If you do not have virtualbox installed already it should be on the usbstick.  Please install that first so you don't need to download it from the internet as bandwidth is limited.

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

If you need the external IP address of your docker-machine to access services on it you can use this command.

```sh
docker-machine ip pipelinebythebay
```

Another example of using it in a script might be.  Since I have a bash alias to open chrome. This opens the spark notebook in crhome.

```sh
alias openchrome='/Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome --kiosk'
openchrome http://$(docker-machine ip pipelinebythebay):39000
```

###Of note

The installer will work offline but will throw an error at the end.

##VPN USERs

There is a known issue with the cisco anyconnect client screwing up the networking in virtualbox.  The workaround that has worked best for me is to use openconnect instead of anyconnect.  There is a brew formula for openconnect.  ``` brew install openconnect ```
openconnect requires that you have the [OSX tap/tun](http://tuntaposx.sourceforge.net/) driver installed beforehand.

[Install openconnect instructions](https://gist.github.com/moklett/3170636)




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