//
//  AppDelegate.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 08/03/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Reachability

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?
    internal let reachability = try? Reachability()
    internal weak var connectionAlert: StaticAlertView?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        start()
        return true
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        pushToken = deviceToken.reduce("", {$0 + String(format: "%02X", $1)})
    }
    
    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        print(error)
    }
}

