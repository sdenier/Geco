Geco - Development Guidelines
=============================

Tools
-----

- Eclipse IDE (for Java Developers) - pick your favorite Java IDE
- Ant for release build
- Git 1.7+ for source control
- Webgen for user documentation/website

Project
-------

### Language/SDK: Java SE 6

- Use POJO (Plain Old Java Object) so that anyone with basic Java knowledge can read the code
- Swing UI

### Libraries

Used only when necessary, with a small footprint. I prefer to use a custom built solution than reusing a big library doing too many things in a cumbersome way (for a simple app like Geco). Also it has to be license-compatible (prefer MIT, Apache licences...) to avoid licensing hell (Geco is currently GPLed v2 just because of one file).

- SIReader.jar (include RXTX) for SPORTident communication
- icu4j-charsetdetector (a small subset of the Unicode icu4j project)
- JUnit 4 for tests only
- mockito 1.8 for tests only

### Architecture

Geco strives to follow a classic MVC architecture:

- Models `net.geco.models` know nothing except other models
- Controls (actually, more like services) `net.geco.controls` know nothing except models and other controls
- UI `net.geco.ui` can read models and access controls, and should only change models through dedicated controls.

In practice, they are more detailed architectural rules, but there are not strictly enforced (only reviewed from time to time through reverse engineering).

Some entry points:

- `Registry` as the data store for all models
- `Geco` as the main class, which startups everything and links together the UI (`GecoWindow`) and services
- `GecoControl` as the master entity service-side, which provides access to the current `Stage` (and its `Registry`) as well as to other controls
- `GecoWindow` as the master entity UI-side, which setups the window and panels
- You can also check `net.geco.app.*` classes to see the different configurations for apps (classic, free order, orient'show...)

### Good Practices

I strongly recommend practicing TDD (Test-Driven Development) for any contribution. It will help the design, integration, reuse, and maintenance in the long term.

Geco is not perfect from this point of view. I started Geco before I regularly practiced TDD, and there are still some places where I retrospectively feel the lack of tests and not-so-good design.

TDD also helps to separate things between services (controls) and UI: test-develop your services, then plug the UI on top. The UI layer should be mostly setup/layout, so that testing is trivial and can be handmade (in other words: no business behavior in UI). If you feel uneasy because you can't write a test for some complex behaviour, you're doing it wrong!

> Writing UI tests is hard, especially in an application which was not TDD from the start (like Geco). If you want to do this, take a look at Fest for Swing testing. A good option may be to use a BDD library (like JBehave) with UI testing, as UI tests tend to be cluttered with technicalities which make them hard to understand.


User Documentation
------------------

The source for user documentation is in the `doc/user` directory. It comes as a bunch of markdown files, which are processed with the `webgen` tool to generate html pages. The website simply mirrors the documentation of the latest release.

Please, don't forget that writing code is only part of the job. Writing doc for the end user is equally important.


Workflows
---------

### Development

If you want to add a feature/change some code, just follow the [fork/pull](https://help.github.com/articles/using-pull-requests) workflow from GitHub.

- fork the project
- make your change/develop your feature
- trigger a pull request
  - if you are knowledgeable about `git rebase`, please clean your history
- enter the review process (code critics & updates)
- when it is validated as *done*, your feature is merged in the main repository.

> To be done, remember that code is only part of the solution. Don't forget user documentation and internationalization (english at least).

### Continuous Release

Geco targets the [GitHub flow](http://scottchacon.com/2011/08/31/github-flow.html):

- `master` branch is always releasable: one can checkout `master` and build at anytime an intermediate release - this implies any push to `master` contains a complete feature (or bug fixes otherwise)
- stable releases (i.e., the archive bundle with runnable jar, doc...) are built and tagged from time to time from `master`
- development happens in parallel branches, which are merged into master when finished (for long-term development, there might be special 'release' branches)
- any push to the repository triggers a build on [Travis CI](https://travis-ci.org/sdenier/Geco), which compiles the code and launches the test.

The goal is to have a fast release cycle, based on small increments: each new feature pushed to master can produce a development release. When a set of new features in master has been well tested and is deemed robust, master is also tagged as a stable release and a new bundle is built.

A future goal is to have a true 'continuous release' workflow with Travis: a successful build would also create the bundle and make it available for download.

### Create a release bundle

The file `ant.xml` contains targets for the Ant build system, especially to create a bundle for release:

- `userHelp` regenerates the `help` folder with user documentation
- `build_dev_jar` builds the jar for a development release. They are named incrementally after the last stable release, like `geco-1.3-5-fd32k43.jar` (fifth release after release tagged 1.3)
- `build_release_jar` builds the jar for a stable release. The only difference is in the given name, which is simpler, like `geco-1.3.jar`
- `build_distrib` builds the archive bundle for a stable release, including the jar, the user documentation and other files.

> The `build.xml` file contains targets for continuous integration with Travis.