#UsbStick Builder

##NOTE: If you are a trainee, go to the [Start Docker wiki page](https://github.com/bythebay/pipeline/wiki/Start-Docker).

These instructions are just for how to build USB sticks for the training, as well as additional info.

##Prep the stick or just download dependencies
A quick note:  The paths to everything are relative.  This means you should be inside the 'usbstick' folder when executing any of the scripts.


1. run ```./download-deps.sh```
2. copy the folder usbstick onto usbstick

3. pull the pipeline image from docker hub.  This requires you already have docker setup.

```sh
docker pull bythebay/pipeline
docker save aeab7cca3c8c > pipelinebythebay.tar
```

^^ Replace the hash above with the actual hash from `docker images`.

## docker-compose

If you choose to use docker-compose you can instantiate the container by running. This assumes you are in the usbstick folder as there is a link to something relative to your pather for the spark notebooks.

```sh
docker-compose run pipelinetraining
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