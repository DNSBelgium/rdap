pipeline {
  agent any
  triggers {
    pollSCM('H * * * *')
  }
  options {
    buildDiscarder(logRotator(numToKeepStr: '15', artifactNumToKeepStr: '15'))
  }
  parameters {
    booleanParam(name: 'RELEASE', defaultValue: false, description: 'Make a new release')
  }
  stages {
    stage('Checkout') {
      when {
        environment name: 'RELEASE', value: 'true'
      }
      steps {
        checkout scm
        sh "git checkout master"
      }
    }

    stage('Build') {
      steps {
        sh './gradlew clean build'
      }
    }

    stage('Release') {
      when {
        environment name: 'RELEASE', value: 'true'
      }
      steps {
        sh './gradlew release closeAndReleaseRepository -Prelease.useAutomaticVersion=true'
      }
    }

    stage('Publish to Maven Central') {
      steps {
        sh './gradlew uploadArchives'
      }
    }
  }

  post {
    always {
      junit '**/build/test-results/test/*.xml'
    }
  }
}
