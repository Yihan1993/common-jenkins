
// pipeline {
//     agent any
//     libraries {
//         lib('proai-cv@master')
//     }
//     options {
//         timestamps()
//     }
//     stages{
//         stage('test hello'){
//             steps {
//                 hello("hello world")
//             }
//         }
//     }
//     //evenOrOdd(currentBuild.getNumber())
// }


@Library('proai-cv@master') _

pipeline {
    agent any

    options {
        timestamps()
    }
    
    stages{
        stage('test hello'){
           steps {
                script {
                    useHello.useHelloFun('test')
                }
           }
       }
    }
}

@Library('proai-cv@master') _

evenOrOdd(currentBuild.getNumber())

@Library('proai-cv@master') _

pipeline {
    agent any

    options {
        timestamps()
    }
    
    stages{
        stage('test hello'){
           steps {
                hello("hello world")
           }
       }
        stage('test stage class') {
            steps {
                script {
                    new DeclarativeFooStage(this).execute('something', true)
                }
            }
        }
    }
}

// jenkinsfile example with docker agent
//region Shared Libraries

@Library('proai-cv@task/ALM_Task-4748239-test-sharelibrary') _

//region Declarative Pipeline

pipeline {
  options {
    // Prepend all console outputs with a timestamp
    timestamps()
  }

  parameters {

    // Docker settings
    string(
      name: 'DOCKER_IMAGE',
      defaultValue: 'repo-manager.cloud.zf-world.com/proai_cv-docker-dev-local-frd/zf/proai-cv-safetympu:1.0.0-20201124.113234-7',
      description: 'Docker image that shall be used for the build',
      trim: true
    )
  }

  // Agent is set to none as the feature of sequential stages is used below
  // (each stage defines its own agent)
  agent none

  stages {
    // Execute the remaining part of the build in OpenShift
    stage('Schedule Pod in OpenShift') {
      agent {
        kubernetes {
          cloud 'proai_cv' // Matches the name of the cloud instance as configured in Jenkins
          label 'proai-cv-safetympu'
          slaveConnectTimeout '7200' // Wait 120 minutes for an agent to be online
          yaml """
---
apiVersion: v1
kind: Pod
spec:
  # Mark pod as failed and kill associated containers after 120 minutes
  activeDeadlineSeconds: 7200
  containers:
    -
      image: "${params.DOCKER_IMAGE}"
      name: "jnlp"
      resources:
        # Restrict the container to use at most 4 cores and 4 GB RAM
        limits:
          cpu: 4
          memory: "4Gi"
        # Request at least 2 cores and 2 GB RAM
        requests:
          cpu: 2
          memory: "2Gi"
      workingDir: "/home/jenkins/workspace"
  # Use a secret to access the private image registry.
  # The secret shall be configured in the Kubernetes cluster.
  imagePullSecrets:
    -
      name: proai-cv.docker.registry
  # Wait for 120 minutes before killing the pod after it has been received a
  # termination signal
  terminationGracePeriodSeconds: 7200
"""
        }
      }

      stages {
        stage('Setup infrastructure') {
          steps {
            script {
              // Collect all environment variables for easier debugging and
              // archive them
              dir('./output') {
                sh 'set | tee environment_variables.txt'
                archiveArtifacts 'environment_variables.txt'
              }

              customSteps.checkoutGitRepository(
                gitBranch: "master",
                gitCredentialId: "proai-cv.git",
                gitUrlToConan: "https://zf-git.emea.zf-world.com/scm/proai_cv/safetympu.git"
              )
              def buildcause = customSteps.getBuildCause()
            }
          }
        }
      } // stages

     
    } // stage
  } // stages

} // pipeline

//endregion
