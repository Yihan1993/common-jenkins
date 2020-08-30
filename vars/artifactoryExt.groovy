#!groovy

// Some functionality below refers to the file specification of the Artifactory
// plugin. Check
// https://www.jfrog.com/confluence/display/JFROG/Using+File+Specs for more
// details about this specification.

//region Fields

@groovy.transform.Field
Map Config = [
  credentialsId: null,
  serverId: null,
  serverUrl: null,
  release: false
]

//endregion


//region Custom steps

// Configures the connection to the Artifactory server that is used by the
// Artifactory plugin.
def initialize(Map argConfig)
{
  Config.credentialsId = argConfig.credentialsId
  Config.release = argConfig.release ?: false
  Config.serverId = argConfig.serverId ?: UUID.randomUUID().toString()
  Config.serverUrl = argConfig.serverUrl

  // For unknown reason the Artifactory plugin does not work properly when
  // using the credentialsId property
  // TODO: Check usage of credentialsId in upcoming versions of the plugin
  withCredentials([usernamePassword(
    credentialsId: Config.credentialsId,
    usernameVariable: 'SERVER_USER',
    passwordVariable: 'SERVER_PASSWORD')])
  {
    rtServer(
      id: Config.serverId,
      url: Config.serverUrl,
      username: SERVER_USER,
      password: SERVER_PASSWORD
    )
  }

  // It is important to call rtBuildInfo before any other steps
  // (e.g. rtUpload, etc.)
  rtBuildInfo(
    captureEnv: true
  )
}


// Returns true if a tag for a Docker image already exists.
def isDockerTagAvailable(Map argConfig)
{
  def Image = argConfig.image
  def Repository = argConfig.repository
  def Server = Config.serverUrl
  def Request = "${Server}/api/docker/${Repository}/v2/${Image}/tags/list"

  def AllTags = httpRequest authentication: Config.credentialsId,
    url: Request,
    acceptType: 'APPLICATION_JSON',
    httpMode: 'GET',
    // Accept a failing request (e.g. if an artifact is not available)
    validResponseCodes: '200, 404',
    consoleLogResponseBody: true

  if (AllTags.status != 200) {
    return false
  }

  // If one or more tags are found, then the JSON looks as follows:
  // {
  //   "name" : "path/to/image",
  //   "tags" : [ "1.0.0", "2.0.0" ]
  // }
  def Json = readJSON(text: AllTags.content)?.tags
  return Json?.any({ it == argConfig.tag })
}


// Returns true if an artifact with the given version already exists.
def isVersionAvailable(Map argConfig)
{
  // The idea of this method is as follows:
  // - Collect all versions of the artifact
  // - Check if a version matches argConfig.version (no matter if it is an
  //   integration version or not)
  //
  // Benefit of this approach:
  // - The method may be used to check for Bugfix releases (not possible if
  //   latestVersion API request would have been used)
  // - Changed file extensions or classifiers do not influence the request (not
  //   possible if the existence of a concrete artifact would have been checked
  //   by its name)

  def Group = argConfig.project
  def Artifact = argConfig.module
  def Repository = argConfig.repository
  def VersionUrl = "${Config.serverUrl}/api/search/versions"
  def Request = "${VersionUrl}?g=${Group}&a=${Artifact}&repos=${Repository}"

  def AllVersions = httpRequest authentication: Config.credentialsId,
    url: Request,
    acceptType: 'APPLICATION_JSON',
    httpMode: 'GET',
    // Accept a failing request (e.g. if an artifact is not available)
    validResponseCodes: '200, 404',
    consoleLogResponseBody: true

  if (AllVersions.status != 200) {
    return false
  }

  // If one or more versions are found, then the JSON looks as follows:
  // { "results" : [
  //   { "version" : "1.0", "integration" : false }
  // ] }
  def Json = readJSON(text: AllVersions.content)?.results
  return Json?.any({ it.version == argConfig.version })
}


