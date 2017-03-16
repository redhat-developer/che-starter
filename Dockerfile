FROM centos:7

ARG VERSION=1.0-SNAPSHOT

RUN yum update -y && \
    yum install -y \
       java-1.8.0-openjdk java-1.8.0-openjdk-devel git && \
    yum clean all

ENV JAVA_HOME /etc/alternatives/jre

ENV OSO_ADDRESS tsrv.devshift.net:8443
ENV OSO_DOMAIN_NAME tsrv.devshift.net
ENV JBOSS_HOME /opt/jboss/keycloak

RUN mkdir -p $JBOSS_HOME

WORKDIR $JBOSS_HOME

ADD install_certificate.sh /opt/jboss/keycloak/
RUN /opt/jboss/keycloak/install_certificate.sh

EXPOSE 10000

VOLUME /tmp

ADD target/che-starter-$VERSION.jar app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/opt/jboss/keycloak/app.jar"]