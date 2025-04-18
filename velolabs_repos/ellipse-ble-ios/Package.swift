// swift-tools-version:5.3
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "EllipseBLE",
    platforms: [
        .iOS(.v12),
        .watchOS(.v6),
        .macOS(.v10_13),
        .tvOS(.v12)
    ],
    products: [
        // Products define the executables and libraries a package produces, and make them visible to other packages.
        .library(
            name: "EllipseBLE",
            targets: ["EllipseBLE"]),
    ],
    dependencies: [
        // Dependencies declare other packages that this package depends on.
        // .package(url: /* package url */, from: "1.0.0"),
        .package(name: "KeychainSwift", url: "https://github.com/evgenyneu/keychain-swift.git", .branch("master")),
        .package(name: "oval-api", url: "https://github.com/velolabs/oval-ios.git", .branch("master"))
    ],
    targets: [
        // Targets are the basic building blocks of a package. A target can define a module or a test suite.
        // Targets can depend on other targets in this package, and on products in packages this package depends on.
        .target(
            name: "EllipseBLE",
            dependencies: [
                .product(name: "KeychainSwift", package: "KeychainSwift"),
                .product(name: "OvalAPI", package: "oval-api")
            ]),
        .testTarget(
            name: "EllipseBLETests",
            dependencies: ["EllipseBLE"]),
    ]
)
