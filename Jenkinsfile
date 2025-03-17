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
    // these are dnsbelgium specific entries for open sourcing this library
    'ORG_GRADLE_PROJECT_signing.secretKeyRingFile' = credentials('ORG_GRADLE_PROJECT_signing.secretKeyRingFile')
    'ORG_GRADLE_PROJECT_signing.keyId' = credentials('ORG_GRADLE_PROJECT_signing.keyId')
    'ORG_GRADLE_PROJECT_signing.password' = credentials('ORG_GRADLE_PROJECT_signing.password')
    //ORG_GRADLE_PROJECT_sonatype_snapshot_url = 'https://oss.sonatype.org/content/repositories/snapshots/'
    //ORG_GRADLE_PROJECT_sonatype_staging_url = 'https://oss.sonatype.org/service/local/staging/deploy/maven2/'
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
