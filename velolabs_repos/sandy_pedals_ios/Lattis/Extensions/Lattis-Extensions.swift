//
//  Lattis-Extensions.swift
//  Lattis
//
//  Created by Ravil Khusainov on 06/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

extension TimeInterval {
    var time: String {
        let hours = Int(self)/3600
        let minutes = (Int(self)%3600)/60
        let seconds = Int(self)%60
        return hours > 0 ? String(format: "%02d:%02d:%02d", hours, minutes, seconds) : String(format: "%02d:%02d", minutes, seconds)
    }
    
    var descriptiveTime: String {
        let minutes = (Int(self)%3600)/60
        let formatter = DateComponentsFormatter()
        formatter.unitsStyle = .abbreviated
        if minutes == 0 {
            formatter.allowedUnits = .second
        } else {
            formatter.allowedUnits = [.hour, .minute]
        }
        return formatter.string(from: self) ?? "N/A"
    }
}

extension UIDevice {
    var deviceAndSystem: String {
        return "\(modelName)(\(systemVersion))"
    }
    
    var systemNameAndVersion: String {
        return "\(systemName) \(systemVersion)"
    }
    
    var modelName: String {
        var systemInfo = utsname()
        uname(&systemInfo)
        let machineMirror = Mirror(reflecting: systemInfo.machine)
        let identifier = machineMirror.children.reduce("") { identifier, element in
            guard let value = element.value as? Int8, value != 0 else { return identifier }
            return identifier + String(UnicodeScalar(UInt8(value)))
        }
        
        switch identifier {
        case "iPod5,1":                                 return "iPod Touch 5"
        case "iPod7,1":                                 return "iPod Touch 6"
        case "iPhone3,1", "iPhone3,2", "iPhone3,3":     return "iPhone 4"
        case "iPhone4,1":                               return "iPhone 4s"
        case "iPhone5,1", "iPhone5,2":                  return "iPhone 5"
        case "iPhone5,3", "iPhone5,4":                  return "iPhone 5c"
        case "iPhone6,1", "iPhone6,2":                  return "iPhone 5s"
        case "iPhone7,2":                               return "iPhone 6"
        case "iPhone7,1":                               return "iPhone 6 Plus"
        case "iPhone8,1":                               return "iPhone 6s"
        case "iPhone9,1", "iPhone9,3":                  return "iPhone 7"
        case "iPhone9,2", "iPhone9,4":                  return "iPhone 7 Plus"
        case "i386", "x86_64":                          return "Simulator"
        case "iPad2,1", "iPad2,2", "iPad2,3", "iPad2,4":return "iPad 2"
        case "iPad3,1", "iPad3,2", "iPad3,3":           return "iPad 3"
        case "iPad3,4", "iPad3,5", "iPad3,6":           return "iPad 4"
        case "iPad4,1", "iPad4,2", "iPad4,3":           return "iPad Air"
        case "iPad5,3", "iPad5,4":                      return "iPad Air 2"
        case "iPad2,5", "iPad2,6", "iPad2,7":           return "iPad Mini"
        case "iPad4,4", "iPad4,5", "iPad4,6":           return "iPad Mini 2"
        case "iPad4,7", "iPad4,8", "iPad4,9":           return "iPad Mini 3"
        case "iPad5,1", "iPad5,2":                      return "iPad Mini 4"
        case "iPad6,7", "iPad6,8":                      return "iPad Pro"
        case "AppleTV5,3":                              return "Apple TV"
        default:                                        return identifier
        }
    }
}

extension UIDevice: Encodable {
    enum CodingKeys: String, CodingKey {
        case deviceModel
        case deviceOs
    }
    
    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(modelName, forKey: .deviceModel)
        try container.encode(systemNameAndVersion, forKey: .deviceOs)
    }
}

extension Array {
    func limited(by count: Int) -> Array {
        if self.count >= count {
            return Array(self[0..<count])
        }
        return self
    }
}
