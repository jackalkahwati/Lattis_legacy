//
//  Lock.swift
//  Lattis
//
//  Created by Ravil Khusainov on 18/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation
import LattisSDK
import AXALock

struct Lock {
    var peripheral: LattisSDK.Ellipse?
    var ellipse: Ellipse?
    var iotModules: [IoTModule]
    var axaLock: AxaBLE.Lock?
    
    init(peripheral: LattisSDK.Ellipse) {
        self.peripheral = peripheral
        self.ellipse = nil
        self.iotModules = []
        self.axaLock = nil
    }
    
    init(ellipse: Ellipse?) {
        self.ellipse = ellipse
        self.peripheral = nil
        self.axaLock = nil
        self.iotModules = []
    }
    
    init(bike: Bike) {
        if let macId = bike.macId {
            self.ellipse = Ellipse(macId: macId)
        } else {
            self.ellipse = nil
        }
        self.iotModules = bike.iotModules
        self.peripheral = nil
        self.axaLock = nil
    }
}

extension Lock {
    var needEllipse: Bool { ellipse != nil && peripheral == nil }
    var axaModule: IoTModule? { iotModules.first(where: {$0.vendor == .AXA}) }
    var needAxa: Bool { axaModule != nil && axaLock == nil }
}
