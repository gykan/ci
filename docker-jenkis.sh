#!/bin/bash

docker run -p 8080:8080 -p 5000:5000 -v /tmp/jenkins-home:/var/jenkins_home \
        -e ORG_NAME='<GITHUB ORG NAME>' \
        -e AUTH_CLIENT_ID='<GITHUB AUTH ID: https://wiki.jenkins-ci.org/display/JENKINS/Github+OAuth+Plugin>' \
        -e AUTH_CLIENT_SECRET='<GITHUB AUTH SECRET>' \
        -e GIT_USER_NAME='<GITHUB USERNAME>' \
        -e GIT_USER_KEY='<GITHUB USERKEY>' \
        -e ARTIFACTORY_USER_NAME='<ARTIFACTORY USERNAME>' \
        -e ARTIFACTORY_USER_KEY='<ARTIFACTORY USERKEY>' \
        -e AWS_CLIENT_ID='<AWS ID>' \
        -e AWS_CLIENT_SECRET='<AWS SECRET>' \
        github-org-jenkins-docker
