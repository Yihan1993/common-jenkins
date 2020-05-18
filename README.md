# Common Groovy scripts for Jenkins

## Content

This repository provides a [Shared Library](https://www.jenkins.io/doc/book/pipeline/shared-libraries/) for Jenkins and contains Groovy scripts to set up an automated build for the project *ProAI for Commercial Vehicles*.

## Directory layout

A Shared Library for Jenkins requires a [predetermined directory layout](https://www.jenkins.io/doc/book/pipeline/shared-libraries/#directory-structure) which cannot be changed.
Therefore the repository is structured as follows:

Directory    | Description                                           | Usage
---          | ---                                                   | ---
`\src`       | Storage of Groovy source files, e.g. helper classes   | Not used yet
`\vars`      | Storage of global functions and custom Pipeline steps | In use
`\resources` | Storage of resource files, e.g. a JSON file           | Not used yet

## Jenkins

### Configure the Shared Library

Proceed the following steps to configure the Shared Library in Jenkins:

- Navigate to `Jenkins > Manage Jenkins > Configure System`.

- Create a new `Global Pipeline Library` and set the attributes as follows:

  Attribute                                      | Value        | Description
  ---                                            | ---          | ---
  Name                                           | `proai-cv`   | Specifies the identifier for the Shared Library
  Default version                                | `master`     | Specifies the default version if no explicit version is selected (e.g. branch, tag, commit)
  Load implicity                                 | `false`      | Disables automatic access to the library without an explicit `@Library` declaration
  Allow default version to be overridden         | `true`       | Allows scripts to select a custom version
  Include @Library changes in job recent changes | `false`      | Do not list changes of the Shared Library in the changelog of the build
  Retrieval method                               | `Modern SCM` | Loads the library from a SCM plugin like Git

- Configure `Git` as `Source Code Management` and provide a valid url and valid credentials.

- Select the following `Behaviors` from the category `Within Repository`:

  - `Discover branches`
  - `Discover tags`

- Select the following `Behaviors` from the category `Additional`:

  - `Clean before checkout`
  - `Clean after checkout`

### Jenkins plugins

The following Jenkins plugins are used by the Shared Library:

Name                            | URL                                                           | Version | Description
---                             | ---                                                           | ---     | ---
Artifactory                     | https://plugins.jenkins.io/artifactory/                       | 3.6.1   | Adds Artifactory's Build Integration support to Jenkins
Bitbucket Branch Source Plugin  | https://plugins.jenkins.io/cloudbees-bitbucket-branch-source/ | 2.7.0   | Allows the use of Bitbucket Server as a multi-branch project source
Credentials Binding             | https://plugins.jenkins.io/credentials-binding/               | 1.21    | Allows credentials to be bound to environment variables
Email Extension Plugin          | https://plugins.jenkins.io/email-ext/                         | 2.68    | Extends Jenkins built-in email notification functionality
HTTP Request Plugin             | https://plugins.jenkins.io/http_request/                      | 1.8.26  | Adds support to send HTTP requests
Job and Node ownership plugin   | https://plugins.jenkins.io/ownership                          | 0.12.1  | Provides an ownership engine for Jenkins
Pipeline                        | https://plugins.jenkins.io/workflow-aggregator/               | 2.6     | Provides orchestration of automated builds
Pipeline Utility Steps          | https://plugins.jenkins.io/pipeline-utility-steps/            | 2.5.0   | Provides cross platform utility steps for Pipeline jobs

## Documentation

Once a build with the Shared Library was executed successfully the documentation of the Groovy scripts is available on the completed Jenkins job under `Pipeline Syntax > Global Variables Reference`.

## Contacts

This repository is maintained by [Markus Krötz](mailto:markus.kroetz@zf.com).
