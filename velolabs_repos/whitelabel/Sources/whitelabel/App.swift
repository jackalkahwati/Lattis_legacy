//
//  App.swift
//  
//
//  Created by Ravil Khusainov on 15.06.2021.
//

import Foundation

struct App {
    internal init(teamID: String, bundleID: String, paths: [String] = ["*"], link: String? = nil, path: String? = nil) {
        self.teamID = teamID
        self.bundleID = bundleID
        self.paths = paths
        self.link = link
        self.path = path
    }
    
    let teamID: String
    let bundleID: String
    let paths: [String]
    let link: String?
    let path: String?
}
