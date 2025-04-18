//
//  RestService+Ellipse.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 19/12/2016.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import Foundation
import RestService
import SwiftyJSON

extension RestService {
    static let main = RestService("https://lattisapi.io/api/v1/")
    
    func getGoogleDirections(urlString: String, completion: @escaping (Data?) -> ()) {
        print("\(#function)")
        guard let url = URL(string: urlString) else { return completion(nil) }
        URLSession.shared.dataTask(with: url) { (data, _, error) in
            if error != nil {
                print("error getting directions from url: \(urlString), failed with error: \(error!)")
            }
            completion(data)
        }.resume()
    }
}

extension RestService {
    struct Result {
        let statusCode: Int
        let payload: [String: JSON]?
        let error: Error?
    }
}

extension RestService.Result {
    var success: Bool {
        return statusCode == 200 || statusCode == 201
    }
    
    static let parse: (JSON) -> RestService.Result? = { dict in
        guard let statusCode = dict["status"].object as? Int else { return nil }
        var payload: [String: JSON]?
        if let pl = dict["payload"].dictionary {
            payload = pl
        } else {
            payload = ["payload": dict["payload"]]
        }
        var error: RestService.Error?
        if let message = dict["error"].string {
            error = .server(message)
        }
        return RestService.Result(statusCode: statusCode, payload: payload, error: error)
    }
    
    static func request(path: String, post: [String: Any]? = nil) -> Resource<RestService.Result> {
        return Resource<RestService.Result>(path: path,
            jsonData: post,
            parse: parse
        )
    }
}
