[![Build Status](https://travis-ci.org/openml/openml-weka.svg?branch=master)](https://travis-ci.org/openml/openml-weka)

The OpenmlWeka package

(New) Ant installation

(1) Download Weka from SVN, trunk folder
    https://svn.cms.waikato.ac.nz/svn/weka/trunk

(2) Put OpenmlWeka package folder in the following folder:
    <svnroot>/packages/external
    (Or use symlink, ln -s )

(3) Run from build file the <svnroot>/weka folder:
    a) ant (to make weka itself, which is a dependency)
    b) ant make_external -DpackageName=OpenmlWeka (build the package)

(4) Package available in OpenmlWeka folder.


(Old) Plain ant installation:

ant make_external -DpackageName=OpenmlWeka
