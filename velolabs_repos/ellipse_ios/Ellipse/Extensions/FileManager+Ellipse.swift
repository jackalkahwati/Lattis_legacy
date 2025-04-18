//
//  FileManager+Ellipse.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/19/17.
//  Copyright © 2017 Lattis. All rights reserved.
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
    
    func removeUserDirectory(for userId: Int) {
        let url = userDirectoryUrl(for: userId)
        do {
            try removeItem(at: url)
        } catch {
            print(error)
        }
    }
    
    func userDirectory(for userId: Int?) -> String {
        return userDirectoryUrl(for: userId).path
    }
}
