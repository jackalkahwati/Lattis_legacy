//
//  Bike+DeviceManager.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 30.09.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
import Model

extension Bike {
    func deviceManager() -> DeviceManager {
        var devices: [DeviceRepresenting] = []
        if let macId = macId, let key = fleetKey {
            devices.append(EllipseDeviceManager(fleetKey: key, macId: macId, bikeId: bikeId))
        }
        if let controllers = controllers {
            devices += controllers.compactMap(axaFactory)
            devices += controllers.compactMap(nokeFactory)
            devices += controllers.compactMap(kuhmuteFactory)
            devices += controllers.compactMap(ducktFactory)
            devices += controllers.compactMap(manualFactory)
            devices += controllers.compactMap(omniFactory)
            devices += controllers.compactMap(tapkeyFactory)
            devices += controllers.compactMap(kisiFactory)
            devices += controllers.compactMap(parcelHiveFactory)
            devices += controllers.compactMap(sasFactory)
            devices += controllers.compactMap(sentinelFactory)
            devices += controllers.compactMap(edgeFactory)
            devices += controllers.compactMap(iotFactory)
        }
        return .init(devices)
    }
    
    fileprivate func axaFactory(iot: Thing) -> AxaDeviceManager? {
        guard let t = Thing.DeviceType(rawValue: iot.deviceType),
              t == .lock,
              let vendor = Thing.Vendor(rawValue: iot.vendor),
              vendor == .AXA else { return nil }
        return .init(key: iot.key)
    }
    
    fileprivate func iotFactory(iot: Thing) -> IoTDeviceManager? {
        guard let t = Thing.DeviceType(rawValue: iot.deviceType),
              t == .iot,
              let vendor = Thing.Vendor(rawValue: iot.vendor),
              !vendor.in([.omni, .Kisi, .Sentinel, .ParcelHive, .Edge]) else { return nil }
        return .init(bike: self, thing: iot)
    }
    
    fileprivate func nokeFactory(iot: Thing) -> NokeDeviceManager? {
        guard let t = Thing.DeviceType(rawValue: iot.deviceType),
              t == .lock,
              Thing.Vendor.compare(rawValue: iot.vendor, to: .Noke) else { return nil }
        return .init(iot)
    }
    
    fileprivate func kuhmuteFactory(iot: Thing) -> KuhmuteDeviceManager? {
        guard let t = Thing.DeviceType(rawValue: iot.deviceType),
              t == .adapter,
              let vendor = Thing.Vendor(rawValue: iot.vendor),
              vendor == .Kuhmute else { return nil }
        return .init(iot)
    }
    
    fileprivate func ducktFactory(iot: Thing) -> KuhmuteDeviceManager? {
        guard let t = Thing.DeviceType(rawValue: iot.deviceType),
              t == .adapter,
              let vendor = Thing.Vendor(rawValue: iot.vendor),
              vendor == .Duckt else { return nil }
        return .init(iot)
    }
    
    fileprivate func manualFactory(iot: Thing) -> ManualDeviceManager? {
        guard Thing.DeviceType.compare(rawValue: iot.deviceType, to: .lock),
              Thing.Vendor.compare(rawValue: iot.vendor, to: .manual) else { return nil }
        return .init(iot)
    }
    
    fileprivate func omniFactory(iot: Thing) -> OmniDeviceManager? {
        guard Thing.DeviceType.compare(rawValue: iot.deviceType, to: .iot),
              Thing.Vendor.compare(rawValue: iot.vendor, to: .omni) else { return nil }
        return .init(iot, controllers: controllers?.map(\.key) ?? [])
    }
    
    fileprivate func tapkeyFactory(iot: Thing) -> TapkeyDeviceManager? {
        guard Thing.DeviceType.compare(rawValue: iot.deviceType, to: .lock),
              Thing.Vendor.compare(rawValue: iot.vendor, to: .tapkey) else { return nil }
        return .init(iot)
    }
    
    fileprivate func kisiFactory(iot: Thing) -> KisiDeviceManager? {
        guard Thing.DeviceType.compare(rawValue: iot.deviceType, to: .iot),
              Thing.Vendor.compare(rawValue: iot.vendor, to: .Kisi) else { return nil }
        return .init(iot, bike: self)
    }
    
    fileprivate func sasFactory(iot: Thing) -> SasDeviceManager? {
        guard Thing.DeviceType.compare(rawValue: iot.deviceType, to: .lock),
              Thing.Vendor.compare(rawValue: iot.vendor, to: .Sas) else { return nil }
        return .init(iot)
    }
    
    fileprivate func sentinelFactory(iot: Thing) -> SentinelDeviceManager? {
        guard Thing.DeviceType.compare(rawValue: iot.deviceType, to: .iot),
              Thing.Vendor.compare(rawValue: iot.vendor, to: .Sentinel) else { return nil }
        return .init(iot, bike: self)
    }
    
    fileprivate func parcelHiveFactory(iot: Thing) -> ParcelHiveDeviceManager? {
        guard Thing.DeviceType.compare(rawValue: iot.deviceType, to: .iot),
              Thing.Vendor.compare(rawValue: iot.vendor, to: .ParcelHive) else { return nil }
        return .init(iot, bike: self)
    }
    
    fileprivate func edgeFactory(iot: Thing) -> EdgeDeviceManager? {
        guard Thing.DeviceType.compare(rawValue: iot.deviceType, to: .iot),
              Thing.Vendor.compare(rawValue: iot.vendor, to: .Edge) else { return nil }
        return .init(iot, bike: self)
    }
}
