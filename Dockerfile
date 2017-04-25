FROM jenkins:2.32.3

RUN /usr/local/bin/install-plugins.sh \
    docker:0.16.2 \
    github-oauth:0.26 \
    github-organization-folder:1.6 \
    kubernetes:0.11 \
    token-macro:2.1 \
    workflow-aggregator:2.5

COPY groovy/*.groovy /usr/share/jenkins/ref/init.groovy.d/

ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false
ENV K8S_URL http://kubernetes.default.svc.cluster.local
ENV K8S_NAMESPACE default
