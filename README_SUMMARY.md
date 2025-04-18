# README Summary for Lattis Legacy Repositories

## Android-manufacturing-test-app
### Excerpt from README.md:
# Android-manufacturing-test-app

This repository contains an Android application, likely used for manufacturing testing purposes, built with Gradle.

## Structure

- Standard Gradle project structure (`gradlew`, `build.gradle`, `settings.gradle`).
- The main application module is located in the `app/` directory.

## Purpose

---

## AndroidBLEManager
### Excerpt from README.md:
# AndroidBLEManager

This repository contains an Android project, likely focusing on Bluetooth Low Energy (BLE) management, built with Gradle.

## Structure

- Standard Gradle project structure (`gradlew`, `build.gradle`, `settings.gradle`).
- Contains an `app/` directory for the main application.
- Includes a directory named `ellipseblemanger/`, which might be a library module related to BLE for an Ellipse product.


---

## axalock_ios
### Excerpt from README.md:
# AXALock

A description of this package.

---

## BLEService
### Excerpt from README.md:
# BLEService

[![Swift](https://img.shields.io/badge/Swift-3.0-orange.svg)]()

Bluetooth connection manager framework for ellipse locks

## Install ##

Insert
`pod 'BLEService', :git => 'https://github.com/velolabs/BLEService.git'`

---

## bluf_backend
### Excerpt from README.md:
# bluf_backend

This repository contains a backend service, likely named "bluf_backend", built using TypeScript.

## Structure

- Contains `package.json` for Node.js dependencies and `tsconfig.json` for TypeScript configuration.
- Includes `ormconfig.json` and `ormconfig.ts`, indicating the use of the TypeORM library for database interaction.
- Source code likely resides in the `app/` directory.


---

## bluf
### Excerpt from README.md:
# bluf

This repository contains an Xcode project named "BLUF".

## Structure

- Contains `BLUF.xcodeproj`, the main Xcode project file.
- Code appears to be organized into platform-specific directories (`iOS/`, `macOS/`) and a `Shared/` directory.

## Purpose

---

## chimpy
### Excerpt from README.md:
# Project Name

Brief description of your project.

## Prerequisites

- Node.js (v18.0.0 or later)
- npm (v7.0.0 or later)

## Setup

---

## circle
### Excerpt from README.md:
# circle

This repository contains a Swift project named "circle", built using the Swift Package Manager.

## Structure

- Managed by Swift Package Manager (`Package.swift`, `Package.resolved`).
- Source code is in the `Sources/` directory.
- Includes tests in `Tests/`.
- Contains deployment configurations for AWS CodeDeploy (`appspec.yml`, `codeploy/`) and GitHub Actions (`.github/`).

---

## documentation
No README.md file found.

---

## ellipse_ios
### Excerpt from README.md:
# ellipse_ios

This repository contains the source code for the Ellipse iOS application.

## Structure

- Contains the Xcode project (`Ellipse.xcodeproj`) and workspace (`Ellipse.xcworkspace`).
- The main application code is likely within the `Ellipse/` subdirectory.
- Dependencies are managed using CocoaPods (`Podfile`).
- Includes a `fastlane/` directory for automation tasks.

---

## ellipse_lock_apple
### Excerpt from README.md:
# ellipse_lock_apple

A description of this package.

---

## Ellipse-Android-Bluetooth-SDK-Public
### Excerpt from README.md:
# Ellipse-Android-Bluetooth-SDK-Public



# Application usage:

## Authorization Token: 

Please enter the authorization token provided by Lattis.


---

## Ellipse-Android-Bluetooth-SDK
### Excerpt from README.md:
# Ellipse-Android-Bluetooth-SDK
Android SDK for Ellipse Bluetooth lock

---

## ellipse-ble-ios
### Excerpt from README.md:
# EllipseBLE

A description of this package.

---

## ellipselock_ios
### Excerpt from README.md:
# EllipseLock

A description of this package.

---

## GPSTracking
### Excerpt from README.md:
# GPSTracking
Handles trip GPS information

---

## jobs
### Excerpt from README.md:
# jobs
Repo to help manage cron jobs for other apps

- Cron jobs cause problems when apps run on multiple instances in AWS on on multiple cores using PM2. To fix this issue, this repo will purely be a utility repo to run on a single instance always and do light jobs. The main apps will still be responsible for handling the cron logic.

- For instance, the GPS service needs to ping the dashboard every 5 minutes to update GPS locations for out of ride vehicles. We run this using a cron job. Since the GPS service runs on a 12 core CPU, 12 duplicate requests are send and executed by the dashboard backend. If we have 3 instances of this with each running a 12 core CPU, 36 request will be made. If it's to update 100 vehicles, the updates go from 100 to 3*12*100=3600. We made 3500 extra DB writes with the same information.

- To solve the above issue, the jobs service will run the cron job and dispatch the request. This service should always run on a single instance and handle light jobs.

- If we need to update the dashboard with GPS data, the jobs service runs a cron job and triggers a HTTP request to the GPS service. This way the AWS load balancer will handle where the request will go. In case PM2 is involved, it's load balancer knows how to not duplicate request handling. A request will contain the name of the operation to be executed. For example, if the operation is update vehicle locations in the dashboard, we can send a request with parameters `{"operation": "update_dashboard_GPS_locations"}` and based on that, the GPS service can run the function that does this.

---

## keychain-swift
### Excerpt from README.md:
# Helper functions for storing text in Keychain for iOS, macOS, tvOS and WatchOS

[![Carthage compatible](https://img.shields.io/badge/Carthage-compatible-4BC51D.svg?style=flat)](https://github.com/Carthage/Carthage)
[![CocoaPods Version](https://img.shields.io/cocoapods/v/KeychainSwift.svg?style=flat)](http://cocoadocs.org/docsets/KeychainSwift)
[![Swift Package Manager compatible](https://img.shields.io/badge/Swift%20Package%20Manager-compatible-brightgreen.svg)](https://github.com/apple/swift-package-manager)
[![License](https://img.shields.io/cocoapods/l/KeychainSwift.svg?style=flat)](http://cocoadocs.org/docsets/KeychainSwift)
[![Platform](https://img.shields.io/cocoapods/p/KeychainSwift.svg?style=flat)](http://cocoadocs.org/docsets/KeychainSwift)

This is a collection of helper functions for saving text and data in the Keychain.
 As you probably noticed Apple's keychain API is a bit verbose. This library was designed to provide shorter syntax for accomplishing a simple task: reading/writing text values for specified keys:

---

## keymaster
### Excerpt from README.md:
# keymaster

This repository appears to host a Python backend service, likely named "keymaster".

## Structure

- Contains `requirements.txt` for Python dependencies.
- Includes an `application/` directory, `application.py`, and `config/` directory, common in Flask/Django projects.
- A `Procfile` and `.ebextensions/` suggest deployment configurations (e.g., Heroku, AWS Elastic Beanstalk).
- Includes `tests/` for automated tests and `locustfile.py` for load testing.

---

## lattis_android_v2
### Excerpt from README.md:
# lattis_android_v2

---

## lattis_android
### Excerpt from README.md:
# lattis_android

This repository contains the Android application project for "Lattis", built using Gradle.

## Structure

- Standard Gradle project structure (`gradlew`, `build.gradle`, `settings.gradle`).
- The main application module is in the `app/` directory.
- It appears to include the `Ellipse-Android-Bluetooth-SDK` as a submodule or related component (`.gitmodules` file is present).


---

## Lattis_elevate
No README.md file found.

---

## lattis_ios_v1
### Excerpt from README.md:
# lattis_ios_v1

This repository contains the source code for a version of the Lattis iOS application (potentially v1).

## Structure

- Contains the Xcode project (`Lattis.xcodeproj`) and workspace (`Lattis.xcworkspace`).
- Main application code is likely in the `Lattis/` directory, with shared code in `Shared/`.
- Includes a `Today/` directory (possibly for a Today Extension) and `Locations/`.
- Dependencies managed via CocoaPods (`Podfile`) and potentially Ruby gems (`Gemfile`).

---

## lattis_ios
### Excerpt from README.md:
# lattis_ios

This repository contains the source code for the Lattis iOS application, potentially including multiple related app targets or whitelabeled versions.

## Structure

- Contains the main Xcode project (`Lattis.xcodeproj`) and workspace (`Lattis.xcworkspace`).
- Includes numerous subdirectories (e.g., `Lattis/`, `Sandy Pedals/`, `Grin/`, `YRyde/`, `Shared/`, `LattisCore/`) which might represent different modules, targets, or versions of the application.
- Dependencies managed via CocoaPods (`Podfile`) and Ruby gems (`Gemfile`).
- Contains `fastlane/` for automation and `.github/` for GitHub Actions.

---

## lattis_sdk_ios_public
### Excerpt from README.md:
# Lattis SDK for iOS
![Xcode](https://img.shields.io/badge/Xcode-11-blue.svg) ![Swift](https://img.shields.io/badge/Swift-5.1-orange.svg) ![platform](https://img.shields.io/badge/platform-iOS%2010.0%2B-lightgrey.svg)
## Dependencies

* Oval.framework (Lattis networking API)
* [KeychainSwift](https://github.com/evgenyneu/keychain-swift.git)
* [CryptoSwift](https://github.com/krzyzanowskim/CryptoSwift.git)

## Installation


---

## lattis_sdk_ios
### Excerpt from README.md:
# lattis_sdk_ios

This repository contains the source code for the Lattis iOS SDK.

## Structure

- The core SDK code is likely in the `LattisSDK/` directory.
- Includes a demo application (`Lattis SDK Demo/`, with `.xcodeproj` and `.xcworkspace`).
- Configured for distribution via CocoaPods (`LattisSDK.podspec`, `Podfile`) and Swift Package Manager (`Package.swift`).
- Contains a `fastlane/` directory for automation.

---

## lattis-dashboard
### Excerpt from README.md:
# Lattis Dashbaord #

[Open the deployed app](https://fleet-dev.us-west-2.elasticbeanstalk.com/)

### INSTALLATION ###
Install all the dependencies with

```npm install```

### ENVIRONMENT SETUP ###

---

## lattis-datascience
### Excerpt from README.md:
# lattis-datascience
Repo for pricing and repositioning microservice



## Installing your python environment

On Ubuntu :
 - Install pip


---

## Lattis-O-M-iOS
### Excerpt from README.md:
# Lattis-O-M-iOS

This repository contains the source code for the Lattis O&M (Operations & Maintenance) iOS application.

## Structure

- Contains the Xcode project (`Lattis O&M.xcodeproj`) and workspace (`Lattis O&M.xcworkspace`).
- Main application code is likely in the `Lattis O&M/` directory.
- Dependencies managed via CocoaPods (`Podfile`) and Ruby gems (`Gemfile`, likely for Fastlane).
- Includes a `fastlane/` directory for automation.

---

## lattis-platform
### Excerpt from README.md:
# lattis-platform

This repository contains a Node.js backend application, likely the core "Lattis Platform".

## Structure

- Contains `package.json` for dependencies and `index.js` as a likely entry point.
- Organized into directories common for backend services: `models/`, `handlers/`, `db/`, `utils/`, `helpers/`, etc.
- Includes `config.js` for configuration settings.


---

## lemond-ios
### Excerpt from README.md:
# lemond-ios

This repository contains the source code for the LeMond iOS application.

## Structure

- Contains the Xcode project (`LeMond.xcodeproj`).
- Main application code is likely in the `LeMond/` directory.
- Includes directories for tests (`LeMondTests/`, `LeMondUITests/`).


---

## linus
### Excerpt from README.md:
# linus

This repository contains a Node.js project named "linus".

## Structure

- Contains `package.json` for dependencies.
- `app.js` is likely the main entry point.
- Additional code may be in the `source/` directory.


---

## lucy
### Excerpt from README.md:
# lucy

This repository contains a Python project named "lucy".

## Structure

- Includes `requirements.txt` for dependencies.
- Core files include `application.py` and `config.py`.
- Additional source code might be in the `source/` directory.


---

## MapboxStatic.swift
### Excerpt from README.md:
# MapboxStatic

[![CircleCI](https://circleci.com/gh/mapbox/MapboxStatic.swift.svg?style=svg)](https://circleci.com/gh/mapbox/MapboxStatic.swift)
[![Carthage compatible](https://img.shields.io/badge/Carthage-compatible-4BC51D.svg?style=flat)](https://github.com/Carthage/Carthage)
[![CocoaPods](https://img.shields.io/cocoapods/v/MapboxStatic.swift.svg)](http://cocoadocs.org/docsets/MapboxStatic.swift/)

MapboxStatic.swift makes it easy to connect your iOS, macOS, tvOS, or watchOS application to the [Mapbox Static Images API](https://docs.mapbox.com/api/maps/#static-images) or the [Legacy Static Images API](https://docs.mapbox.com/api/legacy/static-classic). Quickly generate a map snapshot – a static map image with overlays – by fetching it synchronously or asynchronously over the Web using first-class Swift or Objective-C data types.

A snapshot is a flattened PNG or JPEG image, ideal for use in a table or image view, user notification, sharing service, printed document, or anyplace else you’d like a quick, custom map without the overhead of an interactive view. A static map is created in a single HTTP request. Overlays are added server-side.


---

## mob-e-iot
### Excerpt from README.md:
# Node API Back End

The repo contains the back-end API code for the Mob-E.

## Getting Started

To run this project locally, follow these steps.

1. Clone the repo.


---

## mobility-dashboard
### Excerpt from README.md:
This is a [Next.js](https://nextjs.org/) project bootstrapped with [`create-next-app`](https://github.com/vercel/next.js/tree/canary/packages/create-next-app).

## Getting Started

First, run the development server:

```bash
npm run dev
# or
yarn dev

---

## operator-android
### Excerpt from README.md:
# operator-android

---

## operator-ios
### Excerpt from README.md:
# operator-ios

This repository contains the source code for the Operator application, targeting iOS and possibly macOS.

## Structure

- Contains the Xcode project (`Operator.xcodeproj`).
- Code is organized into `iOS/`, `macOS/`, and `Shared/` directories.
- Includes `fastlane/` for automation and `.github/` for GitHub Actions.
- Uses Ruby gems (`Gemfile`), likely for Fastlane.

---

## oval_ios
### Excerpt from README.md:
# oval_ios

This repository contains a Swift project named "Oval", likely intended for iOS.

## Structure

- Contains `Package.swift` for integration with the Swift Package Manager.
- Contains `Oval.podspec` for integration with CocoaPods.
- Source code is likely located in the `Source/` directory.
- An example project might be in `OvalExample/`.

---

## oval-ios
### Excerpt from README.md:
# oval-api

A description of this package.

---

## oval
### Excerpt from README.md:
# Oval - Lattis App and O&M app API

### INSTALLATION ###
Install all the dependencies with

```npm install```

### ENVIRONMENT SETUP ###
Add the necessary Environment variables on your machine


---

## philidelphia
### Excerpt from README.md:
# philidelphia

This repository contains a Node.js web application, likely built using the Express framework.

## Structure

- `package.json` lists project dependencies.
- `app.js` is likely the main application entry point.
- Directories like `routes/`, `views/`, and `public/` are common in Express applications.
- `gulpfile.js` indicates the use of Gulp for task automation.

---

## platform-packer
### Excerpt from README.md:
# platform-packer

This repository contains a Node.js application or script.

## Structure

- Contains `package.json` for managing dependencies.
- The main logic is likely within `app.js`.

## Purpose

---

## platform-swift
### Excerpt from README.md:
# platform-swift

A description of this package.

---

## QRCodeReader.swift
### Excerpt from README.md:
<p align="center">
  <img src="https://cloud.githubusercontent.com/assets/798235/19688388/c61a6ab8-9ac9-11e6-9757-e087c268f3a6.png" alt="QRCodeReader.swift">
</p>

<p align="center">
  <a href="http://cocoadocs.org/docsets/QRCodeReader.swift/"><img alt="Supported Platforms" src="https://cocoapod-badges.herokuapp.com/p/QRCodeReader.swift/badge.svg"/></a>
  <a href="http://cocoadocs.org/docsets/QRCodeReader.swift/"><img alt="Version" src="https://cocoapod-badges.herokuapp.com/v/QRCodeReader.swift/badge.svg"/></a>
</p>

**QRCodeReader.swift** is a simple code reader (initially only QRCode) for iOS in Swift. It is based on the `AVFoundation` framework from Apple in order to replace ZXing or ZBar for iOS 8.0 and over. It can decodes these [format types](https://developer.apple.com/library/ios/documentation/AVFoundation/Reference/AVMetadataMachineReadableCodeObject_Class/index.html#//apple_ref/doc/constant_group/Machine_Readable_Object_Types).

---

## quality_assurance
### Excerpt from README.md:
# quality_assurance

This repository contains a simple Node.js application or script.

## Structure

- Consists primarily of `package.json` (for dependencies) and `index.js` (likely the main script).

## Purpose


---

## RestService
### Excerpt from README.md:
# RestService

[![Swift](https://img.shields.io/badge/Swift-3.0-orange.svg)]()

REST API framework

## Install ##

Insert
`pod 'RestService', :git => 'https://github.com/velolabs/RestService.git'`

---

## sandy_pedals_android
### Excerpt from README.md:
# sandy_pedals_android

This repository contains the Android application project for "Sandy Pedals", built using Gradle.

## Structure

- Standard Gradle project structure (`gradlew`, `build.gradle`, `settings.gradle`).
- The main application module is in the `app/` directory.
- It appears to include the `Ellipse-Android-Bluetooth-SDK` as a submodule or related component (`.gitmodules` file is present).


---

## sandy_pedals_ios
### Excerpt from README.md:
# sandy_pedals_ios

This repository contains the source code for the Sandy Pedals iOS application.

## Structure

- Contains the Xcode project (`Lattis.xcodeproj`) and workspace (`Lattis.xcworkspace`). Note the project/workspace names might differ from the repo name.
- Main application code is likely in the `Lattis/` directory, with potentially shared code in `Shared/`.
- Includes a `Today/` directory, possibly for a Today Extension.
- Dependencies managed via CocoaPods (`Podfile`) and potentially Ruby gems (`Gemfile`, likely for Fastlane).

---

## sas-ble-apple
### Excerpt from README.md:
# sas-ble-apple

A description of this package.

---

## sas-ble-swift
### Excerpt from README.md:
# sas-ble-swift

A description of this package.

---

## SEBLEInterface
### Excerpt from README.md:
# SEBLEInterface

This repository appears to contain an Xcode project for an iOS or macOS library/framework.

## Structure

- Contains an `SEBLEInterface.xcodeproj` directory, indicating an Xcode project.
- Source code likely resides in the nested `SEBLEInterface/` directory.
- Includes `SEBLEInterfaceTests/` for unit tests.


---

## Skyfleet-Android
### Excerpt from README.md:
# Skyfleet-Android

This repository contains an Android application project, likely named "Skyfleet", built using Gradle.

## Structure

- Standard Gradle project structure (`gradlew`, `build.gradle`, `settings.gradle`).
- Contains an `app/` directory, likely the main application module.
- Includes a `Ranger/` directory, which might be a library module or a related component.


---

## Skylock_backend
### Excerpt from README.md:
# Skylock_backend

This repository appears to host the source code for the main Skylock backend service, likely built with Python.

## Structure

- Contains Python dependencies listed in `requirements.txt`.
- Includes an `application/` directory, `application.py`, and `config/` directory, suggesting a web framework like Flask or Django.
- A `Procfile` and `.ebextensions/` directory indicate deployment configurations, possibly for platforms like Heroku or AWS Elastic Beanstalk.
- Includes `tests/` directory for automated tests and `locustfile.py` for load testing.

---

## Skylock_beta_backend
### Excerpt from README.md:
# Skylock_beta_backend

This repository appears to contain the source code for a Python backend service, likely related to the Skylock product.

## Structure

- The project includes a `requirements.txt` file, indicating Python dependencies.
- Key files like `manage.py`, `application.py`, and directories like `app/` and `config/` suggest a web framework structure, potentially Flask or Django.
- A `Procfile` is present, often used for deployment instructions (e.g., on Heroku or similar platforms).
- Unit tests seem to be located in the `tests/` directory.

---

## skylock_ble
### Excerpt from README.md:
Skylock Firmware Project Fact Sheet
===================================

This document is a meant as a quick reference and guide to helping someone start to understand the different parts and pieces of the firmware project for the Skylock product.

##### Engineers:

- Jan worked on this from I don't know when until March 6, 2015. He had some basic functionality working of which some was used for the next implementation. His final work can be found in SKYLOCK\_JAN\_030615.ZIP in the "Archive" folder.
- John Bettendorff worked on this project from March 1, 2015 through ?.


---

## skylock_ios
### Excerpt from README.md:
# skylock_ios

This repository contains the source code for the Skylock iOS application.

## Structure

- The main Xcode project/workspace files (`Ellipse.xcodeproj`, `Ellipse.xcworkspace`) are located within the `Skylock/` subdirectory.
- Dependencies are managed using CocoaPods (`Podfile`).
- It includes a submodule or related code for `SEBLEInterface`.
- Contains setup instructions in the `readme` file.

---

## Skylock_test_Android
### Excerpt from README.md:
# Skylock_test_Android

This repository appears to contain an Android application project, likely developed using the older Eclipse ADT structure.

## Contents

- The main application code seems to reside within the `skylock_led/Skylock_LED/` directory.
- It includes standard Android project components like `AndroidManifest.xml`, `src`, `res`, and `libs`.

## Purpose

---

## Skylock-Android
### Excerpt from README.md:
# Skylock-Android

This repository contains an Android application project for Skylock, built using Gradle.

## Structure

- Standard Gradle project layout with the main application likely in `app/`.
- Includes build scripts (`build.gradle`, `settings.gradle`, `gradlew`).
- Presence of `library/` and `libs/` directories suggests it might contain or depend on custom libraries.


---

## Skylock-Test-App
### Excerpt from README.md:
# Skylock-Test-App

This repository contains an Android application project built using Gradle.

## Structure

- The project uses the standard Gradle structure, with the main application module located in the `app/` directory.
- Build scripts include `build.gradle`, `settings.gradle`, and the Gradle wrapper (`gradlew`).

## Purpose

---

## test-drive-actions
### Excerpt from README.md:
# test-drive-actions

Just trying
---

## velo-website
### Excerpt from README.md:
# velo-website

This repository appears to contain the source code for a Shopify theme.

## Structure

- The directory structure (`templates`, `snippets`, `layout`, `locales`, `config`, `assets`) matches the standard layout for Shopify themes.

## Purpose


---

## whitelabel
### Excerpt from README.md:
# whitelabel

A description of this package.

---

## Wrappers
### Excerpt from README.md:
# Wrappers

A description of this package.

---

