//
//  Environment.swift
//  Operator
//
//  Created by Ravil Khusainov on 25.03.2021.
//

import Foundation

public enum Env {
  // MARK: - Keys
    enum Keys {
        enum Plist {
            static let rootURL = "ROOT_URL"
            static let ovalURL = "OVAL_URL"
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
    static let rootURL: URL = {
        guard let rootURLstring = Env.infoDictionary[Keys.Plist.rootURL] as? String else {
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
