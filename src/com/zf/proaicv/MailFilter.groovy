#!groovy

package com.zf.proaicv

//region Classes

// Provides methods to filter mail addresses.
class MailFilter {

  //region Public static methods

  // Filters out blacklisted mail addresses or mail addresses that do not
  // belong to the company.
  static def filter(List argList) {
    return argList.findAll { isCompanyMailAddress(it) && !isBlacklisted(it) }
  }

  //endregion


  //region Private static methods

  // Returns true if the mail address ends with '@zf.com'.
  static def private isCompanyMailAddress(String MailAddress)
  {
    // Check for 'zf.com' as domain (use '?i' to ignore case)
    return (MailAddress =~ /(?i)@zf\.com$/) ? true : false
  }


  // Returns true if the mail address does not belong to a valid person or is
  // blacklisted for other reason. E.g. mail addresses like no-reply@zf.com
  // shall be filtered out.
  static def private isBlacklisted(String MailAddress)
  {
    // Check for special user names (use '?i' to ignore case)
    return (MailAddress =~ /(?i)^(SYSTEM|no\-reply|noreply)/) ? true : false
  }

  //endregion

}

//endregion
