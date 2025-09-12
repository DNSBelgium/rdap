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
    ORG_GRADLE_PROJECT_signingInMemoryKey = readFile file: ORG_GRADLE_PROJECT_signingKeyTxtFile
    ORG_GRADLE_PROJECT_signingInMemoryKeyId = credentials('ORG_GRADLE_PROJECT_signingInMemoryKeyId')
    ORG_GRADLE_PROJECT_signingInMemoryKeyPassword = credentials('ORG_GRADLE_PROJECT_signingInMemoryKeyPassword')
    ORG_GRADLE_PROJECT_mavenCentralUsername = credentials('ORG_GRADLE_PROJECT_sonatype_username')
    ORG_GRADLE_PROJECT_mavenCentralPassword = credentials('ORG_GRADLE_PROJECT_sonatype_password')
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
      }
    }

    stage('Publish snapshot to Maven Central') {
      steps {
        sh './gradlew publishToMavenCentral'
      }
    }
  }

  post {
    always {
      junit '**/build/test-results/test/*.xml'
    }
  }
}
