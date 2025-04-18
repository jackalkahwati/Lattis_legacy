//
//  Notifications.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 14.10.2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UserNotifications

public extension Notification.Name {
    static let endRide = Notification.Name.init(rawValue: "end_ride")
    static let cancelBooking = Notification.Name.init(rawValue: "cancel_booking")
    static let reservation = Notification.Name(rawValue: "io.lattis.notification.reservation")
    static let tripStarted = Notification.Name(rawValue: "io.lattis.notification.trip_started")
    static let creditCardUpdated = Notification.Name(rawValue: "io.lattis.notification.credit_card_updated")
    static let creditCardAdded = Notification.Name(rawValue: "io.lattis.notification.creditCardAdded")
    static let creditCardRemoved = Notification.Name(rawValue: "io.lattis.notification.creditCardRemoved")
    
    static let tripUpdated = Notification.Name("tripUpdated")
    static let deviceStatusUpdated = Notification.Name("deviceStatusUpdated")
    static let deviceMessage = Notification.Name("deviceMessage")
    static let reservationEndingSoon = Notification.Name("reservationEndingSoon")
    
    static let subscriptionsUpdated = Notification.Name("io.lattis.notification.subscriptionsUpdated")
    static let vehicleDocked = Notification.Name("io.lattis.notificatons.vehicle.docked")
    static let dockingUnlock = Notification.Name("io.lattis.notificatons.docking.unlock")
    static let dockingEndRide = Notification.Name("io.lattis.notificatons.docking.end.ride")
    static let smartDocking = Notification.Name("io.lattis.notificatons.smart.docking")
    static let vehicleLocked = Notification.Name("io.lattis.notificatons.vehicle.locked")
    
    static let internetConnection = Notification.Name("io.lattis.notificatons.internetConnection")
    static let sentinelOnline = Notification.Name("io.lattis.notificatons.sentinel.online")
    static let sentinelOffline = Notification.Name("io.lattis.notificatons.sentinel.offline")
    static let sentinelOpen = Notification.Name("io.lattis.notificatons.sentinel.open")
    static let sentinelClose = Notification.Name("io.lattis.notificatons.sentinel.close")
}

extension Notification {
    enum UserInfoKey: String, Hashable {
        case damageReported
    }
    
    static let geofenceViolationIdentifier = "geofenceViolationIdentifier"
}

extension UNNotification {
    enum Category: String {
        case docking
        case docked
        case locked
        case reservationEndingSoon
        case sentinelOnline = "sentinel_lock_online"
        case sentinelClosed = "sentinel_lock_closed"
        case sentinelOpen = "sentinel_lock_opened"
    }
    
    enum UserInfoKey: String {
        case trip_id
    }
    
    var category: Category? {
        Category(rawValue: request.content.categoryIdentifier)
    }
    
    func doesMatch(category: Category) -> Bool {
        guard let cat = self.category else { return false }
        return cat == category
    }
    
    func userInfo<T>(key: UserInfoKey) -> T? {
        request.content.userInfo[key.rawValue] as? T
    }
}

extension UNNotificationResponse {
    enum Action: String {
        case dockingUnlock = "docking.unlock"
        case dockingEndRide = "docking.end.ride"
    }
    
    var action: Action? { Action(rawValue: actionIdentifier) }
}
