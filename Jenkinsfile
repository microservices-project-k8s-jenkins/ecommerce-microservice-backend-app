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
                }
            }
        }
        stage('Build and Test Microservices') {
            parallel {
                stage('Order Service') {
                    steps {
                        dir('order-service') {
                            sh './mvnw clean package'
                            sh "docker build -t $DOCKERHUB_USER/order-service:${TAG} ."
                            sh "docker push $DOCKERHUB_USER/order-service:${TAG}"
                        }
                    }
                }
                stage('Product Service') {
                    steps {
                        dir('product-service') {
                            sh './mvnw clean package'
                            sh "docker build -t $DOCKERHUB_USER/product-service:${TAG} ."
                            sh "docker push $DOCKERHUB_USER/product-service:${TAG}"
                        }
                    }
                }
                stage('User Service') {
                    steps {
                        dir('user-service') {
                            sh './mvnw clean package'
                            sh "docker build -t $DOCKERHUB_USER/user-service:${TAG} ."
                            sh "docker push $DOCKERHUB_USER/user-service:${TAG}"
                        }
                    }
                }
                stage('Api Gateway') {
                    steps {
                        dir('api-gateway') {
                            sh './mvnw clean package'
                            sh "docker build -t $DOCKERHUB_USER/api-gateway:${TAG} ."
                            sh "docker push $DOCKERHUB_USER/api-gateway:${TAG}"
                        }
                    }
                }
                stage('Proxy Client') {
                    steps {
                        dir('proxy-client') {
                            sh './mvnw clean package'
                            sh "docker build -t $DOCKERHUB_USER/proxy-client:${TAG} ."
                            sh "docker push $DOCKERHUB_USER/proxy-client:${TAG}"
                        }
                    }
                }
                stage('Shipping Service') {
                    steps {
                        dir('shipping-service') {
                            sh './mvnw clean package'
                            sh "docker build -t $DOCKERHUB_USER/shipping-service:${TAG} ."
                            sh "docker push $DOCKERHUB_USER/shipping-service:${TAG}"
                        }
                    }
                }
                stage('Favourite Service') {
                    steps {
                        dir('favourite-service') {
                            sh './mvnw clean package'
                            sh "docker build -t $DOCKERHUB_USER/favourite-service:${TAG} ."
                            sh "docker push $DOCKERHUB_USER/favourite-service:${TAG}"
                        }
                    }
                }
                stage('Payment Service') {
                    steps {
                        dir('payment-service') {
                            sh './mvnw clean package'
                            sh "docker build -t $DOCKERHUB_USER/payment-service:${TAG} ."
                            sh "docker push $DOCKERHUB_USER/payment-service:${TAG}"
                        }
                    }
                }
                stage('Cloud Config') {
                    steps {
                        dir('cloud-config') {
                            sh './mvnw clean package'
                            sh "docker build -t $DOCKERHUB_USER/cloud-config:${TAG} ."
                            sh "docker push $DOCKERHUB_USER/cloud-config:${TAG}"
                        }
                    }
                }
                stage('Service Discovery') {
                    steps {
                        dir('service-discovery') {
                            sh './mvnw clean package'
                            sh "docker build -t $DOCKERHUB_USER/service-discovery:${TAG} ."
                            sh "docker push $DOCKERHUB_USER/service-discovery:${TAG}"
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