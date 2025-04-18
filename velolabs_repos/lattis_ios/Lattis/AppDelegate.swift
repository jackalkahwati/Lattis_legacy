//
//  AppDelegate.swift
//  Lattis
//
//  Created by Ravil Khusainov on 09/04/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import LattisCore
import FirebaseCore
import FirebaseCrashlytics
import FirebaseMessaging

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        configureFirebase()
        AppRouter.shared.launch(application: application, launchOptions: launchOptions)
        window = UIWindow(frame: UIScreen.main.bounds)
        window?.rootViewController = AppRouter.shared.rootController(with: MapViewContainer.self)
        window?.makeKeyAndVisible()
        return true
    }
    
    func configureFirebase() {
        let fileName: String
        #if RELEASE
        fileName = "GoogleService-Info"
        #elseif BETA
        fileName = "GoogleService-Info_Beta"
        #else
        fileName = "GoogleService-Info_Dev"
        #endif
        let file = Bundle.main.path(forResource: fileName, ofType: "plist")!
        let config = FirebaseOptions(contentsOfFile: file)!
        FirebaseApp.configure(options: config)
    }
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }
    
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable : Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        if let action = userInfo["action"] as? String, action == "omni_vehicle_locked_manually", let tripId = userInfo["trip_id"] as? String {
            NotificationCenter.default.post(name: .vehicleLocked, object: nil, userInfo: ["trip_id": tripId])
        }
        completionHandler(.noData)
    }
}

