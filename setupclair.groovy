pipeline {
    agent {label "ubuntu"
          }//end of agent

    stages {
        stage('claire setup') {
            steps {
                sh '''
                docker pull postgres:latest
                docker run --name postgres -p 5432:5432 -d postgres
                docker run --rm --link postgres:postgres postgres sh -c 'echo "create database clairtest" | psql -h "$POSTGRES_PORT_5432_TCP_ADDR" -p "$POSTGRES_PORT_5432_TCP_PORT" -U postgres'
                '''
            } //end of steps
        } //end of stage build

    } //end of stages
} //end of pipeline
