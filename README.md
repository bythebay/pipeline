# pipeline

[![Join the chat at https://gitter.im/bythebay/pipeline](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/bythebay/pipeline?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
Complete Pipeline Training at Big Data Scala By the Bay

## Pipeline Description

Dating ratings data => Akka app => Kafka => Spark Streaming => Cassandra => Dashboard

In addition, Spark MLLib, DataFrames will be demonstrated using a combination of the Cassandra real time data plus static Parquet data, on a notebook interface.

## Setup

You need to be able to run Docker containers, which probably means a virtual machine (unless you are running Linux) with at least 5GB, preferably 8GB of RAM in it, and need to be configured for multiple cores.  If you already have Docker, skip to the third method.

### Method 1: Install using Docker Toolbox

* Follow [this README](usbstick/README.md) in the `usbstick/` folder.  It will download all of the things needed to install Docker, Docker Toolbox, and get your environment up and running on OSX.

### Method 2: Install from OVA

* Grab a USB stick, with the bythebay-pipeline.ova file on it
* Make sure you have VirtualBox, VMware Player or VMware Fusion installed (Fusion is for OSX/Macs)
* Import the .ova as an appliance, accept the defaults, then start the VM
* Login as ubuntu/reverse
* Run `./start-bythebay.sh`, which will start and log you into the Docker container with 5GB of RAM

### Method 3: You already have Docker, or are running Linux

* Pull the pipeline image 
```
docker pull bythebay/pipeline
```
* Verify image is downloaded 
```
docker images
```

* Start the image:
```
docker run -it -m 5g -v ~/pipeline/notebooks:/root/pipeline/notebooks -p 30080:80 -p 34042:4042 -p 39160:9160 -p 39042:9042 -p 39200:9200 -p 37077:7077 -p 36060:6060 -p 36061:6061 -p 32181:2181 -p 38090:8090 -p 30000:10000 -p 30070:50070 -p 30090:50090 -p 39092:9092 -p 36066:6066 -p 39000:9000 -p 39999:19999 -p 36081:6081 -p 35601:5601 -p 37979:7979 -p 38989:8989 -p 34040:4040 bythebay/pipeline bash
```

### Once inside the bythebay container

* Inside of Docker, run the following commands:
```
cd ~/pipeline
```
Source the ./bythebay-setup.sh file
```
. ./bythebay-setup.sh
```
^ <-- Don't forget the `.`

## Running the Rating Simulator

### Start Spark Streaming
```
./bythebay-streaming.sh
```

### Start the Ratings Feeder
```
./bythebay-feed.sh
```

## Building a new Docker Image

This should only be done by committers:
```
docker build -t bythebay/pipeline .
```
