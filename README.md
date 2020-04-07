# Spelling Bee
A CLI to solve nytimes.com/puzzles/spelling-bee

## Installation
1. Clone spelling-bee repo in any directory `git clone git@github.com:eccentric-j/clap.git`
2. `cd` into spelling-bee
3. Run `npm install`

## Usage
Automatically solve puzzle and take a screenshot when the highest rank is achieved
```shell
npm start
```

List matching words
```shell
npx lumo -c src -m spelling-bee.words <charset>
```

Example:
```shell
clj lumo -c src -m spelling-bee.core "ohtcumn"
```

## Notes
- Assumes first letter of `charset` is the required character.
- Words list is a bit broader than the game typically allows.

## Credits
- Dictionary word list comes from https://github.com/dwyl/english-words
- Promise macros inspired by https://github.com/athos/kitchen-async
