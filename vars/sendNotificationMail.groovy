#!groovy

//region Imports

import com.zf.proaicv.MailFilter

//endregion


//region Custom steps

// Sends a notification mail about the build depending on the branch type and
// the build result.
//
// The real logic to send the notification mail is implemented in the
// function isNotificationMailNeeded().
def call()
{
  // Use single quotes below to prevent automatic expansion of the variables
  // DEFAULT_CONTENT, DEFAULT_SUBJECT and DEFAULT_REPLYTO (they will be
  // expanded by the Email Extension Plugin)

  // Append the URL of the Pull Request to the mail's content
  def BodyText = '$DEFAULT_CONTENT'
  if (gitExt.isPullRequest()) {
    BodyText = '$DEFAULT_CONTENT\n\n' + gitExt.getPullRequestUrl()
  }

  if (isNotificationMailNeeded()) {
    emailext subject: '$DEFAULT_SUBJECT',
      body: BodyText,
      replyTo: '$DEFAULT_REPLYTO',
      to: getUserMailAddresses()
  }
}

//endregion


//region Private methods

// Returns the default recipients configured for the Email Extension Plugin.
def private getFallbackMailAddress()
{
  // Use single quotes to prevent automatic expansion of the variable
  // DEFAULT_RECIPIENTS (it will be expanded by the plugin)
  return '$DEFAULT_RECIPIENTS'
}


// Returns the list of mail addresses that shall be informed about the
// current build.
//
// On task branches the last committer shall be informed only. On all other
// kind of branches the job owners shall be informed.
def private getUserMailAddresses()
{
  def Users = []

  // The culprits functionality of Jenkins does not work properly, e.g. the
  // initial build may not contain a changeset and rebase commits may also
  // lead to an invalid list of culprits. Therefore we need to calculate the
  // culprits at our own.
  Users += (gitExt.isTaskBranch() ? gitExt.getLastCommitterMailAddress() :
    jobExt.getJobOwnerMailAddresses())

  // Filter out unwanted mail addresses
  Users = MailFilter.filter(Users)

  if (Users.isEmpty()) {
    Users += getFallbackMailAddress()
  }

  return Users.unique().join(',')
}


// Implements the logic when users shall be notified about a build:
// - Builds that belong to a Pull Request shall always send a notification
//   mail.
// - Builds that got fixed shall always send a notification mail.
// - Builds that fail the first time or fail repeatedly (except if they have
//   been triggered manually) shall send a notification mail.
def private isNotificationMailNeeded()
{
  if (gitExt.isPullRequest()) {
    return true
  }

  // Send mail for builds that got fixed
  if ((currentBuild.result == 'SUCCESS') &&
      (currentBuild.previousBuild?.result != 'SUCCESS')) {
    return true
  }

  // Send mail for builds that fail the first time or that fail repeatedly
  if (currentBuild.resultIsWorseOrEqualTo('UNSTABLE')) {
    if ((currentBuild.previousBuild?.result != 'FAILURE') ||
        !(buildExt.isBuildTriggeredByUser())) {
      return true
    }
  }

  return false
}

//endregion
