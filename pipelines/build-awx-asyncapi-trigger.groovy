def cicd
def invName
def invId
def logicalBroker
def cicdExtraVars
def branch
pipeline {
  agent { label 'ansible' }
  parameters {
    string( name:           'WEBHOOK_REF',
            defaultValue:   'refs/heads/main', 
            description:    'refs from GitHook POST body')
    string( name:           'WEBHOOK_REPO_HTTP_URL',  
            defaultValue:   'https://github.com/PATH/TO/REPO',              
            description:    'HTTP URL of the repo with AsyncAPI Info' )
    string( name:           'WEBHOOK_REPO_SSH_URL',  
            defaultValue:   'ssh://git@github.com/PATH/TO/REPO',              
            description:    'SSH URL of the repo with AsyncAPI Info' )
    string( name:           'ASYNCAPI_FILE',
            defaultValue:   'asyncapi/acme-retail-fake-queues.yaml',
            description:    'The location of the AsyncAPI file in the repository' )
    string( name:           'REPO_CREDS_ID',
            defaultValue:   'my-jenkins-credentials-id',
            description:    'The credentials used to checkout from the repo' )    
  }
  environment {
    BUILD_DIR = "__BUILD_DIR__/"
    TMP_DIR = "tmp/"
    CICDCONFIG_FILE = "${TMP_DIR}generated-cicd-config.yaml"
    JAR_CICD_EXTRACT = "/home/jenkins/exec-jars/asyncapi-cicd-parser-0.1.1.jar"
  }
  stages {
    stage( 'Checkout' ) {
        steps {
            script {
               def values = "${WEBHOOK_REF}".split('/')
               branch = values[2]
               println( "Found Branch: ${branch}" )
            }
            script {
                dir( "${BUILD_DIR}" ) {
                    git branch: "${branch}", url: "${WEBHOOK_REPO_HTTP_URL}"
                }
            }
        }
    }
    stage( 'Extract CICD' ) {
      steps {
        script {
          sh "mkdir -p ${TMP_DIR} && rm -f ${CICDCONFIG_FILE}"
          sh "java -jar ${JAR_CICD_EXTRACT} --asyncapi-in=${BUILD_DIR}${ASYNCAPI_FILE} --output=${CICDCONFIG_FILE} --target-server=${branch}"
        }
      }
    }
    stage( 'Read CICD Input' ) {
      steps {
        script {
          cicd = readYaml file: "${CICDCONFIG_FILE}"
          invName = cicd.environment
          logicalBroker = cicd.logicalBroker
          cicdExtraVars = writeJSON returnText: true, json: cicd
          if ( invName != branch ) {
            println( "### THE [cicd_spec.environment] != [branch] of the Repo; EXITING ###" )
            error('Aborting the build.')
          }
        }
      }
    }
    stage( 'lookup tower invId' ) {
      steps {
        script {
            def responseJson = httpRequest httpMode: 'GET',
                                url: "http://awx-tower-service.awx.svc.cluster.local/api/v2/inventories/?name=${invName}",
                                authentication: 'awx-credentials',
                                validResponseCodes: "200,201"

            // ADD ERROR HANDLING
            def response = readJSON text: responseJson.getContent()

            invId = response.results[0].id

            println( "Found Inventory Name=${invName}, ID=${invId}" )
        }
      }
    }
    stage ('tower') {
        steps {
            script {
                def results = ansibleTower(
                    towerServer: 'Solace AWX',
                    jobTemplate: 'multi-queue',
                    inventory: invId.toString(),
                    limit: logicalBroker,
                    extraVars: cicdExtraVars,
                    importTowerLogs: true,
                    removeColor: false,
                    verbose: true,
                    async: false
                )
                println(results.JOB_ID)
                println(results.value)
            }
        }
    }
//    stage( 'Update EP MEM' ) {
//        steps {
//
//        }
//    }
  }
}