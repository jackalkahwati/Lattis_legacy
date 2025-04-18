# Lattis Legacy Repository Summary

This document provides a detailed summary of each repository in the Lattis Legacy codebase (located under the `velolabs_repos` directory), organized into two categories:

1. **Lattis Dashboard and Apps**: Repositories that are directly part of the Lattis product suite, including the dashboard, mobile applications, and associated SDKs/platforms.
2. **Other Repositories (Unrelated)**: Projects that serve different product lines, utilities, or experimental tools not directly integrated with the Lattis Dashboard and Apps.

---

## Lattis Dashboard and Apps

### lattis_ios
Primary Lattis iOS application. Contains the user interface and business logic for Lattis on iOS, built with Xcode and integrated with backend services.

### lattis_sdk_ios
An iOS SDK that provides integration libraries and common functionalities for Lattis applications, along with developer resources and documentation.

### lattis-platform
Core platform services for Lattis, offering essential APIs and backend functionality. Likely includes microservices components and infrastructure scripts.

### operator-ios
An iOS application designed for operator functionalities. It provides tools and interfaces for operational management, tightly integrated with the Lattis ecosystem.

### Lattis-O-M-iOS
A variant of the Lattis iOS application offering specialized features. Detailed functionality can be further understood by reviewing the project structure and code.

### lattis_ios_v1
A legacy version of the Lattis iOS application, preserving earlier implementations and design choices that serve as a historical reference.

### lattis_android
The Android application under the Lattis brand. Built with Gradle, it follows standard Android project practices and offers core functionality for Lattis users.

### operator-android
An Android application for operator functionalities. It parallels operator-ios in purpose and integrates with backend services for operational control.

### lattis_sdk_ios_public
A public version of the Lattis iOS SDK. It provides integration libraries, documentation, and tools for third-party developers to work with Lattis services.

### lattis-dashboard
A comprehensive dashboard application for managing Lattis services, combining both backend and frontend components to offer an administrative interface.

---

## Other Repositories (Unrelated)

### Android-manufacturing-test-app
An Android application intended for manufacturing testing purposes. It follows a standard Gradle structure with the primary application logic in the `app/` directory.

### AndroidBLEManager
An Android project focused on managing Bluetooth Low Energy (BLE) functionalities, likely serving Ellipse product integrations.

### axalock_ios
An iOS project (named "AXALock") developed for AXALock systems. The README provides only a brief description; further investigation is required for full details.

### BLEService
A Swift framework that manages Bluetooth connections for Ellipse lock systems. Distributed via CocoaPods with installation instructions in the README.

### bluf_backend
A backend service for the BLUF project, built with TypeScript on Node.js/Express. Includes configuration files like `package.json`, `tsconfig.json`, and TypeORM settings for database interactions.

### bluf
An Xcode project for the BLUF application, incorporating both iOS and macOS components organized into platform-specific and shared code.

### chimpy
A Node.js project following a basic template. The README outlines prerequisites and setup instructions, but the specific purpose of the project is not clearly defined.

### circle
A Swift project named "circle" managed by the Swift Package Manager. Contains source code, tests, and deployment configurations for AWS CodeDeploy and GitHub Actions, indicating CI/CD use.

### documentation
A repository presumed to contain documentation and reference materials for the overall codebase (no README.md is present).

### ellipse_ios
The Ellipse iOS application, built with Xcode. It includes an Xcode project and workspace, employs CocoaPods for dependency management, and utilizes Fastlane for automation tasks.

### ellipse_lock_apple
A project focused on implementing Ellipse lock systems on Apple devices. The README is minimal, and further details must be obtained by inspecting the source code.

### Ellipse-Android-Bluetooth-SDK-Public
A public Android Bluetooth SDK for Ellipse lock systems. The README provides usage instructions and notes on authorization tokens, indicating its design for third-party integrations.

### Ellipse-Android-Bluetooth-SDK
An Android SDK aimed at facilitating Bluetooth communication with Ellipse locks, enabling developers to integrate Ellipse Bluetooth features into their apps.

### ellipse-ble-ios
An iOS package, labeled "EllipseBLE," offering BLE-related functionalities for Ellipse products. The brief description suggests further review is needed for full context.

### ellipselock_ios
An iOS application focused on Ellipse lock systems. The overview is minimal, and additional details are gleaned from the source code.

### GPSTracking
A repository that processes GPS tracking data for trips, integrating with other services to provide comprehensive tracking information.

### jobs
A utility repository that manages cron jobs across the codebase by centralizing scheduling and dispatching of light tasks, helping prevent duplicate executions.

