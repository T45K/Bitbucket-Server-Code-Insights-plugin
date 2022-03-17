# Bitbucket Server Code Insights plugin

[![Build Status](https://github.com/T45K/Bitbucket-Server-Code-Insights-plugin/actions/workflows/execute-test.yaml/badge.svg)](https://github.com/T45K/Bitbucket-Server-Code-Insights-plugin/actions/workflows/execute-test.yaml)
[![GitHub release](https://img.shields.io/github/v/release/T45K/Bitbucket-Server-Code-Insights-plugin?display_name=tag&include_prereleases)](https://github.com/T45K/Bitbucket-Server-Code-Insights-plugin/releases/latest)

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

|      Name      | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
|:--------------:|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `bitbucketUrl` | URL of your Bitbucket.                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
|   `project`    | Project name.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
|  `reportKey`   | Report Key of API (see [document](https://docs.atlassian.com/bitbucket-server/rest/7.21.0/bitbucket-code-insights-rest.html#:~:text=The%20report%20key%20should%20be%20a%20unique%20string%20chosen%20by%20the%20reporter%20and%20should%20be%20unique%20enough%20not%20to%20potentially%20clash%20with%20report%20keys%20from%20other%20reporters.%20We%20recommend%20using%20reverse%20DNS%20namespacing%20or%20a%20similar%20standard%20to%20ensure%20that%20collision%20is%20avoided.)). |
|   `username`   | Your username of Bitbucket.                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |  
|   `password`   | Your password of Bitbucket.                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |

### Local settings

Set up in your Job settings page of give in `Jenkinsfile` as parameter.

|         Name         | Description                                                                                            |
|:--------------------:|:-------------------------------------------------------------------------------------------------------|
|   `repositoryName`   | Your repository name.                                                                                  |                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
|      `srcPath`       | Source path of your repository (e.g., `src/main/java`).                                                |
|      `commitId`      | Commit Sha of target branch. You can use `env.GIT_COMMIT` as commitId in Multibranch pipeline project. |
|     `baseBranch`     | Base branch of your repository (e.g., `origin/main`). Default is `origin/master`. This is optional.    |  
| `checkstyleFilePath` | Relative path of Checkstyle result file from repository root (e.g., `target/checkstyle-result.xml`).   |

## Supporting tools

Currently, this plugin supports only Checkstyle.

The following tools are planned to be supported in the future.

- PMD
- SpotBugs
- SonarQube

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

