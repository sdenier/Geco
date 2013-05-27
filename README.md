Geco 2.1 beta
=============
Copyright (c) 2008-2013 Simon Denier  
Homepage: http://sdenier.github.io/Geco

[![Build Status](https://travis-ci.org/sdenier/Geco.png)](https://travis-ci.org/sdenier/Geco)

Geco is a lightweight application for managing orienteering races.
It is written in Java and designed to be cross-platform.

Geco comes with powerful features designed around its algorithm for automatic course detection: accurate trace, automatic entry before/after the race, race recreation from backup memory...

The Geco UI is designed to provide a lean user experience: navigate through stage workflow using the top tabs, and access data by direct manipulation.

Geco can handle multiple race formats: classic inline, free order, Orient'Show (penalty count, knockout qualifications).


About the 2.1 beta
------------------

Geco 2.1 is the second beta in the 2.x series. It provides a brand new feature which allows anyone to customize results outputs using the [mustache](http://mustache.github.io) template system. Customizable outputs include ranking, ranking with splits, tickets for runner...


Install & Launch
----------------

Unzip the archive file then double-click the jar file.

Geco runs with Java version 6 and above. You can download a JRE (Java Runtime Environment) from http://www.java.com

You need SPORTIdent drivers to read SI cards.

- Windows: download available at http://www.sportident.com/
- Linux: recent kernels recognize the chip used by SI station, so it’s plug’n’play.
- Mac OS X: read the [FAQ](http://sdenier.github.io/Geco/begin/faq.html).


User documentation
------------------

Available under the `help/` folder in html format.

If you are experienced with orienteering softwares, you can jump-start using the application without the doc. Geco UI is designed to be usable: almost any available action is visible, data accessible through direct manipulation, no hidden menus, no complicated workflow.


Directory structure for Geco 2 releases
---------------------------------------

- `geco*.jar` - application
- `data/template/` - sample files for various imports (archive, startlist...)
- `data/modeles/` - french models for course-category associations
- `formats/` - mustache templates for results (ranking, splits, ticket...)
- `help/` - documentation in html format
- `licences/` - license files


Contribution
------------
If you want to contribute to Geco (whatever your skills are), you can! Just read [that page](http://sdenier.github.com/Geco/begin/contribute.html) to see what you can do.

If you specifically want to develop, go read the [dev guidelines](https://github.com/sdenier/Geco/blob/master/README_DEV.md).


License Information
-------------------

The Geco application is released under the GNU General Public License Version 2. See gpl-2.0.txt for details.

Original parts of this program are distributed under the MIT license. See LICENSE file for details. Open-source code available at http://github.com/sdenier/Geco

GecoSI library released under the MIT license. See http://github.com/sdenier/GecoSI for full details. GecoSI uses the [RXTX](http://www.rxtx.org/) library, released under LGPL v 2.1 + Linking Over Controlled Interface.

Other libraries used by Geco:

- [JarClassLoader](http://www.jdotsoft.com/JarClassLoader.php) (GNU General Public License Version 2)
- [Json in Java](http://www.json.org/java/) (subset)
- [Jackson](http://wiki.fasterxml.com/JacksonHome) (Apache License 2.0)
- [ICU4J](http://site.icu-project.org/) (subset, ICU License 1.8.1)
- [JMustache](http://github.com/samskivert/jmustache)

Icons come from the [Crystal Project Icons](http://everaldo.com/crystal) (LGPL) by Everaldo Coelho.
