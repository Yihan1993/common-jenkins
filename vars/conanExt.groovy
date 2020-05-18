#!groovy

//region Custom steps

// Logs in to a Conan remote.
def initialize(Map argConfig)
{
  def Remote = argConfig.remote

  withCredentials([usernamePassword(
      credentialsId: argConfig.credentialsId,
      usernameVariable: 'SERVER_USER',
      passwordVariable: 'SERVER_PASSWORD')])
  {
    if (isUnix()) {
      sh """#!/bin/bash
        set -eu

        conan user --remote ${Remote} ${SERVER_USER} -p ${SERVER_PASSWORD}
      """
    }
  }
}

//endregion
