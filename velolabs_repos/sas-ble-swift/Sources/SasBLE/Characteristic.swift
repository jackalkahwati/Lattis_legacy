//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 06.04.2022.
//

import Foundation
import CoreBluetooth


public extension SasBLE {
    enum Characteristic: String, CaseIterable {
        case nonce = "a563020c-5d20-465f-b493-6a0031b9fcf3"
        case unlock = "a5630201-5d20-465f-b493-6a0031b9fcf3"
        case state = "a5630207-5d20-465f-b493-6a0031b9fcf3"
        
        var uuid: CBUUID {
            .init(string: rawValue)
        }
        
        static var all: [CBUUID] {
            Characteristic.allCases.map(\.uuid)
        }
    }
}
