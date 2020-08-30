#!groovy

//region Custom steps

// Returns a list with the mail addresses of all job owners (primary owner
// and all secondary owners).
def getJobOwnerMailAddresses()
{
  def JobOwners = []

  try {
    if (ownership.job.ownershipEnabled) {
      JobOwners += ownership.job.primaryOwnerEmail ?: []
      JobOwners += ownership.job.secondaryOwnerEmails ?: []
      JobOwners.unique()
    }
  } catch (Exception ex) {
    // do nothing if ownershop is not installed or configured
  }

  return JobOwners
}

//endregion
