fastlane documentation
================
# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```
xcode-select --install
```

Install _fastlane_ using
```
[sudo] gem install fastlane -NV
```
or alternatively using `brew cask install fastlane`

# Available Actions
## iOS
### ios fabric
```
fastlane ios fabric
```

### ios dev
```
fastlane ios dev
```
Publish Dev build
### ios beta
```
fastlane ios beta
```
Publish Beta build
### ios prod
```
fastlane ios prod
```

### ios deploy
```
fastlane ios deploy
```
Publish Dev, Beta and TestFlight (if prod is true)
### ios release
```
fastlane ios release
```
Create and push a new tag according to version number

----

This README.md is auto-generated and will be re-generated every time [fastlane](https://fastlane.tools) is run.
More information about fastlane can be found on [fastlane.tools](https://fastlane.tools).
The documentation of fastlane can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
