pipeline {
    agent any
    environment {
        DOCKERHUB_USER = 'sjbs0212'
        TAG = "${env.BUILD_NUMBER}"
        IS_PR = "${env.CHANGE_ID != null ? 'true' : 'false'}"
        IS_DEV_PR = "${env.CHANGE_ID != null && env.CHANGE_TARGET == 'dev' ? 'true' : 'false'}"
    }
    stages {
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
                            sh './mvnw test -Dtest=com.ecommerce.orderservice.unit.*Test'
                            sh './mvnw test -Dtest=com.ecommerce.orderservice.integration.*Test'
                            sh './mvnw test -Dtest=com.ecommerce.orderservice.e2e.*Test'
                            sh "docker build -t ${DOCKERHUB_USER}/order-service:${TAG} ."
                            sh "docker push ${DOCKERHUB_USER}/order-service:${TAG}"
                        }
                    }
                }
                stage('Product Service') {
                    steps {
                        dir('product-service') {
                            sh './mvnw test -Dtest=com.ecommerce.productservice.unit.*Test'
                            sh './mvnw test -Dtest=com.ecommerce.productservice.integration.*Test'
                            sh './mvnw test -Dtest=com.ecommerce.productservice.e2e.*Test'
                            sh "docker build -t ${DOCKERHUB_USER}/product-service:${TAG} ."
                            sh "docker push ${DOCKERHUB_USER}/product-service:${TAG}"
                        }
                    }
                }
                stage('User Service') {
                    steps {
                        dir('user-service') {
                            sh './mvnw test -Dtest=com.ecommerce.userservice.unit.*Test'
                            sh './mvnw test -Dtest=com.ecommerce.userservice.integration.*Test'
                            sh './mvnw test -Dtest=com.ecommerce.userservice.e2e.*Test'
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
                    sh "helm upgrade --install ecommerce-app ecommerce-charts/ecommerce-app --namespace ${namespace} --set cloud-config.image.tag=${TAG},service-discovery.image.tag=${TAG},api-gateway.image.tag=${TAG},proxy-client.image.tag=${TAG},order-service.image.tag=${TAG},payment-service.image.tag=${TAG},product-service.image.tag=${TAG},shipping-service.image.tag=${TAG},user-service.image.tag=${TAG},favourite-service.image.tag=${TAG} -f ecommerce-charts/ecommerce-app/${valuesFile}"
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
                    def namespace = env.DEPLOY_NAMESPACE ?: (env.BRANCH_NAME == 'dev' ? 'dev' : (env.BRANCH_NAME == 'stage' ? 'stage' : (env.BRANCH_NAME == 'prod' ? 'master' : 'dev')))
                    sh "kubectl rollout status deployment/order-service --namespace ${namespace}"
                    sh "kubectl rollout status deployment/product-service --namespace ${namespace}"
                    sh "kubectl rollout status deployment/user-service --namespace ${namespace}"
                    dir('order-service') {
                        sh 'locust -f tests/performance/locustfile.py --headless -u 100 -r 10 --run-time 1m'
                    }
                    dir('product-service') {
                        sh 'locust -f tests/performance/locustfile.py --headless -u 100 -r 10 --run-time 1m'
                    }
                    dir('user-service') {
                        sh 'locust -f tests/performance/locustfile.py --headless -u 100 -r 10 --run-time 1m'
                    }
                }
            }
        }
    }
}