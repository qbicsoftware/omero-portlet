omero-portlet
-----------------------------------

.. image:: https://github.com/qbicsoftware/omero-portlet/workflows/Build%20Maven%20Package/badge.svg
    :target: https://github.com/qbicsoftware/omero-portlet/workflows/Build%20Maven%20Package/badge.svg
    :alt: Github Workflow Build Maven Package Status

.. image:: https://github.com/qbicsoftware/omero-portlet/workflows/Run%20Maven%20Tests/badge.svg
    :target: https://github.com/qbicsoftware/omero-portlet/workflows/Run%20Maven%20Tests/badge.svg
    :alt: Github Workflow Tests Status

.. image:: https://github.com/qbicsoftware/omero-portlet/workflows/QUBE%20lint/badge.svg
    :target: https://github.com/qbicsoftware/omero-portlet/workflows/QUBE%20lint/badge.svg
    :alt: QUBE Lint Status

.. image:: https://img.shields.io/travis/qbicsoftware/omero-portlet.svg
    :target: https://travis-ci.org/qbicsoftware/omero-portlet
    :alt: Travis CI Status

.. image:: https://readthedocs.org/projects/omero-portlet/badge/?version=latest
    :target: https://omero-portlet.readthedocs.io/en/latest/?badge=latest
    :alt: Documentation Status

.. image:: https://flat.badgen.net/dependabot/thepracticaldev/dev.to?icon=dependabot
    :target: https://flat.badgen.net/dependabot/thepracticaldev/dev.to?icon=dependabot
    :alt: Dependabot Enabled


Omero-portlet. A imaging visualisation portlet for qPortal.

* Free software: MIT license
* Documentation: https://omero-portlet.readthedocs.io.

Features
--------

* TODO

Notes
--------

To run locally, avoiding the error:
javax.net.ssl.SSLHandshakeException: No appropriate protocol (protocol is disabled or cipher suites are inappropriate)

Run with the following cmd:
mvn jetty:run -Djava.security.properties="allow_tls.properties"

Credits
-------

This project was created with QUBE_.

.. _QUBE: https://github.com/qbicsoftware/qube
