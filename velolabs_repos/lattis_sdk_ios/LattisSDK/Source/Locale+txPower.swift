//
//  Locale+txPower.swift
//  LattisSDK
//
//  Created by Ravil Khusainov on 9/21/18.
//  Copyright © 2018 Lattis Inc. All rights reserved.
//

import Foundation


extension Locale {
    enum RegionLocale: String {
        // Europe
        case Belgium = "BE"
        case Greece    = "EL"
        case Lithuania = "LT"
        case Portugal = "PT"
        case Bulgaria = "BG"
        case Spain = "ES"
        case Luxembourg    = "LU"
        case Romania = "RO"
        case CzechRepublic = "CZ"
        case France    = "FR"
        case Hungary = "HU"
        case Slovenia = "SI"
        case Denmark = "DK"
        case Croatia = "HR"
        case Malta = "MT"
        case Slovakia = "SK"
        case Germany = "DE"
        case Italy = "IT"
        case Netherlands = "NL"
        case Finland = "FI"
        case Estonia = "EE"
        case Cyprus    = "CY"
        case Austria = "AT"
        case Sweden    = "SE"
        case Ireland = "IE"
        case Latvia    = "LV"
        case Poland = "PL"
        case UnitedKingdom = "UK"
        
        //EFTA
        case Iceland = "IS"
        case Norway = "NO"
        case Liechtenstein = "LI"
        case Switzerland = "CH"
        
        //Others
        case Russia = "RU"
        case UnitedStates = "US"
        case undefined
        
        var isEurope: Bool {
            switch self {
            case .Belgium, .Greece, .Lithuania, .Portugal, .Bulgaria, .Spain, .Luxembourg, .Romania, .CzechRepublic, .France, .Hungary, .Slovenia, .Denmark, .Croatia, .Malta, .Slovakia, .Germany, .Italy, .Netherlands, .Finland, .Estonia, .Cyprus, .Austria, .Sweden, .Ireland, .Latvia, .Poland, .UnitedKingdom, .Iceland, .Norway, .Liechtenstein, .Switzerland:
                return true
            default:
                return false
            }
        }
    }
    
    var region: RegionLocale {
        guard let code = regionCode else { return .undefined }
        return RegionLocale(rawValue: code) ?? .undefined
    }
    
    var txPowerValue: UInt8 {
        return region.isEurope ? 0x00 : 0x04
    }
}
