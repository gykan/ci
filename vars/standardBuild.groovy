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

            def options = ' -PartUsername=$ARTUSERNAME -PartPassword=$ARTPASSWORD -Daws.accessKeyId=$AWS_ID -Daws.secretKey=$AWS_KEY '

            stage('Assemble') {
                if (config.assemble != null) {
                    sh (config.assemble + options)
                } else {
                    sh "./gradlew $options clean build -x test"
                }
            }

            stage('Test') {
                if (config.test != null) {
                    sh (config.test + options)
                } else {
                    sh "./gradlew $options test"
                }
            }

            stage('Regression') {
                if (config.regression != null) {
                    sh (config.regression + options)
                } else {
                    sh "./gradlew $options regression"
                }
            }

            stage('Upload') {
                if (config.upload != null) {
                    sh (config.upload + options)
                } else {
                    sh "./gradlew $options -Denv=dev s3Upload; ./gradlew $options -Denv=rc s3Upload"
                }
            }

            stage('Deploy') {
                if (config.deploy != null) sh (config.deploy + options)
                else echo "Deploying..."
            }
        }
    }
}
