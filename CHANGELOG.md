# Geco Changelog

## Version 2.3 (2014/11/xx)

- Push GecoSI 1.2.0 (pCard support)
- More reliable 32 vs 64 bits detection to use the correct SPORTIdent driver
- Re-enable the multi-section race format (previously Raid Orient'Alpin), fully compatible with other Geco features (course detection, trace display)
- Multi-sections: support in-order sections mixing inline and free order controls
- Multi-sections: automatically detect section limits when analyzing the trace (ability to manually set section limits when detection goes wrong)
- Multi-sections: ability to neutralize a section (subtract section time from race time)
- Multi-sections: each control can have a custom time penalty
- Multi-sections: ability to manually set a time bonus/malus for each entry
- Multi-sections: custom output formats for section splits and results
- Orientshow: count "extra" controls in trace and add a time penalty for such controls
- Training mode: can choose between copy first or archive insertion for duplicate entries
- Training mode: fix bug with duplicate not triggering split printing automatically
- Results: merge options to export Splits/O'Splits into a single option + updated splits template
- Display a brief summary of stage stats in status bar
- Merge dialog: start id can also be searched (yannisgu)
- Wording: rename "Functions" as "Operations" in the Log/Operations tab

## Version 2.2 (2014/02/06)

- Push GecoSI 1.1.0 (faster and more reliable readout for Si6/10/11, fix bug with empty ecards)
- Introduce course sets, which define group of related courses (for example, variations in a butterfly course)
- Enable results by course sets (for example, to rank runners in a mass start with course variations)
- Enable course detection restricted by runner category, based on course sets
- True mass start time can be set up for each course
- Course length & climb can be viewed and edited from the course configuration panel
- Course code are immediately displayed next to the course (not yet editable)
- Time are now displayed in 24h00+ format, not in modulo 24h00 format (for race longer than one day)
- Runner panel displays more details about time computation (start time, before/after neutralization)
- One-click export for O'Splits
- More robust parsing for station log and ecard log (trim field quotes, skip bad record)
- Raise popup to better inform user when the master station notifies a fatal error
- Enable page format setup for multi-columns ticket

## Version 2.1 (2013/09/13)

- Push GecoSI 1.0.0 stable release
- Use Mustache as a versatile export engine for results (ranking, splits, O'Splits...)
- Basic startlist export
- Let user choose the path for result auto-export
- Focus on runner after a manual merge

## Version 2.0 beta1 (2013/05/01)

- Use new GecoSI library to read all SPORTident cards si5 through si11
- Refresh serial ports on pop-up (no need to restart Geco to see serial ports)
- Make Leg Neutralization available in Inline and Orient'Show configs

## Version 2.0 alpha (early 2013)

- Replace CSV persistence with JSON persistence
- Import OE CSV format with custom course fields
- Export heats startlist as custom OE CSV format
- Rename Live mode as Finish mode
