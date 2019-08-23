pipeline {
  agent any
  triggers {
    pollSCM('H * * * *')
  }
  options {
    buildDiscarder(logRotator(numToKeepStr: '15', artifactNumToKeepStr: '15'))
  }
//   parameters {
//     booleanParam(name: 'RELEASE', defaultValue: false, description: 'Make a new release')
//   }
  stages {
    stage('Build') {
      steps {
        sh './gradlew clean build'
      }
    }

//     stage('Release') {
//       when {
//         environment name: 'RELEASE', value: 'true'
//       }
//       steps {
//         sh './gradlew release -Prelease.useAutomaticVersion=true'
//       }
//     }

    stage('Publish to Maven Central') {
      steps {
        sh './gradlew uploadArchives'
      }
    }
  }

  post {
    always {
      junit '**/build/test-results/*.xml'
    }
  }
}
