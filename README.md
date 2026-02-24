# CTNH-Lib

[![Build](https://github.com/CTNH-Team/CTNH-Lib/actions/workflows/build.yml/badge.svg?branch=dev)](https://github.com/CTNH-Team/CTNH-Lib/actions/workflows/build.yml)

Common library mod for core mods of modpack Create: New Horizon (CTNH).

## Building

This mod should be built under [CTNH-Team/CTNH-Modules](https://github.com/CTNH-Team/CTNH-Modules) repository using Gradle.

```shell
$ git clone --recursive https://github.com/CTNH-Team/CTNH-Modules.git 
$ cd CTNH-Modules   # And you may need to update the submodules manually
$ ./gradlew :modules:CTNH-Lib:build            # To build the mod .jar
$ ./gradlew :modules:CTNH-Lib:runData          # To generate data
$ ./gradlew :modules:CTNH-Lib:spotlessCheck    # To check code formatting
$ ...
```

Nightly builds are available on the [Actions](https://github.com/CTNH-Team/CTNH-Lib/actions/workflows/build.yml) page.

## License

All code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
