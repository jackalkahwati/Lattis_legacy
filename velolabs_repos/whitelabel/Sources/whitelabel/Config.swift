//
//  Config.swift
//  
//
//  Created by Ravil Khusainov on 15.06.2021.
//

import Foundation

enum Config {
    static let rootPath: String = "Artefacts"
    static var apps: [App] {
        [
            App(teamID: "4WD59CZ9Z6", bundleID: "io.lattis.www.Lattis", link: "https://apps.apple.com/us/app/lattis/id1235042268", path: "lattis"),
            App(teamID: "6VRGKAMM36", bundleID: "com.guestbike.mobility", link: "https://apps.apple.com/us/app/guestbike/id1538103816", path: "guestbike"),
            App(teamID: "SCZ62CRDN2", bundleID: "com.ongrin.grin", link: "https://apps.apple.com/us/app/grin-mobility/id1411088480", path: "grin"),
        ]
    }
}
