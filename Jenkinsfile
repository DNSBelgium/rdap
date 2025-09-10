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
  environment {
    ORG_GRADLE_PROJECT_signingKeyTxtFile = credentials('ORG_GRADLE_PROJECT_signingInMemoryKeyTxt')
    ORG_GRADLE_PROJECT_signingKey = readFile file: ORG_GRADLE_PROJECT_signingKeyTxtFile
    ORG_GRADLE_PROJECT_signingKeyId = credentials('ORG_GRADLE_PROJECT_signingInMemoryKeyId')
    ORG_GRADLE_PROJECT_signingPassword = credentials('ORG_GRADLE_PROJECT_signingInMemoryKeyPassword')
    ORG_GRADLE_PROJECT_sonatype_username = credentials('ORG_GRADLE_PROJECT_sonatype_username')
    ORG_GRADLE_PROJECT_sonatype_password = credentials('ORG_GRADLE_PROJECT_sonatype_password')
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
