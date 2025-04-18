//
//  HyenaBikeService.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 7/21/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import HyLink
import Crashlytics

class HyenaBikeService: EBikesService {
    let request: EBikeInfo.Request
    fileprivate let callback: (EBikeInfo.Result) -> ()
    fileprivate let hyLink = HyLink.sharedInstance
    
    required init(request: EBikeInfo.Request, callback:@escaping (EBikeInfo.Result) -> ()) {
        self.request = request
        self.callback = callback
        
        perform()
    }
    
    private func perform() {
        hyLink.addConnectListener(listener: self)
        hyLink.addDataListener(listener: self)
        hyLink.startScanning()
    }
}

extension HyenaBikeService: HyConnectListener {
    func hyConnectDidDiscover(device: HyDevice) {
        Answers.logCustomEvent(withName: "Hyenatek", customAttributes: ["discovered": device.key])
//        if device.key == key {
            hyLink.connectWith(device: device)
//        }
    }
    
    func hyConnectDidConnectWith(device: HyDevice) {
        Answers.logCustomEvent(withName: "Hyenatek", customAttributes: ["connected": device.key])
        hyLink.startDeviceService(device: device)
        hyLink.addCommand(data_type: .DISP)
        hyLink.sendCommand()
    }
    
    func hyConnectDidDisconnectWith(device: HyDevice) {
        Answers.logCustomEvent(withName: "Hyenatek", customAttributes: ["disconnected": device.key])
    }
    
    func hyConnectDidFailedToConnectTo(device: HyDevice, error: Error?) {
        if let error = error {// key == device.key {
            callback(.error(error, request))
        }
    }
}

extension HyenaBikeService: HyDataListener {
    func hyDeviceHasConnection() {
        
    }
    
    func hyDeviceHasData(data_type: HyDataType, data_info: HyDataInfo) {
        Answers.logCustomEvent(withName: "Hyenatek", customAttributes: ["get_info": String(describing: type(of: data_info))])
        if let info = data_info as? DispInfo {
            let result = EBikeInfo(batteryLevel: Double(info.battery_level_in_10)/10, request: request)
            callback(.success(result))
            hyLink.removeDataListener(listener: self)
            hyLink.removeConnectListener(listener: self)
        }
    }
}
