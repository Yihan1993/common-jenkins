#!groovy

//region Custom steps

def isBuildTriggeredByUser()
{
  def UserBuilds = currentBuild.getBuildCauses(
    'hudson.model.Cause$UserIdCause')
  return (UserBuilds.size() > 0 ? true : false)
}

//endregion
