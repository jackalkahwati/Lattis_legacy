//
//  SLDirection.swift
//  Skylock
//
//  Created by Andre Green on 9/11/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

import Foundation;
import CoreLocation;

open class SLDirection: NSObject {
    open var start: CLLocationCoordinate2D?
    open var end: CLLocationCoordinate2D?
    open var directions: String?
    open var distance: Int?
    open var address: String?
    open var duration: Double?
    fileprivate let milesPerMeter:Float = 0.000621371
    
    convenience init(input: [String: AnyObject]) {
        self.init()
        
        if let instructions = input["html_instructions"] as? String {
            self.directions = instructions.replacingOccurrences(
                of: "<[^>]+>",
                with: "",
                options: .regularExpression,
                range: nil
            )
        }
        
        if let startLocation = input["start_location"] as? [String:Double],
            let latitude = startLocation["lat"],
            let longitude = startLocation["lng"] {
            self.start = CLLocationCoordinate2DMake(latitude, longitude)
        }
        
        if let endLocation = input["end_location"] as? [String:Double],
            let latitude = endLocation["lat"],
            let longitude = endLocation["lng"] {
                self.end = CLLocationCoordinate2DMake(latitude, longitude)
        }
        
        if let distance = input["distance"] as? [String:AnyObject],
            let value = distance["value"] as? Int {
            self.distance = value
        }
        
        if let info = input["duration"] as? [String:AnyObject], let duration = info["value"] as? Double {
            self.duration = duration
        }
    }
    
    @objc open func getDirections() -> String? {
        return self.directions
    }
    
    @objc open func distanceInMiles() -> CGFloat {
        if let distance = self.distance {
            return CGFloat(distance)*CGFloat(self.milesPerMeter)
        }
        
        return CGFloat.greatestFiniteMagnitude
    }
    
    @objc open func getDuration() -> Double {
        guard let duration = self.duration else {
            return 0.0
        }
        
        return duration
    }
}
