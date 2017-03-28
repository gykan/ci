#!groovy

import jenkins.model.*
import hudson.security.*
import org.jenkinsci.plugins.GithubSecurityRealm

def instance = Jenkins.getInstance()

def clientID = System.getenv("AUTH_CLIENT_ID");
def clientSecret = System.getenv("AUTH_CLIENT_SECRET");
if (clientID == null || clientSecret == null) {
    println("AUTH_CLIENT_ID and AUTH_CLIENT_SECRET must both be defined, create admin user instead")
    def hudsonRealm = new HudsonPrivateSecurityRealm(false)
    hudsonRealm.createAccount('admin','admin')
    instance.setSecurityRealm(hudsonRealm)
} else {
    def githubRealm = new GithubSecurityRealm("https://github.com", "https://api.github.com", clientID, clientSecret, "read:org,user:email")
    instance.setSecurityRealm(githubRealm)
}

def strategy = new hudson.security.FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(strategy)

instance.save();

