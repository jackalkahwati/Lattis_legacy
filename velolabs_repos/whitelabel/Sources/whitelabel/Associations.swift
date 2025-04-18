//
//  Associations.swift
//  
//
//  Created by Ravil Khusainov on 15.06.2021.
//

import Foundation

struct Associations: Codable {
    let applinks: Applinks
    
    struct Applinks: Codable {
        var apps: [String] = []
        let details: [Detail]
    }
    
    struct Detail: Codable {
        let appID: String
        let paths: [String]
    }
}

extension Associations {
    init(apps: [App]) {
        self.init(applinks: .init(details: apps.map(Detail.init)))
    }
}

extension Associations.Detail {
    init(app: App) {
        self.appID = app.teamID + "." + app.bundleID
        if let path = app.path {
            self.paths = ["/\(path)/*"]
        } else {
            self.paths = app.paths
        }
    }
}

