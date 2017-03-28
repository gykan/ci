#!groovy

import hudson.triggers.TimerTrigger
import jenkins.model.*

def orgName = System.getenv('ORG_NAME')

if (!orgName) {
    println("ORG_NAME must be defined, skipping seed job creation!")
    return
}

def libRepo = System.getenv('BUILD_LIB_REPO')

if (libRepo == null) {
    libRepo = "ci"
}

def configXml = """<?xml version='1.0' encoding='UTF-8'?>
<jenkins.branch.OrganizationFolder plugin="branch-api@2.0.8">
  <actions/>
  <description></description>
  <properties>
    <org.jenkinsci.plugins.workflow.libs.FolderLibraries plugin="workflow-cps-global-lib@2.7">
      <libraries>
        <org.jenkinsci.plugins.workflow.libs.LibraryConfiguration>
          <name>build-library</name>
          <retriever class="org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever">
            <scm class="org.jenkinsci.plugins.github_branch_source.GitHubSCMSource" plugin="github-branch-source@2.0.4">
              <checkoutCredentialsId>SAME</checkoutCredentialsId>
              <scanCredentialsId>git-credentials</scanCredentialsId>
              <repoOwner>${orgName}</repoOwner>
              <repository>${libRepo}</repository>
              <includes>*</includes>
              <excludes></excludes>
            </scm>
          </retriever>
          <defaultVersion>master</defaultVersion>
          <implicit>false</implicit>
          <allowVersionOverride>true</allowVersionOverride>
        </org.jenkinsci.plugins.workflow.libs.LibraryConfiguration>
      </libraries>
    </org.jenkinsci.plugins.workflow.libs.FolderLibraries>  
    <jenkins.branch.NoTriggerOrganizationFolderProperty>
      <branches>.*</branches>
    </jenkins.branch.NoTriggerOrganizationFolderProperty>
  </properties>
  <orphanedItemStrategy class="com.cloudbees.hudson.plugins.folder.computed.DefaultOrphanedItemStrategy" plugin="cloudbees-folder@6.0.3">
    <pruneDeadBranches>true</pruneDeadBranches>
    <daysToKeep>0</daysToKeep>
    <numToKeep>0</numToKeep>
  </orphanedItemStrategy>
  <triggers>
    <com.cloudbees.hudson.plugins.folder.computed.PeriodicFolderTrigger plugin="cloudbees-folder@6.0.3">
      <spec>*/2 * * * *</spec>
      <interval>1800000</interval>
    </com.cloudbees.hudson.plugins.folder.computed.PeriodicFolderTrigger>
  </triggers>
  <navigators>
    <org.jenkinsci.plugins.github__branch__source.GitHubSCMNavigator plugin="github-branch-source@2.0.4">
      <repoOwner>${orgName}</repoOwner>
      <scanCredentialsId>git-credentials</scanCredentialsId>
      <checkoutCredentialsId>SAME</checkoutCredentialsId>
      <pattern>.*</pattern>
    </org.jenkinsci.plugins.github__branch__source.GitHubSCMNavigator>
  </navigators>
  <projectFactories>
    <org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProjectFactory plugin="workflow-multibranch@2.14"/>
  </projectFactories>
</jenkins.branch.OrganizationFolder>"""

if (!Jenkins.instance.getItem(orgName)) {
    def xmlStream = new ByteArrayInputStream( configXml.getBytes() )
    try {
        def seedJob = Jenkins.instance.createProjectFromXML(orgName, xmlStream)
        seedJob.scheduleBuild(5000, new TimerTrigger.TimerTriggerCause())
    } catch (ex) {
        println "ERROR: ${ex}"
        println configXml.stripIndent()
    }
}