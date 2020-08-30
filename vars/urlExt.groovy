#!groovy

//region Imports

import java.net.URI

//endregion


//region Custom steps

// Returns the host of an URL.
def getDomainName(Map argConfig)
{
  def Uri = new URI(argConfig.url)
  return Uri.getHost()
}

//endregion
