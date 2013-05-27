Roadmap for Geco 2.x
====================

One big goal of the 2.x line is to offer more flexibility for different types of organisations, especially stages with multiple sections (think adventure racing). This involves building many small blocks as well as the framework to compose them:

- score events, free order, time limit, manual penalties and bonuses
- inline sections, butterfly/loop sections (where teams get to choose their own order), optional sections, "decision" controls (where teams can opt for different choices)
- multi-stage events & merging results from multiple stages (giving GecoPools a true UI)
- mass start, chasing start

Please note these are only speculative ideas, not definitive features!


Deep Infrastructure Changes (or, Internal Plumbing)
---------------------------------------------------

What also comes with a new major version? Big breaking changes. Some internal parts of Geco need to be replaced to support the oncoming features:

- the persistence layer should be replaced by an external library providing more flexibility. This means it should be easier to extend the data model whenever needed;
- the SportIdent library needs to be completely rewritten, in order to read station memories and the latest generations of SI-cards.


Shorter Release Cycle
---------------------

Geco development has moved to GitHub and runs some continuous integration on [Travis CI](http://travis-ci.org/sdenier/Geco). One goal is to have shorter release cycles: when a new feature is developed and delivered, then an intermediate release is built, without waiting for a full stable release.


And Further down the Road...
----------------------------

The following is not a current goal for the 2.x line, and will only speak to people of the trade. One dream for Geco is to make it truly reliable and usable for big events. The underlying path would be innovative, at least for an orienteering event software!

- build a journalized log of changes, so that any instance of Geco can crash without losing data, even replay all changes for a given stage in another instance.
- support network replication across multiple instances of Geco: this would provide concurrent ecard downloads for example, but also master updates across all instances. Even in case of network crashes, each instance would continue to work and later synchronize. Each instance could play a different role in the network, supporting either ecard reading, data edition, race animation...
