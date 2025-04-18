//
//  SLDirectionAPIHelper.swift
//  Skylock
//
//  Created by Andre Green on 9/16/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

import Foundation
import CoreLocation
import SwiftyJSON
import RestService

class SLDirectionAPIHelper: NSObject {
    let startPoint: CLLocationCoordinate2D
    let endPoint: CLLocationCoordinate2D
    let isBiking: Bool
    
    init(start: CLLocationCoordinate2D, end:CLLocationCoordinate2D, isBiking: Bool) {
        self.startPoint = start
        self.endPoint = end
        self.isBiking = isBiking
    }
    
    @objc func getDirections(completion: @escaping ([AnyObject]?, String?)->()) {
        let url = "https://maps.googleapis.com/maps/api/directions/json?" +
        "origin=\(self.startPoint.latitude),\(self.startPoint.longitude)" +
        "&destination=\(self.endPoint.latitude),\(self.endPoint.longitude)" +
        "&departure_time=now&mode=\(self.isBiking ? "bicycling" : "walking")"
        
        RestService.oval.getGoogleDirections(urlString: url) { (responseData) in
            if let data = responseData {
                var json = JSON(data: data)
                var endAddress:String?
                if let address = json["routes"][0]["legs"][0]["end_address"].string {
                    endAddress = address
                }
                
                if let steps = json["routes"][0]["legs"][0]["steps"].arrayObject {
                    var directions = [SLDirection]()
                    for step in steps where step is [String: AnyObject] {
                        let direction = SLDirection(input: step as! [String : AnyObject])
                        directions.append(direction)
                    }
                    
                    completion(directions, endAddress)
                } else {
                    completion(nil, nil)
                }
            } else {
                completion(nil, nil)
            }
        }
    }
    
    func cleanHtmlFromDirections(inputString: String) -> String {
        return inputString.replacingOccurrences(of: "<[^>]+>", with: "", options: .regularExpression, range: nil)
    }
}
