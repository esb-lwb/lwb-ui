# Logic Workbench User Interface (lwb-ui)

This is a small wrapper around the [Logic Workbench](https://github.com/esb-lwb/lwb).

## Usage information

Just execute the *Lwb Ui: Toggle* command from the Command Palette inside of atom.

## Development Setup

- first clone this repository
- then initialize the `plugin/` submodule using: `git submodule init` `git submodule update`
- install the dependencies: `npm install`

## Compiling and running

For development

```
npx shadow-cljs watch lwb-ui
```

To compile for release (`:simple` optimizations), use
```
npx shadow-cljs release lwb-ui
```

After you have done that, go into the `plugin/` folder and run for a local installation
in atom
```
apm install
apm link
```

lwb-ui should now be installed inside atom!

More about lwb-ui [Documentation](https://github.com/esb-lwb/lwb-ui/wiki)
