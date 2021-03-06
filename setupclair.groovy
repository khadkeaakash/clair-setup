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
                    withCredentials ([
                                        string(credentialsId: 'redhat-quay-token', variable: 'redhat-quay-token'),
                                        string(credentialsId: 'quay-auth-token', variable: 'quay-auth-token')     
                                    ])
                    {
                            
                    script {
                                sh '''
                                        docker pull postgres:latest
                                        #export PGPASSWORD='chaklee'
                                        docker container stop claire || true
                                        docker container rm claire || true
                                        docker container stop postgresdb || true
                                        docker container rm postgresdb || true
                                        docker container stop quayredisdb || true
                                        docker container rm quayredisdb || true
                                        docker container stop quaycore || true
                                        docker container rm quaycore || true
                                        sleep 3
                                        docker run --rm --name postgresdb -e POSTGRES_PASSWORD=chaklee -d postgres || true
                                        sleep 5
                                        docker run --rm --link postgresdb:postgres postgres sh -c 'echo "create database clairtest" | PGPASSWORD=chaklee psql -h "$POSTGRES_PORT_5432_TCP_ADDR" -p "$POSTGRES_PORT_5432_TCP_PORT" -U postgres' ||true
                                        #curfold=$(pwd)
                                        cd $HOME
                                        mkdir core || true
                                        cd core ||true
                                        mkdir .docker ||true
                                        rm -rf config.json ||true
                                        cat << 'EOF' >> config.json
{
  "auths":{
    "quay.io": {
        "auth": ${quay-auth-token},
        "email": ""
    }
  }
}
EOF

                                        cat config.json
                                        cd $WORKSPACE
                                        
                                        
                                        docker pull quay.io/quay/redis
                                        docker run --rm --name quayredisdb -d -p 6379:6379 quay.io/quay/redis
                                        
                                        docker login -u "coreos+rhcp" -p ${redhat-quay-token} quay.io
                                        docker pull quay.io/coreos/quay:v2.9.3
                                        mkdir storage
                                        mkdir config
                                        docker run --restart=always --name quaycore -p 443:443 -p 80:80 --privileged=true -v ./config:/conf/stack -v ./storage:/datastorage -d quay.io/coreos/quay:v2.9.3

                                        

                                        docker pull quay.io/coreos/clair-jwt:v2.0.0
                                        mkdir clair-config || true
                                        cd clair-config ||true
                                        cat << 'EOF' >> config.yaml
clair:
  database:
    type: pgsql
    options:
      # A PostgreSQL Connection string pointing to the Clair Postgres database.
      # Documentation on the format can be found at: http://www.postgresql.org/docs/9.4/static/libpq-connect.html
      source: postgresql://postgres@172.17.0.2:5432/clairtest?sslmode=disable
      cachesize: 16384
  api:
    # The port at which Clair will report its health status. For example, if Clair is running at
    # https://clair.mycompany.com, the health will be reported at
    # http://clair.mycompany.com:6061/health.
    healthport: 6061

    port: 6062
    timeout: 900s

    # paginationkey can be any random set of characters. *Must be the same across all Clair instances*.
    paginationkey:

  updater:
    # interval defines how often Clair will check for updates from its upstream vulnerability databases.
    interval: 6h
    notifier:
      attempts: 3
      renotifyinterval: 1h
      http:
        # QUAY_ENDPOINT defines the endpoint at which Quay Enterprise is running.
        # For example: https://myregistry.mycompany.com
        endpoint: { QUAY_ENDPOINT }/secscan/notify
        proxy: http://localhost:6063

jwtproxy:
  signer_proxy:
    enabled: true
    listen_addr: :6063
    ca_key_file: /certificates/mitm.key # Generated internally, do not change.
    ca_crt_file: /certificates/mitm.crt # Generated internally, do not change.
    signer:
      issuer: security_scanner
      expiration_time: 5m
      max_skew: 1m
      nonce_length: 32
      private_key:
        type: autogenerated
        options:
          rotate_every: 12h
          key_folder: /config/
          key_server:
            type: keyregistry
            options:
              # QUAY_ENDPOINT defines the endpoint at which Quay Enterprise is running.
              # For example: https://myregistry.mycompany.com
              registry: { QUAY_ENDPOINT }/keys/


  verifier_proxies:
  - enabled: true
    # The port at which Clair will listen.
    listen_addr: :6060

    # If Clair is to be served via TLS, uncomment these lines. See the "Running Clair under TLS"
    # section below for more information.
    # key_file: /config/clair.key
    # crt_file: /config/clair.crt

    verifier:
      # CLAIR_ENDPOINT is the endpoint at which this Clair will be accessible. Note that the port
      # specified here must match the listen_addr port a few lines above this.
      # Example: https://myclair.mycompany.com:6060
      audience: { CLAIR_ENDPOINT }

      upstream: http://localhost:6062
      key_server:
        type: keyregistry
        options:
          # QUAY_ENDPOINT defines the endpoint at which Quay Enterprise is running.
          # Example: https://myregistry.mycompany.com
          registry: { QUAY_ENDPOINT }/keys/
EOF

                                                #cat config.yaml

                                                #docker run --restart=always --name=claire -p 6060:6060 -p 6061:6061 -v $(pwd):/config quay.io/coreos/clair-jwt:v2.0.0
                '''
                    } //end of script  
                } //end of withCredentials              
            } //end of steps
        } //end of stage build
    } //end of stages
} //end of pipeline
