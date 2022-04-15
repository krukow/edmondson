FROM jupyter/scipy-notebook:lab-3.3.3
USER root
RUN apt-get update && \
    apt-get install -y software-properties-common
RUN wget -qO - https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | apt-key add -
RUN add-apt-repository --yes https://adoptopenjdk.jfrog.io/adoptopenjdk/deb/
RUN apt-get update && \
    apt-get install -y zip unzip emacs && \
    apt-get install -y adoptopenjdk-11-hotspot && \
    apt-get install -y bash curl rlwrap && \
    apt-get clean
RUN wget -qO - https://download.clojure.org/install/linux-install-1.10.3.1087.sh | /bin/bash
RUN echo "jovyan ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/jovyan
