pipeline {
  agent any
  tools {
    jdk 'OpenJDK 17'
  }
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
        sh "git pull --rebase"
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
        sh './gradlew release -Prelease.useAutomaticVersion=true'
        sh './gradlew closeRepository'
      }
    }

    stage('Publish to Maven Central') {
      steps {
        sh './gradlew publish'
      }
    }
  }

  post {
    always {
      junit '**/build/test-results/test/*.xml'
    }
  }
}
