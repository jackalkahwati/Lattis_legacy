// swift-tools-version:5.3
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "whitelabel",
    dependencies: [
        .package(name: "Plot", url: "https://github.com/johnsundell/plot.git", from: "0.9.0")
    ],
    targets: [
        // Targets are the basic building blocks of a package. A target can define a module or a test suite.
        // Targets can depend on other targets in this package, and on products in packages this package depends on.
        .target(
            name: "whitelabel",
            dependencies: ["Plot"]),
        .testTarget(
            name: "whitelabelTests",
            dependencies: ["whitelabel"]),
    ]
)
