//
//  AppDelegate.swift
//  BLEService
//
//  Created by Ravil Khusainov on 17/12/2016.
//  Copyright © 2016 Lattis. All rights reserved.
//

import UIKit
import Fabric
import Crashlytics
import RestService


@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?


    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
//        BLEService.setup(network: FakeNetwork(userId: "1000000000", signedMessage: "000040ed54877027712d450585686e6b841e27fc4cd9caaf6734bc732e14e755ffffffff00c47d79242fd3a44117127e94e5a487f018d74d3ecc0113dee8aadf3e3b2979c1ac4ddbc6ca0b25deff9b48f9e164d39c456bd4c55151fb5a6cc0475ca976cb02", publicKey: "e55ef9026fee9b1f262c734a4c4692bebe556ed2423610b29e73ba022b07fb8e7c621e2c143f203013acb19d444f60cf7e26f1ce925b0b18faf4bfaa117519c0"), backgroundEnabled: false)
        BLEService.setup(network: Oval.locks, backgroundEnabled: false)
        Fabric.with([Crashlytics.self])
        return true
    }

    func applicationWillResignActive(_ application: UIApplication) {
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
        // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
        // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    }

    func applicationWillEnterForeground(_ application: UIApplication) {
        // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    }

    func applicationWillTerminate(_ application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    }


}

