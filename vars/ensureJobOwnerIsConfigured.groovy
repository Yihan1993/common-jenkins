#!groovy

//region Imports

import com.zf.proaicv.MailFilter

//endregion


//region Custom steps

// Sets the current build stage to 'UNSTABLE' if no valid job owner is
// configured for a maintenance or integration branch. However, the entire
// build result remains untouched.
def call()
{
  if (gitExt.isTaskBranch()) {
    return
  }

  def Users = jobExt.getJobOwnerMailAddresses()
  Users = MailFilter.filter(Users)

  catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
    if (Users.isEmpty()) {
      error("No valid Job Owner configured for a maintenance or integration " +
        "branch!")
    }
  }
}

//endregion
