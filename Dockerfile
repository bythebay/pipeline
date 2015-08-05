FROM fluxcapacitor/pipeline

ENV SCALA_VERSION=2.10.4

EXPOSE 80 4042 9160 9042 9200 7077 38080 38081 6060 6061 8090 10000 50070 50090 9092 6066 9000 19999 6379 6081 7474 8787 5601 8989 7979 4040

# Override pipeline repo to point at bythebay/pipeline, sync latest
RUN \
 cd ~ \
 && rm -rf pipeline \
 && git clone https://github.com/bythebay/pipeline.git \
 && chmod a+rx pipeline/*.sh

# .profile Shell Environment Variables
 && mv ~/.profile ~/.profile.orig \
 && ln -s ~/pipeline/config/bash/.profile ~/.profile \
