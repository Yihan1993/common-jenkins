#!groovy

//region Imports

import hudson.model.ParametersDefinitionProperty

//endregion


//region Custom steps

def call()
{
  if (buildExt.isBuildTriggeredByUser()) {
    return
  }

  getAllBuildParameters().each {
    echo "Set parameter '${it}' to its default value ..."

    def defaultValue = getDefaultValueOfParameter(it)
    updateParameterValue(defaultValue)
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
  def buildProperties = currentBuild.rawBuild.parent.properties
  def definitions = buildProperties.find {
    it.value instanceof ParametersDefinitionProperty
  }.value

  def paramDefinition = definitions.getParameterDefinition(argName)
  return paramDefinition.getDefaultParameterValue()
}


def private updateParameterValue(argParameterValue)
{
  currentBuild.rawBuild.addOrReplaceAction(
    currentBuild.rawBuild.getAction(ParametersAction.class).createUpdated(
      [ argParameterValue ]
    )
  )
}

//endregion
