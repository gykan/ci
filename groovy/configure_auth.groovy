#!groovy

import jenkins.model.*
import hudson.security.*
import org.jenkinsci.plugins.GithubSecurityRealm
import org.jenkinsci.plugins.GithubAuthorizationStrategy

def instance = Jenkins.getInstance()

def clientID = System.getenv("AUTH_CLIENT_ID");
def clientSecret = System.getenv("AUTH_CLIENT_SECRET");
def orgName = System.getenv("ORG_NAME")

if (clientID == null || clientSecret == null) {
    println("AUTH_CLIENT_ID and AUTH_CLIENT_SECRET must both be defined, create admin user instead")
    def hudsonRealm = new HudsonPrivateSecurityRealm(false)
    hudsonRealm.createAccount('admin','admin')
    instance.setSecurityRealm(hudsonRealm)
} else {
    def githubRealm = new GithubSecurityRealm("https://github.com", "https://api.github.com", clientID, clientSecret, "read:org,user:email")
    instance.setSecurityRealm(githubRealm)
}

def strategy
if (!orgName) {
    println("No Github org defined (ORG_NAME), using FullControlOnceLoggedIn auth strategy")
    strategy = new hudson.security.FullControlOnceLoggedInAuthorizationStrategy()
    strategy.setAllowAnonymousRead(false)
} else {
    strategy = new GithubAuthorizationStrategy(
            'nykanong,nykanon', // admin usernames
            true, // authenticatedUserReadPermission
            true, // useRepositoryPermissions
            false, // authenticatedUserCreateJobPermission
            orgName, // organizationNames
            true, // allowGithubWebHookPermission
            false, // allowCcTrayPermission
            false, // allowAnonymousReadPermission
            false // allowAnonymousJobStatusPermission
    )
}

instance.setAuthorizationStrategy(strategy)

instance.save();

