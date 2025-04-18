//
//  InversDevice.swift
//  Operator
//
//  Created by Ravil Khusainov on 01.11.2021.
//

import Foundation
import Combine

enum InversDevice {
    struct Status: Codable {
        let central_lock: Security?
        let immobilizer: Security?
        let ignition: Ignition?
    }
    
    enum Security: String, Codable {
        case locked, unlocked
    }
    
    enum Ignition: String, Codable {
        case on, off
    }
}