// Uploads files to the Artifactory server.
//
// A list of default properties may be defined. The default properties will
// be added automatically to all artifacts that do not provide explicit
// properties.
def uploadFiles(Map argConfig)
{
  def DefaultProps = getPropertiesAsFileSpecString(argConfig.defaultProps)

  // Add missing attributes for File Spec
  argConfig.fileSpec?.each {
    addTargetPathToFileSpec(it,
      argConfig.repository,
      argConfig.project,
      argConfig.module,
      argConfig.classifier,
      argConfig.baseRevision
    )
    addDefaultPropertiesToFileSpec(it, DefaultProps)
    normalizeValuesOfFileSpec(it)
  }

  // Use JSON pipeline step as all other solutions require explicit script
  // permission (which affects portability to other Jenkins installations)
  def FilesSpec = readJSON text: '{ "files": [] }'
  FilesSpec['files'] = argConfig.fileSpec

  def JsonOutput = groovy.json.JsonOutput.prettyPrint(FilesSpec.toString())

  // Print JSON for debugging purpose
  echo "JSON:\n${JsonOutput}"

  rtUpload (
    serverId: Config.serverId,
    spec: JsonOutput
  )
}


// Publishes the current build on the Artifactory server.
def publishBuild()
{
  rtPublishBuildInfo (
    serverId: Config.serverId
  )
}

//endregion


//region Private methods

// Gets the path on the Artifactory server where the build artifacts shall be
// stored.
def private getPublishFolder(String argRepo, String argProject,
  String argModule, String argVersion)
{
  def Repo = argRepo
  def Group = argProject.replace('.', '/')
  def Module = argModule
  def FolderVersion = mavenExt.getFolderVersion(
    baseRevision: argVersion,
    release: Config.release
  )

  return "${Repo}/${Group}/${Module}/${FolderVersion}"
}


// Gets the base name of the build artifact.
//
// The base name does not include the file extension and the concrete
// classifier (however, if a general classifier is defined, then this
// classifier will be part of the base name).
def private getPublishBaseFileName(String argModule, String argClassifier,
  argVersion)
{
  def Module = argModule
  def AddInfo = argClassifier ? '-' + argClassifier : ''
  def FileVersion = mavenExt.getFileVersion(
    baseRevision: argVersion,
    release: Config.release
  )

  return "${Module}-${FileVersion}${AddInfo}"
}


// Converts a map into a valid property string that fits the file specification
// for the rtUpload function of the Artifactory plugin.
def private getPropertiesAsFileSpecString(Map argProperties)
{
  def DefaultProps = argProperties?.collect {
    // Join the value by using a comma as delimiter if it is a list
    def Value = it.value
    if (it.value instanceof List) {
      Value = it.value.join(',')
    }

    return "${it.key}=${Value}"
  }?.join(';')

  return DefaultProps
}


// Calculates the mandatory 'target' property for a file specification if it
// is not set already.
//
// The 'target' will be set to '<folder><baseName><classifier><extension>':
// - folder = Automatically calculated by using the project, module and version
//            information
// - baseName = Automatically calculated by using the module, classifier and
//              version information
// - classifier = Empty if not set explicitly in the map (will be finally
//                removed from the file specification as it is our own
//                extension and not officially supported)
// - extension = Automatically calculated by checking the pattern in the file
//               specification for an extension like '.tar.*', *.* or ''.
def private addTargetPathToFileSpec(Map argFileSpec, String argRepo,
  String argProject, String argModule, String argClassifier, String argVersion)
{
  if (argFileSpec.target) {
    return
  }

  // Extract the file extension from the mandatory pattern and accept '.tar.*',
  // '.*' and ''
  def Extension =
    (argFileSpec.pattern =~ /(?:\.tar\.[^\.\/]+|\.[^\.\/]+|)$/)[0]

  // Check if a classifier is set. 'classifier' is our own attribute that is
  // not supported by the rtUpload plugin, so we need to remove it.
  def Classifier = argFileSpec.classifier?.trim() ?
    "-${argFileSpec.classifier}" :
    ''
  argFileSpec.remove('classifier')

  // Build the Maven like target path

  def FolderName = getPublishFolder(argRepo, argProject, argModule, argVersion)
  def FileBaseName = getPublishBaseFileName(argModule, argClassifier,
    argVersion)
  def FileName = "${FileBaseName}${Classifier}${Extension}"

  argFileSpec['target'] = "${FolderName}/${FileName}"
}


// Add the given default properties to the file specification if 'props' is
// not set already.
def private addDefaultPropertiesToFileSpec(Map argFileSpec,
  String argProperties)
{
  if (!argFileSpec.containsKey('props') && argProperties) {
    argFileSpec['props'] = argProperties
  }
}


// Convert map elements like GStrings to Java strings in order to prevent
// that the full class structure of those elements will be added to a JSON
// output.
def private normalizeValuesOfFileSpec(Map argFileSpec)
{
  argFileSpec.each {
    argFileSpec[it.key] = it.value.toString()
  }
}

//endregion
