# Info about the status of Logic Workbench User Interface (lwb-ui)

The project is not longer maintained. I recommend the [Logic Workbench GUI lwb-gui](https://github.com/esb-lwb/lwb-gui) as a simple graphical user interface to the [Logic Workbench](https://github.com/esb-lwb/lwb).

lwb-ui uses proto-repl and ink als plugins in the atom editor. Unfortunately it seems that proto-repl is not maintaindes anymore. The current version of proto-repl doesn't work with the newest version of ink.

That's the reason, why this project is deprecated.

-- 2020-01-15

# Logic Workbench User Interface (lwb-ui)

This is a small wrapper around the [Logic Workbench](https://github.com/esb-lwb/lwb).

## Usage information

Just execute the *Lwb Ui: Toggle* command from the Command Palette inside of atom.

## Development Setup

- first clone this repository
- then initialize the `plugin/` submodule using: `git submodule init` `git submodule update`

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
