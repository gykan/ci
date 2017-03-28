FROM jenkins:2.32.3

RUN /usr/local/bin/install-plugins.sh \
    docker:0.16.2 \
    token-macro:latest git:3.1.0 \
    workflow-aggregator:2.5 \
    github-oauth:0.25 \
    github-organization-folder:1.6

COPY groovy/*.groovy /usr/share/jenkins/ref/init.groovy.d/

ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false