//
//  Media.swift
//  RestService
//
//  Created by Ravil Khusainov on 18/12/2016.
//  Copyright © 2016 Lattis. All rights reserved.
//

import UIKit

public struct Media<A> {
    public let url: URL
    public let parse: (Data) -> A?
    public var data: Data? = nil
    public var headers: [String: Any]? = nil
    public var mimeType: MimeType? = nil
    
    public init(url: URL, parse: @escaping (Data) -> A?, data: Data? = nil, mimeType: MimeType? = nil, headers: [String: Any]? = nil) {
        self.url = url
        self.parse = parse
        self.data = data
        self.headers = headers
        self.mimeType = mimeType
    }
}

public extension Media {
    enum MimeType: String {
        case imageJpg = "image/jpg"
    }
}

public extension UIImage {
    public static func load(url: URL) -> Media<UIImage> {
        return Media<UIImage>(url: url, parse: { data in
            return UIImage(data: data)
        })
    }
    public static func facebook(userId: String) -> Media<UIImage> {
        let url = URL(string: "https://graph.facebook.com/\(userId)/picture?type=large")!
        return self.load(url: url)
    }
}

public extension RestService {
    public func download<A>(media: Media<A>, success: @escaping (A) -> (), fail: @escaping (Swift.Error) -> ()) {
        let request = URLRequest(url: media.url)
        perform(request: request, parse: media.parse, success: { result in
            DispatchQueue.main.async {
                success(result)
            }
        }, fail: { error in
            DispatchQueue.main.async {
                fail(error)
            }
        })
    }
    
    public func upload<A>(media: Media<A>, success: @escaping (A) -> (), fail: @escaping (Swift.Error) -> ()) {
        func handle(error: Swift.Error) {
            print("error: \(error)")
            DispatchQueue.main.async {
                fail(error)
            }
        }
        var request = URLRequest(url: media.url)
        request.httpMethod = "POST"
        for (key, value) in  media.headers ?? [:] {
            request.setValue(value as? String, forHTTPHeaderField: key)
        }
        print("request: \(media.url)")
        session.uploadTask(with: request, from: media.data) { (data, _, error) in
            guard error == nil else { return handle(error: error!) }
            guard let data = data else { return handle(error: RestService.Error.invalidData )}
            guard let result = media.parse(data) else { return  handle(error: RestService.Error.invalidParsing(String(data: data, encoding: .utf8)))}
            print("response: \(result)")
            DispatchQueue.main.async {
                success(result)
            }
        }.resume()
    }
}
