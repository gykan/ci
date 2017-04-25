def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    withCredentials(
            [
                    [
                            $class          : 'UsernamePasswordMultiBinding',
                            credentialsId   : 'artifactory-credentials',
                            usernameVariable: 'ARTUSERNAME',
                            passwordVariable: 'ARTPASSWORD'
                    ],
                    [
                            $class          : 'UsernamePasswordMultiBinding',
                            credentialsId   : 'aws-credentials',
                            usernameVariable: 'AWS_ACCESS_KEY',
                            passwordVariable: 'AWS_SECRET_ACCESS_KEY'
                    ]
            ]
    ) {
//Lets define a unique label for this build.
        def label = "${env.JOB_NAME}.${env.BUILD_NUMBER}".replace('-', '_').replace('/', '_')
        podTemplate(
                label: label,
                containers: [
                        containerTemplate(
                                name: 'gradle',
                                image: 'frekele/gradle:latest',
                                ttyEnabled: true,
                                command: 'cat',
                                envVars: [
                                        containerEnvVar(key: "ARTUSERNAME", value: env.ARTUSERNAME),
                                        containerEnvVar(key: "ARTPASSWORD", value: env.ARTPASSWORD),
                                        containerEnvVar(key: "AWS_ACCESS_KEY", value: env.AWS_ACCESS_KEY),
                                        containerEnvVar(key: "AWS_SECRET_ACCESS_KEY", value: env.AWS_SECRET_ACCESS_KEY)
                                ]
                        ),
                        containerTemplate(
                                name: 'docker',
                                image: 'docker:latest',
                                ttyEnabled: true,
                                command: 'cat'
                        ),
                        containerTemplate(
                                name: 'jnlp',
                                image: 'jenkinsci/jnlp-slave:2.62-alpine',
                                args: '${computer.jnlpmac} ${computer.name}'
                        )
                ],
                volumes: [
                        emptyDirVolume(
                                mountPath: '/home/jenkins',
                                memory: false
                        ),
                        hostPathVolume(
                                mountPath: '/var/run/docker.sock',
                                hostPath: '/var/run/docker.sock'
                        )
                ]
        ) {
            //Lets use pod template (refernce by label)
            node() {
                checkout scm
                def options = ' -PartUsername=$ARTUSERNAME -PartPassword=$ARTPASSWORD -Daws.accessKeyId=$AWS_ACCESS_KEY -Daws.secretKey=$AWS_SECRET_ACCESS_KEY '
                def v = getGradleProperty("version")
                def serviceName = getGradleProperty("description")
                def env = env.BRANCH_NAME == 'development' ? "rc" : "dev"

                stage ('Assemble') {
//                    container('gradle') {
                        if (config.assemble != null) {
                            if (config.assemble) {
                                sh(config.assemble + options)
                            }
                        } else {
                            sh "./gradlew $options clean build -x test"
                        }
//                    }
                }

                stage ('Test') {
//                    container('gradle') {
                        if (config.test != null) {
                            if (config.test) {
                                sh(config.test + options)
                            }
                        } else {
                            sh "./gradlew $options test"
                        }
//                    }
                }

                stage ('Regression') {
//                    container('gradle') {
                        if (config.regression != null) {
                            if (config.regression) {
                                sh(config.regression + options)
                            }
                        } else {
                            sh "./gradlew $options regression"
                        }
//                    }
                }

                stage ('Upload rc') {
//                    container('gradle') {
                        if (config.upload != null) {
                            if (config.upload) {
                                sh(config.upload + " -Denv=" + env + " " + options)
                            }
                        } else {
                            sh "./gradlew $options -Denv=${env} s3Upload"
                        }
//                    }
                }

                stage('Docker') {
                    if (config.docker != null && config.docker) {
                        docker.withRegistry(
                                'https://index.docker.io/v1/',
                                'docker-hub-credentials'
                        )
                                {
                                    def imageName = "nykanon/gykan:${serviceName}-latest"
                                    sh "docker build -t ${imageName} ."
                                    image = docker.image(imageName)
                                    image.push()
                                }
                    } else {
                        echo "Skip Docker"
                    }
                }

                stage('Deploy') {
                    if (config.deploy != null) {
                        if (config.deploy) {
                            sh(config.deploy + " -Denv=" + env + " " + options)
                        }
                    } else {
                        echo "Deploying..."
                    }
                }
            }
        }
    }
}

def getGradleProperty(name) {
    def matcher = readFile('gradle.properties') =~ "${name}=(.+)"
    matcher ? matcher[0][1] : null
}
