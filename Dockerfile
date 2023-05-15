FROM insideo/centos7-java8-build

MAINTAINER PierreLuo

ENV ANT_HOME /opt/apache-ant-1.10.12

RUN yum -y install wget && \
    cd /tmp && \
    wget --no-check-certificate https://mirrors.tuna.tsinghua.edu.cn/apache/ant/binaries/apache-ant-1.10.12-bin.tar.gz --no-check-certificate&& \
    tar zxf /tmp/apache-ant-1.10.12-bin.tar.gz -C /opt/ && \
    rm -f /tmp/*.gz

WORKDIR /root

ENV PATH ${PATH}:${ANT_HOME}/bin

EXPOSE 15151

