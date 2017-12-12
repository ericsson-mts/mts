# Introduction

This directory contains documentation generation instructions and tools.


# Schemas documentation generation workflow

The schemas documentation should be re-generated only when the schemas changes.

The process is not automated :

1. convert schemas from *mts/src/main/conf/schemas* to *mts/src/main/doc/schemas* using the *generate_xsd* script
2. use XmlSpy (under windows), open each schema from *mts/src/main/doc/schemas* and export it to html ( *Schema design* -> *Generate documentation*)
3. compress the *mts/src/main/doc/schemas* directory, put the archive in *mts/src/main/conf/schemas.zip
4. delete the *mts/src/main/doc/schemas* directory
5. commit the archive

# *generate_xsd* script

An ant file is used to generate XmlSpy compatible schema files from the mts schemas

``` shell
ant clean generate_xsd
```

