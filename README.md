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

The large volume of data produced by various omics disciplines (e.g. genomics, transcriptomics, proteomics, metabolomics), biomedical imaging techniques (e.g. X-ray CT and PET), and the unprecedented increase in spatial resolution achieved by conventional confocal and super-resolution light microscopy and modern electron microscopes present a challenge for long-term storage and management of these high-dimensional digital assets. Itt is of particular importance to employ rich metadata models that allow researchers to relate data from different disciplines, and design experiments using an integrative approach to handle both, multilayer omics, as well as biomedical imaging data. The integration of metadata from multi-omics experiments together with imaging metadata is an integral step in discovering new insights. 

This portlet provides users with an interface that facilitates retrieval of metadata, both from experimental design and associated images.



### Functionality

<img src="screenshot-1.1.0.png" alt="user interface of 1.1.0" style="zoom:80%;" />

This application provides easy access to imaging metadata and image visualization, using the *OMERO Client* component to query the OMERO server. This portlet also accesses OMERO.web functionality, in particular the 5D image viewers. 

For images that were uploaded in a proprietary format, this portlet enables the user to download images from the OMERO server  in the open [`OME-TIFF`](https://github.com/ome/ome-model/blob/master/docs/sphinx/ome-tiff/index.rst) format.

The problem of association with experimental metadata is solved by integration of imaging information with metadata stored in the openBIS instance. Thus enabeling scientists to associate image infromation with their projects.



## How to Install

The `omero-portlet` does provide a comprehensive user interface for scientists to interact with their imaging data. For the portlet to run three other main components are required. First the Liferay portal, this portal provides basic functionality for portlet deployment. The second dependency is the OMERO server itself. OMERO offers a platform to store and retrieve imaging data on request. This imaging solution is integrated in our infrastructure and connected to an openBIS data management instance using ETL routines. Information on the ETL routines can be found in section [ETL routine](#etl-routine). 

Instructions on setting up the Liferay portal, openBIS and OMERO are located in section [Setting up the portal](#setting-up-the-portal).

### Setting up the portal

This portlet requires you to have a portal running on Liferay and a biological data management system set up in openBIS. In addition to the basic setup an OMERO server instance is required for this portlet. 

For further instructions on the infrastructure setup please refer to the [instructions on our portal](https://portal.qbic.uni-tuebingen.de/portal/web/qbic/software#instructions).

### ETL routine

Here at QBiC we use automated ETL routines to register data. These routines produce quality measures to ensure that the data uploaded meets our standard. Further information on the ETL routines can be found in our [open source ETL repository](https://github.com/qbicsoftware/etl-scripts#etl-openbis-dropboxes). 
