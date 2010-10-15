Geco readme file version 1.0
Copyright (c) 2008-2010 Simon Denier
Contact: http://www.simondenier.eu

Geco is a lightweight application for managing orienteering races.
It is written in Java and designed to be cross-platform (please report if you find platform issues).

Geco is especially geared towards the Orient'Show format:
- it provides advanced functions to explain MPs and count penalties (including butterfly miss);
- it manages knockout qualifications.

Geco UI is also designed to provide a lean user experience: navigate through stage workflow using the top tabs, and access data by direct manipulation.

----------
Install: unzip the archive file (should be done already if you can read this file).

Geco runs with Java version 6 and above. It might run with Java version 5.
You can download a JRE (Java Runtime Environment) from http://www.java.com

You need the SportIdent drivers to read SI chips.
– Windows: download available at http://www.sportident.com/
– Linux: recent kernels recognize the chip used by SI station, so it’s plug’n’play.
– Mac OS X: nothing out of the box, but it is easy to install some driver and get Mac OS to recognize the station (one line to hack). Contact me for the process.

Launch: double-click on the jar file.

----------
User documentation available under the help/ folder in html format.

If you are experienced with orienteering softwares, you can jump-start using the application without the doc.
Geco UI is designed to be very usable (there is still room for improvements): almost any actions available is visible, data accessible through direct manipulation, no hidden menus, no complicated workflow. 

----------
Directory structure
_
|- geco*.jar: application
|- readme.txt: this file
|- LICENSE: license info (source code)
|- gpl-2.0.txt: license info (application)
|- data/: folder holding multiple stage folders (stage folders can be in any folder)
|- data/sample_stages.prop: sample file describing a multi-stage event
|- data/templates/: sample stage files editable with a spreadsheet application 
|- help/: documentation in html format

----------
Many thanks to Julien Thézé, Martin Flynn, and Jannik Laval for their technical help and debugging sessions.

The Geco application is distributed under the GNU General Public License Version 2. See gpl-2.0.txt for details.

Original parts of this program are distributed under the MIT license. See LICENSE file for details.
Open-source code is available at http://bitbucket.org/sdenier/geco

JarClassLoader distributed under the GNU General Public License Version 2.
See http://www.jdotsoft.com/JarClassLoader.php

SIReader library kindly provided by Martin Flynn, many thanks to him!
Visit his software Òr on http://orienteering.ie/wiki/doku.php?id=or:index

SIReader uses the RXTX library, released under LGPL.
See website for details http://www.rxtx.org/

Icons come from the Crystal Project Icons, released under LGPL, designed by Everaldo Coelho.
See http://everaldo.com/crystal