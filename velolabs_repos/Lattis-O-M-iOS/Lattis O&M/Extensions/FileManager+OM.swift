//
//  FileManager+Lattis.swift
//  Lattis
//
//  Created by Ravil Khusainov on 16/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation

extension FileManager {
    func userDirectoryUrl(for userId: Int?) -> URL {
        let urls = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
        let docURL = urls[urls.endIndex-1]

        let user = userId == nil ? "shared" : "\(userId!)"
        let url = docURL.appendingPathComponent(user, isDirectory: true)
        if FileManager.default.fileExists(atPath: url.path, isDirectory: nil) == false {
            do {
                try FileManager.default.createDirectory(at: url, withIntermediateDirectories: true, attributes: nil)
            } catch {
                fatalError("\(error)")
            }
        }
        print(url)
        return url
    }

    func userDirectory(for userId: Int?) -> String {
        return userDirectoryUrl(for: userId).path
    }
}
