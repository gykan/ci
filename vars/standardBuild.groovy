def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    node {
        stage('Checkout') {
            checkout scm
        }
        withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'artifactory-credentials',
                          usernameVariable: 'ARTUSERNAME', passwordVariable: 'ARTPASSWORD'],
                         [$class: 'UsernamePasswordMultiBinding', credentialsId: 'aws-credentials',
                          usernameVariable: 'AWS_ID', passwordVariable: 'AWS_KEY']]) {
            options = '-PartUsername=$ARTUSERNAME -PartPassword=$ARTPASSWORD -Daws.accessKeyId=$AWS_ID -Daws.secretKey=$AWS_KEY'
            stage('Assemble') {
                sh (config.assemble || './gradlew clean build -x test') + options
            }
            stage('Test') {
                sh (config.assemble || './gradlew test') + options
            }
            stage('Upload') {
                sh config.upload || echo "upload"
            }
            stage('Deploy') {
                sh config.deploy || echo "Deploying..."
            }
        }
    }
}
