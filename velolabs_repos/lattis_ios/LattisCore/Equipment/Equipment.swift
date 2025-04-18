//
//  Equipment.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 2022-05-14.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Foundation
import Combine
import Model

@MainActor
protocol Equipment {
    
    var callback: PassthroughSubject<EquipmentControler.Callback, Error> {get}
    
    func connect()
    func lock()
    func unlock()
}

extension Thing {
    @MainActor
    func equipment(asset: Asset) -> Equipment? {
        guard let vendor = Thing.Vendor(rawValue: vendor) else { return nil }
        switch vendor {
        case .Kisi:
            return KisiEquipment(self, asset: asset)
        case .Sas:
            return SasEquipment(self)
        case .ParcelHive:
            return ParcelHiveEquipment(self, asset: asset)
        case .Edge:
            return EdgeEquipment(self, asset: asset)
        default:
            return nil
        }
    }
}
