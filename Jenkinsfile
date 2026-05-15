pipeline {
    agent any
    environment {
        EC2_HOST = '18.234.44.203'
        EC2_USER = 'ubuntu'
    }
    triggers { githubPush() }
    stage('Health Check') {
    steps {
        sshagent(credentials: ['ec2-ssh-key']) {
            sh '''
                ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} "
                    echo 'Waiting 60s for services to start...'
                    sleep 60
                    curl -f http://localhost:8761 && echo 'Eureka UP' || echo 'Eureka still starting - check manually'
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