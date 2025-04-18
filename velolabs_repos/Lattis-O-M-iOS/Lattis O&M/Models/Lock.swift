//
//  Lock.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 17/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import LattisSDK

public class Lock {
    var peripheral: LattisSDK.Ellipse?
    var lock: Ellipse?
    var lastConnected: Date?
    var serialNumber: (@escaping (String?) ->()) -> () = {_ in}
    var firmware: (@escaping (String?) ->()) -> () = {_ in}
    
    var name: String? {
//        return peripheral?.name
        return lock?.name ?? peripheral?.name
    }
    
    var macId: String? {
        return lock?.macId ?? peripheral?.macId
    }
    
    var isConnected: Bool {
        return peripheral != nil && peripheral!.isPaired
    }
    
    var displayTitle: String {
        var result = lock?.name ?? (peripheral?.name ?? "No name")
        if result.isEmpty {
            result = lock?.bikeName ?? ""
        } else if let bikeName = lock?.bikeName {
            result += ": \(bikeName)"
        }
        return result
    }
    
    init(peripheral: LattisSDK.Ellipse? = nil, lock: Ellipse? = nil) {
        self.peripheral = peripheral
        self.lastConnected = nil
        self.lock = lock
        self.serialNumber = { completion in
            completion(self.peripheral?.serialNumber)
        }
        self.firmware = { completion in
            completion(self.peripheral?.firmwareVersion)
        }
    }
}

extension Lock {
    enum Error: Swift.Error {
        case notFound
    }
    enum Filter {
        case all, bike, noBike, notBelongToFleet
        
        var closure: ((Lock) -> Bool)? {
            switch self {
            case .all:
                return {$0.lock != nil}
            case .bike:
                return {$0.lock?.bikeId != nil}
            case .noBike:
                return {$0.lock?.bikeId == nil}
            case .notBelongToFleet:
                return {$0.lock == nil}
            }
        }
        
        var title: String {
            switch self {
            case .all:
                return "locks_filter_all".localized()
            case .bike:
                return "locks_filter_assigned_to_vehicle".localized()
            case .noBike:
                return "locks_filter_no_vehicle".localized()
            case .notBelongToFleet:
                return "Not belong to that fleet".localized()
            }
        }
    }
    
    enum Vendor: String {
        case ellipse
        case axa
        
        var title: String {
            switch self {
            case .ellipse:
                return "Ellipse/Lattis"
            case .axa:
                return "AXA"
            }
        }
    }
}
