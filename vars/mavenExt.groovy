#!groovy

//region Custom steps

// Gets a Maven like file version.
//
// For release builds the method will return the base revision.
// For snapshot (non-release) builds the method will return the version as
// follows: <baseRevision>-<date>.<time>-<buildNumber>.
// For task branches the method will return the name of the task branch
// (slashes will be replaced with hyphens to prevent strange behavior in
// Artifactory).
def getFileVersion(Map argConfig)
{
  if (argConfig.release) {
    return argConfig.baseRevision
  }

  if (!argConfig.ignoreTaskBranch && gitExt.isTaskBranch()) {
    // Slashes would cause a new subdirectory in Artifactory which is not
    // desired here, so replace them with hyphens
    return gitExt.getTaskBranch().replace('/', '-')
  }

  // Do not use new Date() as we need a unique timestamp that is valid for the
  // entire build
  def CurrentDate = new Date((long)currentBuild.startTimeInMillis)

  def FileRevision = CurrentDate.format('yyyyMMdd.HHmmss')
  def BuildNumber = currentBuild.number.toString()
  return argConfig.baseRevision + '-' + FileRevision + '-' + BuildNumber
}


// Gets a Maven like folder version.
//
// For release builds the method will return the base revision.
// For snapshot (non-release) builds the method will return the version as
// follows: <baseRevision>-SNAPSHOT. It is the default behavior of Maven to
// collect all snapshot builds in the same folder.
// For task branches the method will return the name of the task branch
// (slashes will be replaced with hyphens to prevent strange behavior in
// Artifactory).
def getFolderVersion(Map argConfig)
{
  if (!argConfig.ignoreTaskBranch && gitExt.isTaskBranch()) {
    // Slashes would cause a new subdirectory in Artifactory which is not
    // desired here, so replace them with hyphens
    return gitExt.getTaskBranch().replace('/', '-')
  }

  def IntegrationRevision = argConfig.release ? '' : '-SNAPSHOT'
  return "${argConfig.baseRevision}${IntegrationRevision}"
}

//endregion
