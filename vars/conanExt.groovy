#!groovy

//region Imports

import com.zf.proaicv.ConanPackage

//endregion


//region Fields

@groovy.transform.Field
Map Config = [
  remote: null
]

//endregion


//region Custom steps

// Logs in to a Conan remote.
def initialize(Map argConfig)
{
  def Remote = argConfig.remote
  Config.remote = Remote

  withCredentials([usernamePassword(
      credentialsId: argConfig.credentialsId,
      usernameVariable: 'SERVER_USER',
      passwordVariable: 'SERVER_PASSWORD')])
  {
    if (isUnix()) {
      sh """#!/bin/bash
        set -eu

        conan user --remote ${Remote} ${SERVER_USER} -p ${SERVER_PASSWORD}
      """
    }
  }
}


// Extracts relevant information from a Conan lockfile and enriches the
// information with additional data like the SCM revision if possible.
def getConanPackageByLockfile(Map argConfig)
{
  def Lockfile = readJSON file: argConfig.lockfile

  // Print the content of the lockfile if requested
  if (argConfig.verbose) {
    def JsonOutput = groovy.json.JsonOutput.prettyPrint(Lockfile.toString())
    echo "JSON:\n${JsonOutput}"
  }

  // The Conan package is always the first node in the graph of the lockfile.
  // All dependencies are next to this node
  def FirstPackageReference = Lockfile.graph_lock.nodes['1'].pref

  // The package reference in the lockfile is written as:
  // <reference>#<recipe_revision>:<packageid>#<package_revision>
  def HashGroups = FirstPackageReference.split('#')
  def Reference = HashGroups[0]

  def Package = new ConanPackage(
    name: Reference.split('/')[0],
    reference: Reference,
    packageId: HashGroups[1].split(':')[1],
    recipeRevision: HashGroups[1].split(':')[0],
    packageRevision: HashGroups[2],
    scmRevision: getScmRevision(Reference)
  )
  return Package
}


// Reads one or more Conan lockfiles and builds up a map that contains all
// relevant information about the Conan packages.
def getConanPackagesAsPropertiesMap(Map argConfig)
{
  def Properties = [ 'conan.packages': [] ]
  def Lockfiles = argConfig.lockfiles ?: []

  for (Lockfile in Lockfiles) {
    def PackageInfo = getConanPackageByLockfile(
      lockfile: Lockfile
    )

    def Name = PackageInfo.name

    Properties['conan.packages'].add(Name)
    Properties["conan.${Name}.package.id"] = PackageInfo.packageId
    Properties["conan.${Name}.reference"] = PackageInfo.reference

    // Add the SCM revision (e.g. the Commit ID) only if it is set
    if (PackageInfo.scmRevision) {
      Properties["conan.${Name}.scm.revision"] = PackageInfo.scmRevision
    }
  }

  return Properties
}

//endregion


//region Private methods

// Uses 'conan inspect' to read the SCM revision that may be defined in the
// Conan recipe. Returns an empty string if no revision is found.
def private getScmRevision(String argReference)
{
  if (!isUnix()) {
    return null
  }

  def Scm = sh(returnStdout: true,
    script: "conan inspect ${argReference} --raw=scm"
  )
  def ScmJson = readJSON text: Scm ?: '{}'
  return ScmJson?.revision ?: null
}

//endregion
