# Bitbucket Server Code Insights plugin

[![Build Status](https://github.com/T45K/Bitbucket-Server-Code-Insights-plugin/actions/workflows/execute-test.yaml/badge.svg)](https://github.com/T45K/Bitbucket-Server-Code-Insights-plugin/actions/workflows/execute-test.yaml)
[![GitHub release](https://img.shields.io/github/v/release/T45K/Bitbucket-Server-Code-Insights-plugin?display_name=tag&include_prereleases)](https://github.com/T45K/Bitbucket-Server-Code-Insights-plugin/releases/latest)
[![CodeFactor](https://www.codefactor.io/repository/github/t45k/bitbucket-server-code-insights-plugin/badge)](https://www.codefactor.io/repository/github/t45k/bitbucket-server-code-insights-plugin)
[![codecov](https://codecov.io/gh/T45K/Bitbucket-Server-Code-Insights-plugin/branch/master/graph/badge.svg?token=WMB09M8P7R)](https://codecov.io/gh/T45K/Bitbucket-Server-Code-Insights-plugin)
[![CodeQL](https://github.com/T45K/Bitbucket-Server-Code-Insights-plugin/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/T45K/Bitbucket-Server-Code-Insights-plugin/actions/workflows/codeql-analysis.yml)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/T45K/Bitbucket-Server-Code-Insights-plugin.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/T45K/Bitbucket-Server-Code-Insights-plugin/alerts/)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/T45K/Bitbucket-Server-Code-Insights-plugin.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/T45K/Bitbucket-Server-Code-Insights-plugin/context:java)

## Introduction

This plugin enables Jenkins to call Bitbucket Server Code Insights API.

## What's Code Insights API?

https://docs.atlassian.com/bitbucket-server/rest/7.21.0/bitbucket-code-insights-rest.html

## Getting started

Building this plugin requires Java 8+.

1. Clone this repository.
2. Move into the directory.
3. `./mvnw clean package`
4. Upload `target/codeInsights.hpi` on Jenkins.

## Settings

### Global settings

Set up in `your-jenkins/configure`.

|        Name         | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  | required |
|:-------------------:|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------:|
|   `bitbucketUrl`    | URL of your Bitbucket.                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |   Yes    |
|      `project`      | Project name.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |   Yes    |
|     `reportKey`     | Report Key of API (see [document](https://docs.atlassian.com/bitbucket-server/rest/7.21.0/bitbucket-code-insights-rest.html#:~:text=The%20report%20key%20should%20be%20a%20unique%20string%20chosen%20by%20the%20reporter%20and%20should%20be%20unique%20enough%20not%20to%20potentially%20clash%20with%20report%20keys%20from%20other%20reporters.%20We%20recommend%20using%20reverse%20DNS%20namespacing%20or%20a%20similar%20standard%20to%20ensure%20that%20collision%20is%20avoided.)). |   Yes    |
|     `username`      | Your username of Bitbucket.                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |   Yes    |  
|     `password`      | Your password of Bitbucket.                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |   Yes    |
|   `SonarQubeUrl`    | URL of your SonarQube Server.                                                                                                                                                                                                                                                                                                                                                                                                                                                                |    No    |
|  `SonarQubeToken`   | Token of your SonarQube account.                                                                                                                                                                                                                                                                                                                                                                                                                                                             |    No    |
| `SonarQubeUsername` | Username of your SonarQube account.                                                                                                                                                                                                                                                                                                                                                                                                                                                          |    No    |
| `SonarQubePassword` | Password of your SonarQube account.                                                                                                                                                                                                                                                                                                                                                                                                                                                          |    No    |

### Local settings

Set up in your Job settings page of give in `Jenkinsfile` as parameter.

|         Name          | Description                                                                                                                                           | required |     default     |
|:---------------------:|:------------------------------------------------------------------------------------------------------------------------------------------------------|:--------:|:---------------:|
|   `repositoryName`    | Your repository name.                                                                                                                                 |   Yes    |       `-`       |                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
|       `srcPath`       | Source path of your repository.                                                                                                                       |    No    | `src/main/java` |
|      `commitId`       | Commit Sha of target branch. You can use `env.GIT_COMMIT` as commitId in Jenkins Multibranch pipeline job.                                            |   Yes    |       `-`       | 
|     `baseBranch`      | Base branch of your repository (e.g., `origin/main`).                                                                                                 |    No    | `origin/master` | 
| `checkstyleFilePath`  | Relative path of Checkstyle result file from repository root (e.g., `target/checkstyle-result.xml`).                                                  |    No    |       `-`       |
| `spotBugsFilePath`    | Relative path of SpotBugs result file from repository root (e.g., `target/spotbugsXml.xml`).                                                          |    No    |       `-`       |  
| `SonarQubeProjectKey` | Project key of SonarQube project. If you use SonarQube plugin in Jenkins, specify the same value as `XXX` of `mvn sonar:sonar -Dsonar.projectKey=XXX` |    No    |       `-`       | 

## Supporting tools

Currently, this plugin supports the following tools.
- Checkstyle
- SpotBugs
- SonarQube

The following tools are planned to be supported in the future.

- PMD
- CodeQL

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

