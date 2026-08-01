# Novus Language Support for JetBrains IDEs

Language and toolchain support for the [Novus](https://github.com/MJDaws0n/Novus)
systems language and the [Nox](https://github.com/MJDaws0n/Nox) package manager.

The plugin depends only on the common IntelliJ Platform, so the same build is
intended for JetBrains IDEs based on platform 2026.2, including IntelliJ IDEA,
PhpStorm, PyCharm, WebStorm, GoLand, RubyMine, CLion, and DataGrip. It is
currently tested on PhpStorm 2026.2.

## Features

- Lexer and error-recovering parser matched to the upstream Novus compiler, including `#if`, globals, default parameters, selective/aliased/relative imports, arrays, floats, bitwise operators, compound assignment, and C-style `for` loops.
- Syntax and semantic highlighting, configurable colours, brace/quote matching, comments, folding, formatting, and a New Novus File template.
- Completion for language syntax, all current compiler intrinsics, visible locals/parameters/functions, direct imports, import aliases, imported members, and import paths.
- Go to declaration, rename, find usages, Go to Symbol, quick documentation, and Structure view.
- Persistent **Novus** run configurations. The Run action compiles and then launches the native binary; select **Build only** for a build configuration. Supports compiler flags, program arguments, working directory, and all compiler targets.
- Gutter Run icon on `fn main`, **Tools | Novus / Nox | Build Current Novus File**, and compiler output in the Run tool window.
- Nox actions for `init`, `pull`, `update`, `check`, and `list`, with project refresh after filesystem-changing commands.
- Configurable `novus` and `nox` executable paths under **Settings | Tools | Novus**.

## Requirements

- JDK 25
- Novus 0.2.x and Nox on `PATH` to use the build and package actions

## Build

```bash
./gradlew test buildPlugin
```

Gradle downloads IntelliJ IDEA 2026.2 as the build platform. To use an existing
local JetBrains IDE installation instead:

```bash
./gradlew -PlocalIdePath=/path/to/your/jetbrains-ide test buildPlugin
```

The installable ZIP is written to `build/distributions/`.

## Install

In a compatible JetBrains IDE, open **Settings | Plugins**, choose
**Install Plugin from Disk…**, select the ZIP from the release or
`build/distributions/`, and restart the IDE.

For development, `./gradlew runIde` opens a sandboxed IDE with the plugin
installed.

## Use

Open a Nox project (one containing `main.nov`, `libraries.conf`, and usually `lib/`). Open `main.nov` and click the gutter Run icon or use **Run | Run…**; a context-derived Novus configuration is created automatically. To cross-compile, edit the configuration and set Target to values such as `linux/amd64`, `linux/arm64`, `linux/386`, `darwin/arm64`, or `windows/amd64`.

Run Nox package operations from **Tools | Novus / Nox**. Configure the `novus`
and `nox` executable paths under **Settings | Tools | Novus** if they are not
detected on `PATH`.

## Language baseline

The implementation follows the upstream compiler lexer/parser and canonical language reference, not only the limited VS Code TextMate grammar:

- https://github.com/MJDaws0n/Novus
- https://github.com/MJDaws0n/Nox
