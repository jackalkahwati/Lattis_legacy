//
//  AxaDevice.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 26.03.2020.
//  Copyright © 2020 Lattis. All rights reserved.
//

import Foundation
import AXALock

struct AxaDevice {
    let lock: AxaBLE.Lock
    let module: IoTModule
    let bike: Bike?
    
    var name: String { lock.name }
    var isPaired: Bool { lock.connection == .paired }
    
    func change(bike: Bike?) -> AxaDevice {
        .init(lock: lock, module: module, bike: bike)
    }
}
