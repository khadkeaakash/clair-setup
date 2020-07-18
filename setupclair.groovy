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
                docker system prune -f
                sleep 3
                docker run --rm --name postgresdb -e POSTGRES_PASSWORD=chaklee -d postgres || true
                sleep 20
                docker run --rm --link postgresdb:postgres postgres sh -c 'export PGPASSWORD=chaklee | echo "create database clairtest" | psql -h "$POSTGRES_PORT_5432_TCP_ADDR" -p "$POSTGRES_PORT_5432_TCP_PORT" -U postgres'
                docker pull quay.io/coreos/clair-jwt:v2.0.0
                '''
            } //end of steps
        } //end of stage build

    } //end of stages
} //end of pipeline
