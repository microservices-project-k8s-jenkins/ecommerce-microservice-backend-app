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
                        dir('order-service') {
                            script {
                                try {
                                    sh "docker build -t ${DOCKERHUB_USER}/order-service:${TAG} ."
                                    sh "docker push ${DOCKERHUB_USER}/order-service:${TAG}"
                                } catch (Exception e) {
                                    echo "Error in Order Service: ${e.getMessage()}"
                                    currentBuild.result = 'FAILURE'
                                    throw e
                                }
                            }
                        }
                    }
                }
                stage('Product Service') {
                    steps {
                        dir('product-service') {
                            script {
                                try {
                                    sh "docker build -t ${DOCKERHUB_USER}/product-service:${TAG} ."
                                    sh "docker push ${DOCKERHUB_USER}/product-service:${TAG}"
                                } catch (Exception e) {
                                    echo "Error in Product Service: ${e.getMessage()}"
                                    currentBuild.result = 'FAILURE'
                                    throw e
                                }
                            }
                        }
                    }
                }
                stage('User Service') {
                    steps {
                        dir('user-service') {
                            script {
                                try {
                                    sh "docker build -t ${DOCKERHUB_USER}/user-service:${TAG} ."
                                    sh "docker push ${DOCKERHUB_USER}/user-service:${TAG}"
                                } catch (Exception e) {
                                    echo "Error in User Service: ${e.getMessage()}"
                                    currentBuild.result = 'FAILURE'
                                    throw e
                                }
                            }
                        }
                    }
                }
                stage('Api Gateway') {
                    steps {
                        dir('api-gateway') {
                            script {
                                try {
                                    sh "docker build -t ${DOCKERHUB_USER}/api-gateway:${TAG} ."
                                    sh "docker push ${DOCKERHUB_USER}/api-gateway:${TAG}"
                                } catch (Exception e) {
                                    echo "Error in Api Gateway: ${e.getMessage()}"
                                    currentBuild.result = 'FAILURE'
                                    throw e
                                }
                            }
                        }
                    }
                }
                stage('Proxy Client') {
                    steps {
                        dir('proxy-client') {
                            script {
                                try {
                                    sh "docker build -t ${DOCKERHUB_USER}/proxy-client:${TAG} ."
                                    sh "docker push ${DOCKERHUB_USER}/proxy-client:${TAG}"
                                } catch (Exception e) {
                                    echo "Error in Proxy Client: ${e.getMessage()}"
                                    currentBuild.result = 'FAILURE'
                                    throw e
                                }
                            }
                        }
                    }
                }
                stage('Shipping Service') {
                    steps {
                        dir('shipping-service') {
                            script {
                                try {
                                    sh "docker build -t ${DOCKERHUB_USER}/shipping-service:${TAG} ."
                                    sh "docker push ${DOCKERHUB_USER}/shipping-service:${TAG}"
                                } catch (Exception e) {
                                    echo "Error in Shipping Service: ${e.getMessage()}"
                                    currentBuild.result = 'FAILURE'
                                    throw e
                                }
                            }
                        }
                    }
                }
                stage('Favourite Service') {
                    steps {
                        dir('favourite-service') {
                            script {
                                try {
                                    sh "docker build -t ${DOCKERHUB_USER}/favourite-service:${TAG} ."
                                    sh "docker push ${DOCKERHUB_USER}/favourite-service:${TAG}"
                                } catch (Exception e) {
                                    echo "Error in Favourite Service: ${e.getMessage()}"
                                    currentBuild.result = 'FAILURE'
                                    throw e
                                }
                            }
                        }
                    }
                }
                stage('Payment Service') {
                    steps {
                        dir('payment-service') {
                            script {
                                try {
                                    sh "docker build -t ${DOCKERHUB_USER}/payment-service:${TAG} ."
                                    sh "docker push ${DOCKERHUB_USER}/payment-service:${TAG}"
                                } catch (Exception e) {
                                    echo "Error in Payment Service: ${e.getMessage()}"
                                    currentBuild.result = 'FAILURE'
                                    throw e
                                }
                            }
                        }
                    }
                }
                stage('Cloud Config') {
                    steps {
                        dir('cloud-config') {
                            script {
                                try {
                                    sh "docker build -t ${DOCKERHUB_USER}/cloud-config:${TAG} ."
                                    sh "docker push ${DOCKERHUB_USER}/cloud-config:${TAG}"
                                } catch (Exception e) {
                                    echo "Error in Cloud Config: ${e.getMessage()}"
                                    currentBuild.result = 'FAILURE'
                                    throw e
                                }
                            }
                        }
                    }
                }
                stage('Service Discovery') {
                    steps {
                        dir('service-discovery') {
                            script {
                                try {
                                    sh "docker build -t ${DOCKERHUB_USER}/service-discovery:${TAG} ."
                                    sh "docker push ${DOCKERHUB_USER}/service-discovery:${TAG}"
                                } catch (Exception e) {
                                    echo "Error in Service Discovery: ${e.getMessage()}"
                                    currentBuild.result = 'FAILURE'
                                    throw e
                                }
                            }
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
                    } else if (env.BRANCH_NAME == 'prod') {
                        namespace = 'master'
                        valuesFile = 'values-master.yaml'
                    } else {
                        error "Rama no soportada: ${env.BRANCH_NAME}"
                    }
                    
                    sh "kubectl create namespace ${namespace} --dry-run=client -o yaml | kubectl apply -f -"
                    
                    sh """
                        helm upgrade --install ecommerce-app ecommerce-chart/ecommerce-app \
                        --namespace ${namespace} \
                        --set cloud-config.image.tag=${TAG} \
                        --set service-discovery.image.tag=${TAG} \
                        --set api-gateway.image.tag=${TAG} \
                        --set proxy-client.image.tag=${TAG} \
                        --set order-service.image.tag=${TAG} \
                        --set payment-service.image.tag=${TAG} \
                        --set product-service.image.tag=${TAG} \
                        --set shipping-service.image.tag=${TAG} \
                        --set user-service.image.tag=${TAG} \
                        --set favourite-service.image.tag=${TAG} \
                        -f ecommerce-chart/ecommerce-app/${valuesFile} \
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