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

        stage('Configure Kubernetes Access') {
            when {
                expression { env.IS_PR == 'false' }
            }
            steps {
                script {
                    sh '''
                        echo "Configuring kubectl for minikube from Docker container"
                        
                        cp /var/jenkins_home/.kube/config /tmp/kubeconfig
                        export KUBECONFIG=/tmp/kubeconfig
                        
                        MINIKUBE_SERVER=$(kubectl config view --raw -o jsonpath='{.clusters[?(@.name=="minikube")].cluster.server}')
                        echo "Original minikube server: $MINIKUBE_SERVER"
                        PORT=$(echo $MINIKUBE_SERVER | sed 's/.*://')
                        echo "Port: $PORT"
                        
                        MINIKUBE_IP="192.168.49.2"
                        
                        kubectl config set-cluster minikube --server=https://$MINIKUBE_IP:$PORT --insecure-skip-tls-verify=true
                        
                        kubectl cluster-info
                    '''
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
                        helm upgrade --install ecommerce-app ecommerce-chart \\
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
                        -f ecommerce-chart/${valuesFile} \\
                        --wait --timeout=10m
                    """
                    env.DEPLOY_NAMESPACE = namespace
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