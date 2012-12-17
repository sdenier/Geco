Geco 2.x (Roadmap)
==================
Copyright (c) 2008-2012 Simon Denier  
Homepage: http://geco.webou.net

[![Build Status](https://travis-ci.org/sdenier/Geco.png)](https://travis-ci.org/sdenier/Geco)

*This readme describes the big ideas driving Geco development for the next 2.x versions. Version 1.3 is the latest stable release of Geco. You can still see the [readme for 1.3](https://github.com/sdenier/Geco/blob/e0096fc139d390784f578dd6c4217fa20c099457/README.md) and download it [here](https://bitbucket.org/sdenier/geco/downloads).*

Geco is a lightweight application for managing orienteering races.
It is written in Java and designed to be cross-platform.

Geco comes with powerful features designed around its algorithm for automatic course detection: accurate trace, automatic entry before/after the race, race recreation from backup memory...

The Geco UI is designed to provide a lean user experience: navigate through stage workflow using the top tabs, and access data by direct manipulation.

Geco can handle multiple race formats: classic inline, free order, Orient'Show (penalty count, knockout qualifications).


Roadmap for Geco 2.x
--------------------

One big goal of the 2.x line is to offer more flexibility for different types of organisations, especially stages with multiple sections (think adventure racing). This involves building many small blocks as well as the framework to compose them:

- score events, free order, time limit, manual penalties and bonuses
- inline sections, butterfly/loop sections (where teams get to choose their own order), optional sections, "decision" controls (where teams can opt for different choices)
- multi-stage events & merging results from multiple stages (giving GecoPools a true UI)
- mass start, chasing start

Please note these are only speculative ideas, not definitive requirements!

If you are a developer and want to contribute, don't forget to check the dev [guidelines](https://github.com/sdenier/Geco/blob/master/README_DEV.md).


### Deep Infrastructure Changes (or, Internal Plumbing)

What also comes with a new major version? Big breaking changes. Some internal parts of Geco need to be replaced to support the oncoming features:

- the persistence layer should be replaced by an external library providing more flexibility. This means it should be easier to extend the data model whenever needed;
- the SportIdent library needs to be completely rewritten, in order to read station memories and the latest generations of SI-cards.


### Shorter Release Cycle

Geco development has moved to GitHub and runs some continuous integration on [Travis CI](http://travis-ci.org/sdenier/Geco). One goal is to have shorter release cycles: when a new feature is developed and delivered, then an intermediate release is built, without waiting for a full stable release.


### And Further down the Road...

The following is not a current goal for the 2.x line, and will only speak to people of the trade. One dream for Geco is to make it truly reliable and usable for big events. The underlying path would be innovative, at least for an orienteering event software!

- build a journalized log of changes, so that any instance of Geco can crash without losing data, even replay all changes for a given stage in another instance.
- support network replication across multiple instances of Geco: this would provide concurrent ecard downloads for example, but also master updates across all instances. Even in case of network crashes, each instance would continue to work and later synchronize. Each instance could play a different role in the network, supporting either ecard reading, data edition, race animation...


Install
-------

Unzip the archive file (should be done already if you can read this file).

Geco runs with Java version 6 and above. It might run with Java version 5.
You can download a JRE (Java Runtime Environment) from http://www.java.com

You need the SPORTIdent drivers to read SI cards.

- Windows: download available at http://www.sportident.com/
- Linux: recent kernels recognize the chip used by SI station, so it’s plug’n’play.
- Mac OS X: it's possible to install and tweak a USB driver to get Mac OS X to recognize the station. See the FAQ in the documentation.


Launch
------

Double-click the jar file.


User documentation
------------------

Available under the `help/` folder in html format.

If you are experienced with orienteering softwares, you can jump-start using the application without the doc. Geco UI is designed to be usable: almost any available action is visible, data accessible through direct manipulation, no hidden menus, no complicated workflow.


Directory structure
-------------------

- `geco*.jar` - application
- `README.md` - this file
- `data/template/` - sample stage files editable with a spreadsheet application
- `data/modeles/` - french models for course-category associations
- `help/` - documentation in html format
- `licences/LICENSE` - license info (source code)
- `licences/gpl-2.0.txt` - license info (application)
- `licences/icu-license.txt` - more license info


Contribution
------------
If you want to contribute to Geco (whatever your skills are), you can! Just read [that page](http://geco.webou.net/geco/contribute.html) to see what you can do.

If you specifically want to develop, go read the [dev guidelines](https://github.com/sdenier/Geco/blob/master/README_DEV.md).


Thanks
------

Many thanks to Julien Thézé, Martin Flynn, and Jannik Laval for their technical help and debugging sessions.


License Information
-------------------

The Geco application is distributed under the GNU General Public License Version 2. See gpl-2.0.txt for details.

Original parts of this program are distributed under the MIT license. See LICENSE file for details.
Open-source code is available at http://bitbucket.org/sdenier/geco

JarClassLoader distributed under the GNU General Public License Version 2.
See http://www.jdotsoft.com/JarClassLoader.php

SIReader library kindly provided by Martin Flynn, many thanks to him!
Visit his software Òr at http://orienteering.ie/wiki/doku.php?id=or:index

SIReader uses the RXTX library, released under LGPL v 2.1 + Linking Over Controlled Interface.
See website for details http://www.rxtx.org/

Geco uses a subset of the ICU4J library, released under ICU License 1.8.1. See http://site.icu-project.org/

Icons come from the Crystal Project Icons, released under LGPL, designed by Everaldo Coelho.
See http://everaldo.com/crystal