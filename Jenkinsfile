pipeline {
    agent any
    environment {
        EC2_HOST = '18.234.44.203'
        EC2_USER = 'ubuntu'
    }
    triggers { githubPush() }
    stages {
        stage('Deploy to EC2') {
            steps {
                sshagent(credentials: ['ec2-ssh-key']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} "
                            cd /home/ubuntu/BookNestBackendD
                            git pull origin main
                            cd fresh-microservice
                            docker compose up -d --build
                        "
                    '''
                }
            }
        }
        stage('Health Check') {
            steps {
                sshagent(credentials: ['ec2-ssh-key']) {
                    sh 'ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} "curl -f http://localhost:8761 && echo Eureka UP"'
                }
            }
        }
    }
    post {
        success { echo 'Backend deployed!' }
        failure { echo 'Deployment failed!' }
    }
}