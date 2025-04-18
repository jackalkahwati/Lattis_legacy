//
//  Data+Save.swift
//  
//
//  Created by Ravil Khusainov on 15.06.2021.
//

import Foundation

fileprivate let manager = FileManager.default

extension Data {
    func save(to root: URL, path: String, fileName: String) throws {
        var url = root
        url.appendPathComponent(path, isDirectory: true)
        if !manager.fileExists(atPath: url.path) {
            try manager.createDirectory(at: url, withIntermediateDirectories: true, attributes: nil)
        }
        url.appendPathComponent(fileName)
        if manager.fileExists(atPath: url.path) {
            try self.write(to: url)
        } else {
            manager.createFile(atPath: url.path, contents: self, attributes: nil)
        }
    }
}
