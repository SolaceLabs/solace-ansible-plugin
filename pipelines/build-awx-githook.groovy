def cicd
def invName
def invId
def logicalBroker
def cicdExtraVars
pipeline {
  agent { label 'ansible' }
  triggers {
    githubPush()
  }
/*
  parameters {
    string( name:           'PROJECT_REPO',
            defaultValue:   'https://github.com/dennis-brinley/asyncapi-samples.git', 
            description:    'Project containing CICD build file')
    string( name:           'BRANCH',  
            defaultValue:   'development',              
            description:    'Branch in code repo to build' )
    string( name:           'CICDCONFIG_FILE',
            defaultValue:   '.jenkins/cicd-development.yaml',
            description:    'The location of the CICD config file in the repository' )
  }
*/
  environment {
    BUILD_DIR = "__BUILD_DIR__/"
    CICDCONFIG_FILE = "${BUILD_DIR}.jenkins/cicd-test.yaml"
  }
  stages {
/*
    stage( 'Checkout' ) {
        steps {
            script {
                dir( "${BUILD_DIR}" ) {
                    git branch: "${params.BRANCH}", url: "${params.PROJECT_REPO}"
                }
            }
        }
    }
    */
    stage( 'Read CICD Input' ) {
      steps {
        script {
          sh "ls -lah && pwd"
          sh "env"
//          cicd = readYaml file: "${CICDCONFIG_FILE}"
//          invName = cicd.env
//          logicalBroker = cicd.logicalBroker
//          cicdExtraVars = writeJSON returnText: true, json: cicd
        }
      }
    }
/*
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
    */
//    stage( 'Update EP MEM' ) {
//        steps {
//
//        }
    }
}