#!groovy

//region Imports

import hudson.model.ParametersDefinitionProperty

//endregion


//region Custom steps

// Resets all build parameters to their default values if the build is not
// triggered manually by the user.
def call()
{
  if (buildExt.isBuildTriggeredByUser()) {
    return
  }

  getAllBuildParameters().each {
    echo "Set parameter '${it}' to its default value ..."

    def DefaultValue = getDefaultValueOfParameter(it)

    if (DefaultValue) {
      updateParameterValue(DefaultValue)
    }
  }
}

//endregion


//region Private methods

def private getAllBuildParameters()
{
  return params.keySet()
}


def private getDefaultValueOfParameter(String argName)
{
  def BuildProperties = currentBuild.rawBuild.parent.properties
  def Definitions = BuildProperties.find {
    it.value instanceof ParametersDefinitionProperty
  }

  if (!Definitions) {
    return null
  }

  def ParamDefinition = Definitions.value.getParameterDefinition(argName)
  return ParamDefinition?.getDefaultParameterValue()
}


def private updateParameterValue(argParameterValue)
{
  def Action = currentBuild.rawBuild.getAction(ParametersAction.class)

  // Action may be null if no previous build or build parameter is available
  if (!Action) {
    return
  }

  def UpdatedAction = Action.createUpdated( [ argParameterValue ] )
  currentBuild.rawBuild.addOrReplaceAction(UpdatedAction)
}

//endregion
