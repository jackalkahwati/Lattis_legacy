//
//  Analytics.swift
//  Lattis
//
//  Created by Ravil Khusainov on 03/10/2018.
//  Copyright © 2018 Velo Labs. All rights reserved.
//

import FirebaseCrashlytics

public final class Analytics {
    static let crashlitycs = Crashlytics.crashlytics()
    static public func report(_ error: Error, file: String = #file, line: Int = #line) {
//        let info: [String: Any] = [
//            "description": error.localizedDescription,
//            "file": file,
//            "line": line
//        ]
        crashlitycs.setCustomValue(error.localizedDescription, forKey: "description")
        crashlitycs.setCustomValue(file, forKey: "file")
        crashlitycs.setCustomValue(line, forKey: "line")
        crashlitycs.record(error: error)
    }
}
