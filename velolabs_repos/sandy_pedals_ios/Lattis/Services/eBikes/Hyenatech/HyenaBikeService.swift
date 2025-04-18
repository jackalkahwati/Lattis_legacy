//
//  HyenaBikeService.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 7/21/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import HyLink

class HyenaBikeService: EBikesService {
    let key: String
    fileprivate let callback: (EBikeInfo.Result) -> ()
    fileprivate let hyLink = HyLink.sharedInstance
    
    required init(key: String, callback:@escaping (EBikeInfo.Result) -> ()) {
        self.key = key
        self.callback = callback
        
        perform()
    }
    
    private func perform() {
        hyLink.addConnectListener(listener: self)
        hyLink.startScanning()
    }
}

extension HyenaBikeService: HyConnectListener {
    func hyConnectDidDiscover(device: HyDevice) {
        if device.key == key {
            hyLink.addDataListener(listener: self)
            hyLink.connectWith(device: device)
        }
    }
    
    func hyConnectDidConnectWith(device: HyDevice) {
        
    }
    
    func hyConnectDidDisconnectWith(device: HyDevice) {
        
    }
    
    func hyConnectDidFailedToConnectTo(device: HyDevice, error: Error?) {
        if let error = error, key == device.key {
            callback(.error(error, self.key))
        }
    }
}

extension HyenaBikeService: HyDataListener {
    func hyDeviceHasConnection() {
        
    }
    
    func hyDeviceHasData(data_type: HyDataType, data_info: HyDataInfo) {
        if let info = data_info as? DispInfo {
            let result = EBikeInfo(batteryLevel: Double(info.battery_level_in_10)/10, key: key)
            callback(.success(result))
            hyLink.removeDataListener(listener: self)
            hyLink.removeConnectListener(listener: self)
        }
    }
}
