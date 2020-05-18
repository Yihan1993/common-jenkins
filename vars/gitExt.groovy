#!groovy

// This script simplifies the request of Git information when using the
// Multibranch Pipeline plugin
// (https://plugins.jenkins.io/workflow-multibranch).

//region Custom steps

// Gets the mail address of the last committer if the current working directory
// belongs to a Git repository.
def getLastCommitterMailAddress()
{
  def ExitCode = 1

  if (isUnix()) {
    ExitCode = sh(
      returnStatus: true,
      script: 'git rev-parse --is-inside-work-tree'
    )
  } else {
    ExitCode = bat(
      returnStatus: true,
      script: 'git rev-parse --is-inside-work-tree'
    )
  }

  if (ExitCode == 0) {
    def CommitterMail = ''

    if (isUnix()) {
      CommitterMail = sh(returnStdout: true,
        script: 'git log -1 --pretty=format:%ce 2>/dev/null'
      )
    } else {
      CommitterMail = bat(returnStdout: true,
        script: 'git log -1 --pretty=format:%%ce 2>nul'
      )
    }

    return CommitterMail.trim()
  }

  return null
}


def getPullRequestUrl()
{
  // CHANGE_URL is only set for Pull Requests
  return env.CHANGE_URL
}


// Gets the name of the task branch no matter if the branch belongs to a
// Pull Request or not.
def getTaskBranch()
{
  // The branch name for Pull Requests is stored in the environment variable
  // CHANGE_BRANCH, while all other Multibranch jobs use BRANCH_NAME
  def BranchName = env.CHANGE_BRANCH ?: env.BRANCH_NAME
  return BranchName ?: ''
}


def isPullRequest()
{
  // CHANGE_BRANCH is only set for Pull Requests
  return (env.CHANGE_BRANCH != null)
}


// Returns true if the current branch is a task branch no matter if the
// branch belongs to a Pull Request or not.
//
// The naming conventions of the ZF Branch Strategy define that task branches
// shall always start with 'task/'.
def isTaskBranch()
{
  def BranchName = getTaskBranch()
  return (BranchName ==~ /^task\/.*/) ? true : false
}

//endregion
