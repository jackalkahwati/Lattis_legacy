//
//  Env.swift
//  Clip Lattis
//
//  Created by Ravil Khusainov on 26.01.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Foundation

import Foundation

public enum Env {
  // MARK: - Keys
    enum Keys {
        enum Plist {
            static let circleURL = "CIRCLE_ENDPOINT"
            static let ovalURL = "OVAL_BASE_URL"
        }
    }

    // MARK: - Plist
    private static let infoDictionary: [String: Any] = {
        guard let dict = Bundle.main.infoDictionary else {
            fatalError("Plist file not found")
        }
        return dict
    }()
    
    // MARK: - Plist values
    static let circleURL: URL = {
        guard let rootURLstring = Env.infoDictionary[Keys.Plist.circleURL] as? String else {
            fatalError("Root URL not set in plist for this environment")
        }
        guard let url = URL(string: rootURLstring) else {
            fatalError("Root URL is invalid")
        }
        return url
    }()
    
    static let ovalURL: URL = {
        guard let rootURLstring = Env.infoDictionary[Keys.Plist.ovalURL] as? String else {
            fatalError("Oval URL not set in plist for this environment")
        }
        guard let url = URL(string: rootURLstring) else {
            fatalError("Oval URL is invalid")
        }
        return url
    }()
}
