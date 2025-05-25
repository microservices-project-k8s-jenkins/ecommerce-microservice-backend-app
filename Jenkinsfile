pipeline {
    agent any
    options {
        skipDefaultCheckout(true)
    }
    environment {
        DOCKERHUB_USER = 'sjbs0212'
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
        stage('Build and Test Microservices') {
            parallel {
                stage('Order Service') {
                    steps {
                        dir('order-service') {
                            sh './mvnw clean package'
                            sh "docker build -t ${DOCKERHUB_USER}/order-service:${TAG} ."
                            sh "docker push ${DOCKERHUB_USER}/order-service:${TAG}"
                        }
                    }
                }
                stage('Product Service') {
                    steps {
                        dir('product-service') {
                            sh './mvnw clean package'
                            sh "docker build -t ${DOCKERHUB_USER}/product-service:${TAG} ."
                            sh "docker push ${DOCKERHUB_USER}/product-service:${TAG}"
                        }
                    }
                }
                stage('User Service') {
                    steps {
                        dir('user-service') {
                            sh './mvnw clean package'
                            sh "docker build -t ${DOCKERHUB_USER}/user-service:${TAG} ."
                            sh "docker push ${DOCKERHUB_USER}/user-service:${TAG}"
                        }
                    }
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
                    sh "helm upgrade --install ecommerce-app ecommerce-chart/ecommerce-app --namespace ${namespace} --set cloud-config.image.tag=${TAG},service-discovery.image.tag=${TAG},api-gateway.image.tag=${TAG},proxy-client.image.tag=${TAG},order-service.image.tag=${TAG},payment-service.image.tag=${TAG},product-service.image.tag=${TAG},shipping-service.image.tag=${TAG},user-service.image.tag=${TAG},favourite-service.image.tag=${TAG} -f ecommerce-chart/ecommerce-app/${valuesFile}"
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
                    sh "kubectl rollout status deployment/order-service --namespace ${namespace}"
                    sh "kubectl rollout status deployment/product-service --namespace ${namespace}"
                    sh "kubectl rollout status deployment/user-service --namespace ${namespace}"
                    dir('order-service') {
                        sh 'locust -f tests/performance/locustfile.py --headless -u 100 -r 10 --run-time 1m --html=../reports/order-service-report.html --csv=../reports/order-service'
                    }
                    dir('product-service') {
                        sh 'locust -f tests/performance/locustfile.py --headless -u 100 -r 10 --run-time 1m --html=../reports/product-service-report.html --csv=../reports/product-service'
                    }
                    dir('user-service') {
                        sh 'locust -f tests/performance/locustfile.py --headless -u 100 -r 10 --run-time 1m --html=../reports/user-service-report.html --csv=../reports/user-service'
                    }
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'reports/*.html', fingerprint: true
                    archiveArtifacts artifacts: 'reports/*.csv', fingerprint: true
                    
                    publishHTML([
                        allowMissing: false,
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