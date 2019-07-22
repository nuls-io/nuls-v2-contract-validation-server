FROM insideo/centos7-java8-build

MAINTAINER PierreLuo

WORKDIR /root

ENV ANT_HOME /opt/apache-ant-1.10.6

RUN yum -y install wget && \
    cd /tmp && \
    wget http://mirrors.tuna.tsinghua.edu.cn/apache//ant/binaries/apache-ant-1.10.6-bin.tar.gz && \
    tar zxf /tmp/apache-ant-1.10.6-bin.tar.gz -C /opt/ && \
    rm -f /tmp/*.gz

ENV PATH ${PATH}:${ANT_HOME}/bin

EXPOSE 15151

