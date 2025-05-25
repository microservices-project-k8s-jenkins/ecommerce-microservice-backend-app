pipeline {
    agent any
    options {
        skipDefaultCheckout(true)
    }
    environment {
        TAG = "${env.BUILD_NUMBER}"
        IS_PR = "${env.CHANGE_ID != null ? 'true' : 'false'}"
        IS_DEV_PR = "${env.CHANGE_ID != null && env.CHANGE_TARGET == 'dev' ? 'true' : 'false'}"
    }
    stages {
        stage('Checkout') {
            steps {
                cleanWs()
                checkout scm
            }
        }
        stage('Clone Charts') {
            steps {
                sh 'git clone https://github.com/microservices-project-k8s-jenkins/ecommerce-chart.git'
            }
        }
        stage('Docker Login') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub', passwordVariable: 'DOCKERHUB_PASS', usernameVariable: 'DOCKERHUB_USER')]) {
                    sh 'echo $DOCKERHUB_PASS | docker login -u $DOCKERHUB_USER --password-stdin'
                    script {
                        env.DOCKERHUB_USER = "${DOCKERHUB_USER}"
                    }
                }
            }
        }

        stage('Build and Test Microservices') {
            steps {
                sh './mvnw clean package'
            }
        }

        stage('Push Images to Docker Hub') {
            parallel {
                stage('Order Service') {
                    steps {
                        script {
                            buildAndPushDockerImage 'order-service'
                        }
                    }
                }
                stage('Product Service') {
                    steps {
                        script {
                            buildAndPushDockerImage 'product-service'
                        }
                    }
                }
                stage('User Service') {
                    steps {
                        script {
                            buildAndPushDockerImage 'user-service'
                        }
                    }
                }
                stage('Api Gateway') {
                    steps {
                        script {
                            buildAndPushDockerImage 'api-gateway'
                        }
                    }
                }
                stage('Proxy Client') {
                    steps {
                        script {
                            buildAndPushDockerImage 'proxy-client'
                        }
                    }
                }
                stage('Shipping Service') {
                    steps {
                        script {
                            buildAndPushDockerImage 'shipping-service'
                        }
                    }
                }
                stage('Favourite Service') {
                    steps {
                        script {
                            buildAndPushDockerImage 'favourite-service'
                        }
                    }
                }
                stage('Payment Service') {
                    steps {
                        script {
                            buildAndPushDockerImage 'payment-service'
                        }
                    }
                }
                stage('Cloud Config') {
                    steps {
                        script {
                            buildAndPushDockerImage 'cloud-config'
                        }
                    }
                }
                stage('Service Discovery') {
                    steps {
                        script {
                            buildAndPushDockerImage 'service-discovery'
                        }
                    }
                }
            }
        }
        stage('Configure Kubernetes Access') {
            when {
                expression { env.IS_PR == 'false' }
            }
            steps {
                script {
                    sh '''
                        if [ -f /var/run/secrets/kubernetes.io/serviceaccount/token ]; then
                            echo "Using in-cluster configuration"
                            kubectl config set-cluster kubernetes --server=https://kubernetes.default.svc --certificate-authority=/var/run/secrets/kubernetes.io/serviceaccount/ca.crt
                            kubectl config set-credentials jenkins --token=$(cat /var/run/secrets/kubernetes.io/serviceaccount/token)
                            kubectl config set-context kubernetes --cluster=kubernetes --user=jenkins
                            kubectl config use-context kubernetes
                        else
                            echo "Using external kubeconfig"
                        fi
                    '''
                }
            }
        }
        stage('Deploy') {
            when {
                expression { env.IS_PR == 'false' }
            }
            steps {
                script {
                    def namespace = ''
                    def valuesFile = ''
                    if (env.BRANCH_NAME == 'dev') {
                        namespace = 'dev'
                        valuesFile = 'values-dev.yaml'
                    } else if (env.BRANCH_NAME == 'stage') {
                        namespace = 'stage'
                        valuesFile = 'values-stage.yaml'
                    } else if (env.BRANCH_NAME == 'master') {
                        namespace = 'master'
                        valuesFile = 'values-master.yaml'
                    } else {
                        error "Rama no soportada: ${env.BRANCH_NAME}"
                    }
                    
                    sh "kubectl create namespace ${namespace} --dry-run=client -o yaml | kubectl apply -f -"
                    
                    sh """
                        helm upgrade --install ecommerce-app ecommerce-chart/ecommerce-app \\
                        --namespace ${namespace} \\
                        --set cloud-config.image.tag=${TAG} \\
                        --set service-discovery.image.tag=${TAG} \\
                        --set api-gateway.image.tag=${TAG} \\
                        --set proxy-client.image.tag=${TAG} \\
                        --set order-service.image.tag=${TAG} \\
                        --set payment-service.image.tag=${TAG} \\
                        --set product-service.image.tag=${TAG} \\
                        --set shipping-service.image.tag=${TAG} \\
                        --set user-service.image.tag=${TAG} \\
                        --set favourite-service.image.tag=${TAG} \\
                        -f ecommerce-chart/ecommerce-app/${valuesFile} \\
                        --wait --timeout=10m
                    """
                    env.DEPLOY_NAMESPACE = namespace
                }
            }
        }
        stage('Performance Tests') {
            when {
                expression { env.IS_PR == 'false' }
            }
            steps {
                script {
                    sh 'mkdir -p reports'
                    def namespace = env.DEPLOY_NAMESPACE ?: (env.BRANCH_NAME == 'dev' ? 'dev' : (env.BRANCH_NAME == 'stage' ? 'stage' : (env.BRANCH_NAME == 'prod' ? 'master' : 'dev')))
                    
                    sh "kubectl rollout status deployment/order-service --namespace ${namespace} --timeout=300s"
                    sh "kubectl rollout status deployment/product-service --namespace ${namespace} --timeout=300s"
                    sh "kubectl rollout status deployment/user-service --namespace ${namespace} --timeout=300s"
                    
                    try {
                        dir('order-service') {
                            sh 'locust -f tests/performance/locustfile.py --headless -u 100 -r 10 --run-time 1m --html=../reports/order-service-report.html --csv=../reports/order-service'
                        }
                    } catch (Exception e) {
                        echo "Error en performance test de order-service: ${e.getMessage()}"
                    }
                    
                    try {
                        dir('product-service') {
                            sh 'locust -f tests/performance/locustfile.py --headless -u 100 -r 10 --run-time 1m --html=../reports/product-service-report.html --csv=../reports/product-service'
                        }
                    } catch (Exception e) {
                        echo "Error en performance test de product-service: ${e.getMessage()}"
                    }
                    
                    try {
                        dir('user-service') {
                            sh 'locust -f tests/performance/locustfile.py --headless -u 100 -r 10 --run-time 1m --html=../reports/user-service-report.html --csv=../reports/user-service'
                        }
                    } catch (Exception e) {
                        echo "Error en performance test de user-service: ${e.getMessage()}"
                    }
                }
            }
            post {
                always {
                    script {
                        if (fileExists('reports')) {
                            archiveArtifacts artifacts: 'reports/*.html', fingerprint: true, allowEmptyArchive: true
                            archiveArtifacts artifacts: 'reports/*.csv', fingerprint: true, allowEmptyArchive: true
                            publishHTML([
                                allowMissing: true,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'reports',
                                reportFiles: '*.html',
                                reportName: 'Locust Performance Reports'
                            ])
                        }
                    }
                }
            }
        }
    }
    post {
        always {
            cleanWs()
        }
        failure {
            echo "Pipeline failed. Check the logs for details."
        }
    }
}

void buildAndPushDockerImage(String serviceName) {
    dir(serviceName) {
        script {
            try {
                sh "docker build -t ${DOCKERHUB_USER}/${serviceName}:${TAG} ."
                sh "docker push ${DOCKERHUB_USER}/${serviceName}:${TAG}"

                sh "docker tag ${DOCKERHUB_USER}/${serviceName}:${TAG} ${DOCKERHUB_USER}/${serviceName}:latest"
                sh "docker push ${DOCKERHUB_USER}/${serviceName}:latest"

                echo "Successfully built and pushed ${serviceName}:${TAG} and ${serviceName}:latest"
            } catch (Exception e) {
                echo "Error in ${serviceName}: ${e.getMessage()}"
                currentBuild.result = 'FAILURE'
                throw e
            }
        }
    }
}