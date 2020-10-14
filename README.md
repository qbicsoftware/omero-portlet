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



## How to Install

The `omero-portlet` does provide a comprehensive user interface for scientists to interact with their imaging data. For the portlet to run three other main components are required. First the Liferay portal, this portal provides basic functionality for portlet deployment. The second dependency is the OMERO server itself. OMERO offers a platform to store and retrieve imaging data on request. This imaging solution is integrated in our infrastructure and connected to an openBIS data management instance using ETL routines. Information on the ETL routines can be found in section [ETL routine](#etl-routine). 

Instructions on setting up the Liferay portal, openBIS and OMERO up are located in section [Setting up the portal](#setting-up-the-portal).

### Setting up the portal

This portlet requires you to have a portal running on Liferay and a biological data management system set up in openBIS. In addition to the basic setup an OMERO server instance is required for this portlet. 

For further instructions on the infrastructure setup please refer to the [instructions on our portal](https://portal.qbic.uni-tuebingen.de/portal/web/qbic/software#instructions).

### ETL routine

Here at QBiC we use automated ETL routines to register data. These routines produce quality measures to ensure that the data uploaded meats our standard. Further information on the ETL routines can be found in our [open source ETL repository](https://github.com/qbicsoftware/etl-scripts#etl-openbis-dropboxes). 