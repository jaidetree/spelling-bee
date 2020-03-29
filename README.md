# Spelling Bee
A CLI to solve nytimes.com/puzzles/spelling-bee

## Installation
1. Clone spelling-bee repo in any directory `git clone git@github.com:eccentric-j/clap.git`
2. `cd` into spelling-bee

## Usage
```shell
clj -m spelling-bee.core <charset>
```

## Example
```shell
clj -m spelling-bee.core "ohtcumn"
```

## Notes
- Assumes first letter of `charset` is the required character.
- Words list is a bit broader than the game typically allows.

## Credits
Dictionary word list comes from https://github.com/dwyl/english-words
