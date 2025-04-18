//
//  JSONStorage.swift
//  Operator (iOS)
//
//  Created by Ravil Khusainov on 13.03.2021.
//

import Foundation
import Combine

public func getDocumentsDirectory() -> URL {
    let paths = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
    let documentsDirectory = paths[0]
    return documentsDirectory
}

public struct JSONStorage {
    
    public let rootUrl: URL
    public static var shared: JSONStorage { .init(documents: "shared")}
    fileprivate let decoder = JSONDecoder()
    fileprivate let encoder = JSONEncoder()
    fileprivate let queue: DispatchQueue = .global()
    fileprivate let fileManager: FileManager = .default
    
    public init(_ rootUrl: URL) {
        self.rootUrl = rootUrl
    }
    
    public func fetch<Value: Codable>(type: Value.Type, que: DispatchQueue = .main) -> AnyPublisher<Value, Error> {
        let promise = PassthroughSubject<Value, Error>()
        let url = rootUrl.appendingPathComponent(String(describing: Value.self))
        guard fileManager.fileExists(atPath: url.path) else {
            return promise.handleEvents(receiveSubscription: { sub in
                que.async {
                    promise.send(completion: .failure(Failure.noSuchFile))
                }
            }).eraseToAnyPublisher()

        }
        queue.async {
            do {
                let data = try Data(contentsOf: url)
                let cache = try decoder.decode(Value.self, from: data)
                que.async {
                    promise.send(cache)
                    promise.send(completion: .finished)
                }
            } catch {
                que.async {
                    promise.send(completion: .failure(error))
                }
            }
        }
        return promise.eraseToAnyPublisher()
    }
    
    @discardableResult
    public func save<Value: Codable>(_ value: Value) -> AnyPublisher<Void, Error> {
        let promise = PassthroughSubject<Void, Error>()
        let url = rootUrl.appendingPathComponent(String(describing: Value.self))
        queue.async {
            do {
                let data = try encoder.encode(value)
                try self.write(data: data, to: url)
                promise.send(completion: .finished)
            } catch {
                promise.send(completion: .failure(error))
            }
        }
        return promise.eraseToAnyPublisher()
    }
    
    public func destroy() {
        do {
            try fileManager.removeItem(at: rootUrl)
        } catch {
            print(error)
        }
    }
    
    private func write(data: Data, to url: URL) throws {
        if !fileManager.fileExists(atPath: rootUrl.path) {
            try fileManager.createDirectory(at: rootUrl, withIntermediateDirectories: false, attributes: nil)
        }
        if fileManager.fileExists(atPath: url.path) {
            try fileManager.removeItem(at: url)
        }
        fileManager.createFile(atPath: url.path, contents: data, attributes: nil)
    }
}

extension JSONStorage {
    init(documents directory: String) {
        self.init(getDocumentsDirectory().appendingPathComponent(directory))
    }
    
    enum Failure: Error {
        case noSuchFile
    }
}
