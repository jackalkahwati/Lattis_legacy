// swift-tools-version:5.6
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

#if os(Linux)
let libraryType: PackageDescription.Product.Library.LibraryType = .dynamic
#else
let libraryType: PackageDescription.Product.Library.LibraryType = .static
#endif

let package = Package(
    name: "platform-swift",
    platforms: [
        .iOS(.v14),
        .watchOS(.v7),
        .macOS(.v11),
        .tvOS(.v14)
    ],
    products: [
        .library(
            name: "OvalBackend",
            type: libraryType,
            targets: ["OvalBackend"]),
        .library(
            name: "OvalModels",
            type: libraryType,
            targets: ["OvalModels"]),
        .library(
            name: "CircleBackend",
            type: libraryType,
            targets: ["CircleBackend"]),
    ],
    dependencies: [
        .package(url: "https://github.com/rawillk/http-client-swift", branch: "main")
    ],
    targets: [
        .target(
            name: "OvalModels",
            dependencies: []),
        .target(
            name: "OvalBackend",
            dependencies: [
                "OvalModels",
                .product(name: "HTTPClient", package: "http-client-swift")
            ]),
        .target(
            name: "CircleBackend",
            dependencies: [
                .product(name: "HTTPClient", package: "http-client-swift")
            ]),
        .testTarget(
            name: "OvalBackendTests",
            dependencies: ["OvalBackend"]),
    ]
)
