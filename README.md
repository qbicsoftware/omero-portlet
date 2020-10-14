# OMERO visualisation portlet

[![Build Status](https://travis-ci.com/qbicsoftware/omero-portlet.svg?branch=master)](https://travis-ci.com/qbicsoftware/omero-portlet)
![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/qbicsoftware/omero-portlet)
[![Sonatype Nexus Releases](https://img.shields.io/nexus/r/life.qbic/omero-portlet?nexusVersion=3&server=https%3A%2F%2Fqbic-repo.qbic.uni-tuebingen.de%2F)](https://qbic-repo.qbic.uni-tuebingen.de/service/rest/repository/browse/maven-releases/life/qbic/omero-portlet/)
![Java Language](https://img.shields.io/badge/language-java-blue.svg)
[![License](https://img.shields.io/github/license/qbicsoftware/omero-portlet
)](https://travis-ci.com/qbicsoftware/omero-portlet)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.4068252.svg)](https://doi.org/10.5281/zenodo.4068252)

OMERO image visualisation portlet - A Vaadin app for visualising image data in qPortal.

## Author

Created by Luis Kuhn Cuellar (luis.kuhn@qbic.uni-tuebingen.de).

## Description

### Purpose of the portlet

//TODO write down what problems should be solved with this software



### Functionality

//TODO add screenshots of the deployed portlet

//TODO write down how this portlet solves the problems previousle stated in #purpose-of-the-portlet

### Implementation

//TODO write down implementation details (e.g. architecture)

## How to Install

* the imaging solution requires 3 main components
* liferay portal
* omero server
* openBIS instance

### Set up the portal

//TODO link to omero installation documentation or write it here yourself



This portlet requires you to have a portal running on Liferay and a biological data management system set up in openBIS.

For further instructions on the infrastructure setup please see https://portal.qbic.uni-tuebingen.de/portal/web/qbic/software#instructions

### ETL routine

Here at QBiC we use automated ETL routines to register data. These routines produce quality measures to ensure that the data uploaded meats our standard. Further information on the ETL routines can be found in our [open source ETL repository](https://github.com/qbicsoftware/etl-scripts#etl-openbis-dropboxes). 