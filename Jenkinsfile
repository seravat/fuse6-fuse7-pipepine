openshift.withCluster() {
  env.APP_NAME = "fuse7cxfRest"
  env.PIPELINES_NAMESPACE = "cicd"
  env.BUILD_NAMESPACE = "dev"
  env.DEV_NAMESPACE = "dev"
  env.RELEASE_NAMESPACE = "release"
  env.GIT_URL = "https://github.com/seravat/fuse6_to_fuse7"
  echo "Starting Pipeline for ${APP_NAME}..."
}

def notifyBuild(String buildStatus = 'STARTED') {
  // build status of null means successful
  buildStatus =  buildStatus ?: 'SUCCESSFUL'

  // Default values
  def colorName = 'RED'
  def colorCode = '#FF0000'
  def subject = "${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
  def summary = "${subject} (${env.BUILD_URL})"

  // Override default values based on build status
  if (buildStatus == 'STARTED') {
    color = 'YELLOW'
    colorCode = '#FFFF00'
  } else if (buildStatus == 'SUCCESSFUL') {
    color = 'GREEN'
    colorCode = '#00FF00'
  } else {
    color = 'RED'
    colorCode = '#FF0000'
  }

  // Send notifications
//   slackSend (color: colorCode, message: summary)
}

pipeline {

    agent {
        label "master"
    }

    environment {
        GIT_SSL_NO_VERIFY = true
        GIT_CREDENTIALS = credentials('cicd-github-secret')

        JENKINS_TAG = "${JOB_NAME}.${BUILD_NUMBER}"
        RELEASE_TAG = "release"
    }

    stages {

        stage("Pipeline Start") {
            steps {
                notifyBuild('STARTED')
            }
            post {
                failure {
                    notifyBuild('FAIL')
                }
            }
        }
        
        stage("Create OpenShift objects in DEV") {
            agent {
                node { 
                    label "jenkins-slave-ansible"
                }
            }
            steps {
                echo '👷 Create OpenShift objects in DEV using openshift-applier...'

                sh  '''
                printenv
                ansible-galaxy install -r .applier/requirements.yml --roles-path=.applier/roles
                ansible-playbook -i .applier/inventory .applier/apply.yml -e filter_tags=dev
                '''
            }
            post {
                failure {
                    notifyBuild('FAIL')
                }
            }
        }
      
        stage('Build') {
            agent {
                node {
                    label "master"
                }
            }
            steps {
                echo '❤️ Build application'
                script{
                    openshift.withCluster() {
                        openshift.withProject( "${DEV_NAMESPACE}" ) {
                            echo "Using project: ${openshift.project()}"
                            sh '''
                                cd fuse7cxfRest
                                mvn fabric8:deploy -Dfabric8.generator.from=openshift/jboss-fuse70-karaf-openshift:1.0
                            '''
                            // openshift.startBuild("${APP_NAME}","--follow","--wait")
                        }
                    }
                } 
            }
            post {
                failure {
                    notifyBuild('FAIL')
                }
            }
        }


    }
}
