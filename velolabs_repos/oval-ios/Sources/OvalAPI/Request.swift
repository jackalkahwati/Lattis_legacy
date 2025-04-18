//
//  Request.swift
//  OvalExample
//
//  Created by Ravil Khusainov on 7/10/18.
//  Copyright © 2018 Lattis inc. All rights reserved.
//

import Foundation

public struct Request<A: Encodable> {
    public let content: Content<A>
    public let api: API
    let method: String
    let headers: [String: String]
    let dateAsTimestamp: Bool
    
    public static func get(_ api: API, headers: [String: String] = [:], dateAsTimestamp: Bool = true) -> Request<Empty> {
        return Request<Empty>(content: .empty, api: api, method: "GET", headers: headers, dateAsTimestamp: dateAsTimestamp)
    }
    public static func delete(_ api: API, headers: [String: String] = [:], dateAsTimestamp: Bool = true) -> Request<Empty> {
        return Request<Empty>(content: .empty, api: api, method: "DELETE", headers: headers, dateAsTimestamp: dateAsTimestamp)
    }
    public static func delete<A: Encodable>(_ params: A, api: API, headers: [String: String] = [:], dateAsTimestamp: Bool = true) -> Request<A> {
        return Request<A>(content: .json(params), api: api, method: "DELETE", headers: headers, dateAsTimestamp: dateAsTimestamp)
    }
    public static func put<A: Encodable>(_ params: A, api: API, headers: [String: String] = [:], dateAsTimestamp: Bool = true) -> Request<A> {
        return Request<A>(content: .json(params), api: api, method: "PUT", headers: headers, dateAsTimestamp: dateAsTimestamp)
    }
    public static func patch<A: Encodable>(_ params: A, api: API, headers: [String: String] = [:], dateAsTimestamp: Bool = true) -> Request<A> {
        return Request<A>(content: .json(params), api: api, method: "PATCH", headers: headers, dateAsTimestamp: dateAsTimestamp)
    }
    public static func post<A: Encodable>(json params: A, api: API, headers: [String: String] = [:], dateAsTimestamp: Bool = true) -> Request<A> {
        return Request<A>(content: .json(params), api: api, method: "POST", headers: headers, dateAsTimestamp: dateAsTimestamp)
    }
    public static func update<A: Encodable>(_ params: A, api: API, headers: [String: String] = [:], dateAsTimestamp: Bool = true) -> Request<A> {
        return Request<A>(content: .json(params), api: api, method: "UPDATE", headers: headers, dateAsTimestamp: dateAsTimestamp)
    }
    public static func post(multipart: Multipart, api: API, headers: [String: String] = [:], dateAsTimestamp: Bool = true) -> Request<Empty> {
        var head = headers
        head["Content-Type"] = "multipart/form-data; boundary=" + multipart.boundary
        return Request<Empty>(content: Content<Empty>.multipart(multipart), api: api, method: "POST", headers: head, dateAsTimestamp: dateAsTimestamp)
    }
}

public enum Content<A> {
    case empty
    case json(A)
    case multipart(Multipart)
}

public struct Multipart {
    public let data: Data
    public let mimeType: MimeType
    public let params: [String: String]
    public let filename: String
    public let boundary: String
    
    public init(data: Data,
                mimeType: MimeType = .imageJpg,
                params: [String: String] = [:],
                filename: String = UUID().uuidString.lowercased(),
                boundary: String = UUID().uuidString) {
        self.data = data
        self.mimeType = mimeType
        self.params = params
        self.filename = filename
        self.boundary = "Boundary-" + boundary
    }
    
    public enum MimeType: String {
        case imageJpg = "image/jpg"
    }
}


public struct Empty: Codable {}

extension Multipart {
    var body: Data {
        let body = NSMutableData()
        
        let boundaryPrefix = "--\(boundary)\r\n"
        
        for (key, value) in params {
            body.appendString(boundaryPrefix)
            body.appendString("Content-Disposition: form-data; name=\"\(key)\"\r\n\r\n")
            body.appendString("\(value)\r\n")
        }
        
        body.appendString(boundaryPrefix)
        body.appendString("Content-Disposition: form-data; name=\"file\"; filename=\"\(filename)\"\r\n")
        body.appendString("Content-Type: \(mimeType.rawValue)\r\n\r\n")
        body.append(data)
        body.appendString("\r\n")
        body.appendString("--".appending(boundary.appending("--")))
        
        return body as Data
    }
}

private extension NSMutableData {
    func appendString(_ string: String) {
        let data = string.data(using: String.Encoding.utf8, allowLossyConversion: false)
        append(data!)
    }
}
