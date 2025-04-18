//
//  JSONStorage.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 12/06/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Foundation

class JSONStorage<A: Codable> {
    
    fileprivate let url: URL
    fileprivate let decoder = JSONDecoder()
    fileprivate let encoder = JSONEncoder()
    fileprivate var cache: A?
    
    init(_ fileName: String) {
        url = AppRouter.shared.userDirUrl.appendingPathComponent(fileName)
        fetch(completion: {_ in})
    }
    
    var fileExists: Bool {
        return FileManager.default.fileExists(atPath: url.path)
    }
    
    func save(_ item: A) {
        cache = item
        do {
            let data = try encoder.encode(item)
            try data.write(to: url)
        } catch {
            print(error)
        }
    }
    
    func save<B: Equatable>(_ item: B) where A == [B] {
        var cache = self.cache ?? []
        guard cache.contains(item) == false else { return }
        cache.insert(item, at: 0)
        do {
            let data = try encoder.encode(cache)
            try data.write(to: url)
            self.cache = cache
        } catch {
            print(error)
        }
    }
    
    func fetch(completion: (A) -> ()) {
        guard fileExists else {
            return
        }
        do {
            let data = try Data(contentsOf: url)
            let cache = try decoder.decode(A.self, from: data)
            completion(cache)
            self.cache = cache
        } catch {
            print(error)
        }
    }
    
    func destroy() {
        try? FileManager.default.removeItem(at: url)
    }
}

@propertyWrapper struct JSONStorageBacked<Value: Codable> {
    let storage: JSONStorage<Value>
    var cache: Value
    
    init(fileName: String, defaultValue: Value) {
        storage = .init(fileName)
        cache = defaultValue
        storage.fetch { (r) in
            self.cache = r
        }
    }
    
    func destroy() {
        storage.destroy()
    }
    
    var wrappedValue: Value {
        set {
            cache = newValue
            storage.save(cache)
        }
        get {
            cache
        }
    }
}
