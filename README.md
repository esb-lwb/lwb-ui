# Logic Workbench User Interface (lwb-ui)

This is a small wrapper around the [Logic Workbench](https://github.com/esb-dev/lwb).

## Usage information

Just execute the *Lwb Ui: Toggle* command from the Command Palette inside of atom.

## Development Setup

- first clone this repository
- then initialize the `plugin/` submodule using: `git submodule update -b`

## Development

If you want to export a function to atom for use with atom commands, make sure to add them to `dev/build.clj` like so:
```
:foo 'lwb-ui.core/foo
:bar 'lwb-ui.core/bar
```

## Compiling and running

To compile me with a self-reloading loop, use:

```
lein run -m build/dev-repl
```

To compile me with a self-compiling loop but without live-reload:
```
lein run -m build/dev
```

To compile for release (`:simple` optimizations), use
```
lein run -m build/release
```

After you have done that, go into the `plugin/` folder and run
```
apm install
apm link
```

lwb-ui should now be installed inside atom!
