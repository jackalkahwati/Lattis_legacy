//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 04.10.2021.
//

import Foundation
import Vapor

public struct Versions: Codable {
    public let app: String
    public let toolbox: String
    
//    static func current() throws -> Versions {
//        guard let url = Foundation.Bundle.module.url(forResource: "versions", withExtension: "json") else { throw Failure.fileNotFound }
//        let data = try Data(contentsOf: url)
//        return try JSONDecoder().decode(Versions.self, from: data)
//    }
    
    public enum Failure: Error {
        case fileNotFound
    }
}

struct AppVersionsKey: StorageKey {
    typealias Value = Versions
}


//extension Application {
//    public var versions: Versions {
//        get {
//            self.storage[AppVersionsKey.self]!
//        }
//        set {
//            self.storage[AppVersionsKey.self] = newValue
//        }
//    }
//}
