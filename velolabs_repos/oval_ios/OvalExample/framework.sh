#!/bin/sh

mkdir -p .build

xcodebuild archive -scheme Oval -archivePath ".build/ios.xcarchive" -sdk iphoneos SKIP_INSTALL=NO

xcodebuild archive -scheme Oval -archivePath ".build/ios_sim.xcarchive" -sdk iphonesimulator SKIP_INSTALL=NO

xcodebuild -create-xcframework \
    -framework ".build/ios.xcarchive/Products/Library/Frameworks/Oval.framework" \
    -framework ".build/ios_sim.xcarchive/Products/Library/Frameworks/Oval.framework" \
    -output ".build/Oval.xcframework"

rm -rf .build/ios.xcarchive
rm -rf .build/ios_sim.xcarchive
