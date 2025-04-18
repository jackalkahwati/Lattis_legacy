//
//  OvalMisc.swift
//  RestService
//
//  Created by Ravil Khusainov on 06/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import SwiftyJSON
import KeychainSwift

public extension Oval {
    public static let misc = Misc()
    public class Misc: Oval.Route {
        public typealias success = (Responce) -> ()
        internal let basePath = "misc/"
        
        public func upload(data: Data, for type: UploadType, success: @escaping (String) -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let urlString = "\(restService.endpoint)\(basePath)upload?type=\(type.rawValue)"
            guard let url = URL(string: urlString) else { return fail(RestService.Error.invalidURL(urlString)) }
            let parse: (Data) -> String? = { resData in
                let json = JSON(data: resData)
                return json["payload"]["uploaded_url"].string
            }
            let media = Media<String>(url: url, parse: parse, data: data, mimeType: .imageJpg, headers: ["authorization":token])
            guard let multipart = media.multipart else { return fail(Oval.Error.badResponce) }
            restService.upload(media: multipart, success: { resultUrl in
                DispatchQueue.main.async {
                    success(resultUrl)
                }
            }, fail: { error in
                DispatchQueue.main.async {
                    fail(error)
                }
            })
        }
        
        // FIXME: Smell code. Very-very bad backend API
        public func operatorUpload(data: Data, for type: UploadType, success: @escaping (String) -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let urlString = "\(restService.endpoint)\(basePath)operator-upload?type=\(type.rawValue)"
            guard let url = URL(string: urlString) else { return fail(RestService.Error.invalidURL(urlString)) }
            let parse: (Data) -> String? = { resData in
                let json = JSON(data: resData)
                return json["payload"]["uploaded_url"].string
            }
            let media = Media<String>(url: url, parse: parse, data: data, mimeType: .imageJpg, headers: ["authorization":token])
            guard let multipart = media.multipart else { return fail(Oval.Error.badResponce) }
            restService.upload(media: multipart, success: { resultUrl in
                DispatchQueue.main.async {
                    success(resultUrl)
                }
            }, fail: { error in
                DispatchQueue.main.async {
                    fail(error)
                }
            })
        }
    }
}

public extension Oval.Misc {
    enum UploadType: String {
        case parking, maintenance, bike
    }
}

extension Media {
    var multipart: Media? {
        guard let data = data, let mimeType = mimeType else { return nil }
        var media = self
        let boundary = "Boundary-" + UUID().uuidString
        media.headers?["Content-Type"] = "multipart/form-data; boundary=" + boundary
        media.data = createBody(boundary: boundary, data: data, mimeType: mimeType.rawValue, filename: NSUUID().uuidString.lowercased())
        return media
    }
    
    private func createBody(parameters: [String: String] = [:],
                    boundary: String,
                    data: Data,
                    mimeType: String,
                    filename: String) -> Data {
        let body = NSMutableData()
        
        let boundaryPrefix = "--\(boundary)\r\n"
        
        for (key, value) in parameters {
            body.appendString(boundaryPrefix)
            body.appendString("Content-Disposition: form-data; name=\"\(key)\"\r\n\r\n")
            body.appendString("\(value)\r\n")
        }
        
        body.appendString(boundaryPrefix)
        body.appendString("Content-Disposition: form-data; name=\"file\"; filename=\"\(filename)\"\r\n")
        body.appendString("Content-Type: \(mimeType)\r\n\r\n")
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
