#!groovy

import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*

def addPassCredentials(id, loginName, passName) {
  def name_string = System.getenv(loginName);
  def key_string = System.getenv(passName);
    if (name_string == null || key_string == null) {
      println("${loginName} and ${passName} must both be defined, skipping ${id} credentials creation")
      return
    }

    def key = new UsernamePasswordCredentialsImpl(
      CredentialsScope.GLOBAL,
      id,
      "",
      name_string,
      key_string)
    def store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
    store.addCredentials(Domain.global(), key)
}

addPassCredentials('git-credentials', 'GIT_USER_NAME', 'GIT_USER_KEY')

addPassCredentials('artifactory-credentials', 'ARTIFACTORY_USER_NAME', 'ARTIFACTORY_USER_KEY')

addPassCredentials('aws-credentials', 'AWS_CLIENT_ID', 'AWS_CLIENT_SECRET')