### quality_assurance
A collection of scripts and resources dedicated to quality assurance, including automated testing tools and utilities to help maintain code quality.

### lemond-ios
An iOS project with an unclear scope based on the README. Further inspection of the source code is required to determine its exact purpose.

### oval_ios
An iOS application for the Oval project, delivering the Oval brand experience on iOS.

### sandy_pedals_ios
An iOS application tailored for the Sandy Pedals project, focusing on features specific to that initiative.

### sandy_pedals_android
An Android application for the Sandy Pedals project, structured as a typical Android project with tailored business logic.

### Skyfleet-Android
An Android application developed for the Skyfleet project, offering features specific to the Skyfleet brand and built using standard practices.

### skylock_ios
An iOS project that delivers Skylock functionalities, providing dedicated user interfaces and integrations for the Skylock system.

### SEBLEInterface
A repository related to Bluetooth Low Energy (BLE) interfaces. It likely offers shared libraries or tools required by projects that implement BLE communications.

### linus
A repository with an unclear purpose, possibly serving as an internal tool or experimental project. The README provides limited details.

### lucy
A small utility or experimental project with limited documentation; its exact role remains ambiguous.

### platform-packer
A utility tool designed to package platform components, likely automating bundling and deployment processes within the ecosystem.

### keymaster
A project focused on key management and authentication services, offering mechanisms for secure credential storage and access control.

### philidelphia
A repository with minimal documentation, possibly a codename project. Further details require deeper code examination.

### Skylock_backend
Backend services for the Skylock system, likely developed with Node.js/Express. It handles core business logic and system integrations for Skylock features.

### velo-website
A frontend website project for the Lattis or Velo brand, built using modern web technologies (likely React) to provide a public-facing presence.

### Skylock-Android
An Android application that delivers Skylock functionalities, designed to provide mobile access to Skylock services.

### Skylock_beta_backend
A beta version of the Skylock backend services, used for testing and validating new features prior to production deployment.

### Skylock-Test-App
A test application used to verify and validate Skylock functionalities in a sandbox environment for QA and development purposes.

### Skylock_test_Android
An Android test application for Skylock, aimed at integration testing and quality assurance.

### skylock_ble
A repository dedicated to BLE features for Skylock, likely providing shared libraries and integration tools for cross-platform use.

### QRCodeReader.swift
A Swift-based project that implements QR code reading functionality, designed as either a standalone utility or a component within larger systems.

### RestService
A repository that implements a RESTful service, offering API endpoints consumed by various parts of the ecosystem in accordance with REST principles.

### MapboxStatic.swift
A Swift project for generating static maps using Mapbox, providing functionality to embed maps within applications.

### Lattis_elevate
A project focused on modernizing the Lattis platform by re-architecting components and integrating updated frameworks.

### keychain-swift
A Swift library for managing keychain access on iOS devices, offering secure methods for storing and retrieving sensitive data.

### oval-ios
Another iOS project for the Oval brand. It may differ in feature sets or design compared to the primary oval_ios repository.

### Wrappers
A collection of wrapper integrations that streamline interfacing with external services or libraries through abstraction layers.

### whitelabel
A codebase designed to support white-labeled versions of products, enabling customization of branding and features for different clients.

### lattis-datascience
A repository that includes data science projects, analytical tools, machine learning models, and data visualization resources related to Lattis.

### ellipse_lock_apple
A project focused on Ellipse lock systems for Apple devices, integrating hardware control with software management for lock functionalities.

### test-drive-actions
Automation scripts intended for simulating test drives, useful for performance testing and functional validation in automotive scenarios.

### sas-ble-apple
An Apple project that integrates SAS Bluetooth functionalities, providing BLE features specifically tailored for Apple devices.

### sas-ble-swift
A Swift project that incorporates SAS Bluetooth technology, offering BLE communication features for iOS development.

### platform-swift
A Swift-based project that delivers core platform features and likely underpins several other iOS applications within the ecosystem.

### oval
A repository associated with the Oval brand, with an unclear purpose that may require further exploration.

### lattis_android_v2
The second version of the Lattis Android application, representing an updated or redesigned implementation compared to the original.

### mob-e-iot
An IoT-focused project addressing mobility solutions by integrating hardware sensors and connected device functionalities.

### mobility-dashboard
A dashboard application targeted at visualizing mobility data and insights by aggregating key performance metrics.

*Note: This summary is based on the content of README files and selected source code inspections. For complete details, please refer to each repository's documentation or examine the code directly.*

_End of Summary_ 