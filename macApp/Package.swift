// swift-tools-version: 6.0

import PackageDescription

let package = Package(
    name: "WorkClockMac",
    platforms: [.macOS(.v14)],
    products: [
        .executable(name: "WorkClockMac", targets: ["WorkClockMac"])
    ],
    targets: [
        .binaryTarget(
            name: "WorkClockCore",
            path: "../core/data/build/XCFrameworks/debug/WorkClockCore.xcframework"
        ),
        .executableTarget(
            name: "WorkClockMac",
            dependencies: ["WorkClockCore"],
            path: "Sources"
        )
    ],
    swiftLanguageModes: [.v5]
)
