FROM jupyter/scipy-notebook:lab-3.4.7
USER root
RUN apt-get update && \
    apt-get install -y software-properties-common
RUN apt install -y wget apt-transport-https
RUN mkdir -p /etc/apt/keyrings && \
    wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | tee /etc/apt/keyrings/adoptium.asc
RUN echo "deb [signed-by=/etc/apt/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list
RUN apt-get update && \
    apt-get install -y zip unzip emacs && \
    apt install -y temurin-17-jdk && \
    apt-get install -y bash curl rlwrap && \
    apt-get clean
RUN wget -qO - https://download.clojure.org/install/linux-install-1.11.1.1155.sh | /bin/bash
RUN echo "jovyan ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/jovyan