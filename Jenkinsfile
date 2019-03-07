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

        /*
        stage('Deploy to DEV') {
            agent {
                node { 
                    label "master"
                }
            }
            steps {
                echo '❤️ Deploy application in DEV environment'
                script{
                    openshift.withCluster() {
                        openshift.withProject( "${DEV_NAMESPACE}" ) {
                            echo "Deploying to: ${openshift.project()}"
                            def dc = openshift.selector('dc', "${APP_NAME}")
                            dc.rollout().latest()
                            dc.rollout().status()
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

        stage("Run tests in DEV"){
            agent {
                node { 
                    label "master"
                }
            }
            steps {
                echo '👷 Run tests in DEV...'

                sh  '''
                    echo "Dummy test passed succesfully!"
                '''
            } 
            post {
                failure {
                    notifyBuild('FAIL')
                }
            }
        }

        stage("Verify aplication in DEV") {
            agent {
                node { 
                    label "master"
                }
            }
            steps {
                echo 'Verify Deployment in RELEASE'

                script{
                    openshift.withCluster() {
                        openshift.withProject( "${DEV_NAMESPACE}" ) {
                            echo "Deploying to: ${openshift.project()}"
                            def dc = openshift.selector('dc', "${APP_NAME}")
                            dc.rollout().latest()
                            dc.rollout().status()
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

        /*
        stage('Manual Approval to promote to RELEASE') {
            agent none
            steps {
                script {
                    slackSend (color: '#0000FF', message: "Jenkins APPROVAL required")
                    slackSend (color: '#0000FF', message: "URL to APPROVE or DISCARD:  (${env.BUILD_URL}input)")
                    env.PROMOTE_TO_RELEASE = input message: 'User Approval Required',
                    parameters: [choice(name: 'Promote to RELEASE', choices: 'no\nyes', description: 'There are new DEV Images. Do you want promote them to RELEASE?')]
                }
            }
        }
        */  
 
        stage("Promote to RELEASE") {
            agent {
                node { 
                    label "master"
                }
            }
            /*when {
                environment name: 'PROMOTE_TO_RELEASE', value: 'yes'
            }*/
            steps {
                    echo 'Promoting Container Image to Release'
                    script {
                        openshift.withProject( "${DEV_NAMESPACE}" ) {
                            echo "Promoting to ${RELEASE_NAMESPACE}"
                            openshift.tag("${APP_NAME}:latest", "${RELEASE_NAMESPACE}/${APP_NAME}:release")
                        }
                    }
            }
            post {
                failure {
                    notifyBuild('FAIL')
                }
            }
        }

        stage("Create OpenShift objects in RELEASE") {
            agent {
                node { 
                    label "jenkins-slave-ansible"
                }
            }
            steps {
                echo '👷 Create OpenShift objects in RELEASE using openshift-applier...'

                sh  '''
                ansible-galaxy install -r .applier/requirements.yml --roles-path=.applier/roles
                ansible-playbook -i .applier/inventory .applier/apply.yml -e filter_tags=release
                '''
            }
            post {
                failure {
                    notifyBuild('FAIL')
                }
            }
        }

        stage('Deploy to RELEASE') {
            agent {
                node { 
                    label "master"
                }
            }
            steps {
                echo '❤️ Deploy application in RELEASE'
                script{
                    openshift.withCluster() {
                        openshift.withProject( "${RELEASE_NAMESPACE}" ) {
                            echo "Deploying to: ${openshift.project()}"
                            def dc = openshift.selector('dc', "${APP_NAME}")
                            dc.rollout().latest()
                            dc.rollout().status()
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
        
        stage("Verify aplication in RELEASE") {
            agent {
                node { 
                    label "master"
                }
            }
            steps {
                echo 'Verify Deployment in RELEASE'

                script{
                    openshift.withCluster() {
                        openshift.withProject( "${RELEASE_NAMESPACE}" ) {
                            echo "Deploying to: ${openshift.project()}"
                            def dc = openshift.selector('dc', "${APP_NAME}")
                            dc.rollout().latest()
                            dc.rollout().status()
                        }
                    }
                }
            }
            post {
                success {
                    // Merge changes into release branch
                    git branch: 'master',
                        credentialsId: 'cicd-github-secret',
                        url: "$GIT_URL"
                      
                    sh  '''
                        GIT_SERVER_NAME=$(echo ${GIT_URL} | cut -d'/' -f3)
                        GIT_PROJECT_NAME=$(echo ${GIT_URL} | cut -d'/' -f4)
                        GIT_REPO_NAME=$(echo ${GIT_URL} | cut -d'/' -f5)

                        git remote remove origin
                        git remote add origin https://${GIT_CREDENTIALS_USR}:${GIT_CREDENTIALS_PSW}@${GIT_SERVER_NAME}/${GIT_PROJECT_NAME}/${GIT_REPO_NAME}.git
                        
                        git config user.name 'JenkinsCI'
                        git config user.email 'jenkins@jenkins.com'

                        git fetch
                        git checkout release
                        git pull origin release

                        # Eval if there is any difference
                        RELEASE_COMMIT=$(git rev-parse --verify HEAD)
                        git diff $GIT_COMMIT $RELEASE_COMMIT > git_diff.out
                        if [[ -s git_diff.out ]]; then
                            # File size greater than zero, there are changes
                            git checkout master
                            git pull origin master
                            git push origin master:release -f
                           
                        else
                            # File size equal to zero, no changes
                            echo "Nothing to commit -- MASTER and RELEASE branches are up to date"
                        fi
                    '''
                    notifyBuild('SUCCESSFUL')
                }
                failure {
                    notifyBuild('FAIL')
                }
            }
        }*/
    }
}
