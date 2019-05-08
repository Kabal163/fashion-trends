#Step 1 - compile from sources and create jar
FROM openjdk:8-jdk-alpine3.7 AS builder
MAINTAINER Dmitrii Kanaev <Dmitrii_Kanaev@epam.com>

COPY . /usr/src/fashion-trends
WORKDIR /usr/src/myDocker
RUN apk add --no-cache maven && mvn --version
RUN mvn package

#Step 2 - get jar from step 1 + install ssh server
FROM openjdk:8-jre-alpine3.7

WORKDIR /usr/app/fashion-trends
COPY --from=builder /usr/src/fashion-trends/target/fashion-trends-0.0.1.jar .
COPY --from=builder /usr/src/fashion-trends/src/main/resources/rules.properties .
COPY --from=builder /usr/src/fashion-trends/src/main/resources/app/fashion-trends-start.sh /usr/local/bin/
RUN apk update && apk add --no-cache \
    nano \
    rsync \
    curl \
    openssh \
    bash \
    openrc
RUN rc-update add sshd
RUN mkdir /var/run/sshd
RUN echo 'root:screencast' | chpasswd
RUN echo sshd_enable="YES" >> /etc/rc.conf
RUN ssh-keygen -A
RUN echo PermitRootLogin yes >> /etc/ssh/sshd_config


ENTRYPOINT ["sh", "/usr/local/bin/fashion-trends-start.sh", "/usr/app/fashion-trends/fashion-trends-0.0.1.jar"]
