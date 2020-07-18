pipeline {
        agent 
        {
               label "ubuntu"
          }//end of agent
    environment
    {
                
        PGPASSWORD='chaklee'
    }
    stages {
        stage('claire setup') {
            steps {
                sh '''
                docker pull postgres:latest
                #export PGPASSWORD='chaklee'
                docker container prune -f -a
                sleep 3
                docker run --rm --name postgres -d postgres || true
                sleep 20
                docker run --rm --link postgres:postgres postgres sh -c 'echo "create database clairtest" | psql -h "$POSTGRES_PORT_5432_TCP_ADDR" -p "$POSTGRES_PORT_5432_TCP_PORT" -U postgres'
                docker pull quay.io/coreos/clair-jwt:v2.0.0
                '''
            } //end of steps
        } //end of stage build

    } //end of stages
} //end of pipeline
