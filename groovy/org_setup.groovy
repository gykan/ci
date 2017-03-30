#!groovy

import jenkins.branch.OrganizationFolder
import jenkins.model.*
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource
import org.jenkinsci.plugins.workflow.libs.FolderLibraries
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration
import org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever

def orgName = System.getenv('ORG_NAME')

if (!orgName) {
    println("ORG_NAME must be defined, skipping seed job creation!")
    return
}

def libRepo = System.getenv('BUILD_LIB_REPO')

if (libRepo == null) {
    libRepo = "ci"
}

if (!Jenkins.instance.getItem(orgName)) {
    try {

        def job = Jenkins.instance.createProject(OrganizationFolder.class, orgName)

        def navigator = new GitHubSCMNavigator("", orgName, "git-credentials", GitHubSCMNavigator.DescriptorImpl.SAME);
        job.getNavigators().replace(navigator)

        def scm = new GitHubSCMSource("", "", GitHubSCMSource.DescriptorImpl.SAME, "git-credentials", orgName, libRepo)
        def lib = new LibraryConfiguration("build-library", new SCMSourceRetriever(scm))
        lib.setDefaultVersion("master")
        job.addProperty(new FolderLibraries([lib]))

        job.scheduleBuild(10)
    } catch (ex) {
        println "ERROR: ${ex}"
    }
}