// swift-tools-version:5.3
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

#if os(Linux)
let libraryType: PackageDescription.Product.Library.LibraryType = .dynamic
#else
let libraryType: PackageDescription.Product.Library.LibraryType = .static
#endif

let package = Package(
    name: "EllipseLock",
    platforms: [
        .iOS(.v10),
        .watchOS(.v4),
        .macOS(.v10_12),
        .tvOS(.v10)
    ],
    products: [
        .library(
            name: "OvalAPI",
            type: libraryType,
            targets: ["OvalAPI"]
        ),
        .library(
            name: "EllipseLock",
            type: libraryType,
            targets: ["EllipseLock"]
        )
    ],
    dependencies: [
        .package(name: "KeychainSwift", url: "https://github.com/evgenyneu/keychain-swift.git", .branch("master"))
    ],
    targets: [
        .target(
            name: "OvalAPI",
            exclude: ["LattisSDK"]
        ),
        .target(
            name: "EllipseLock",
            dependencies: ["OvalAPI", "KeychainSwift"],
            exclude: ["LattisSDK"]
        ),
    ]
)
