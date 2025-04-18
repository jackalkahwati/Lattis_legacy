//
//  AppDelegate.swift
//  GoScoot
//
//  Created by Ravil Khusainov on 20.11.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import LattisCore
import FirebaseCore
import FirebaseCrashlytics

@main
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        configureFirebase()
        UITheme.theme = .goscoot
        AppRouter.shared.launch(application: application, launchOptions: launchOptions)
        window = UIWindow(frame: UIScreen.main.bounds)
        window?.rootViewController = AppRouter.shared.rootController(with: MapViewContainer.self)
        window?.makeKeyAndVisible()
        return true
    }
    
    func configureFirebase() {
        let fileName = "GoogleService-Info"
        let file = Bundle.main.path(forResource: fileName, ofType: "plist")!
        let config = FirebaseOptions(contentsOfFile: file)!
        FirebaseApp.configure(options: config)
    }

    // MARK: UISceneSession Lifecycle

    @available(iOS 13.0, *)
    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        // Called when a new scene session is being created.
        // Use this method to select a configuration to create the new scene with.
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    @available(iOS 13.0, *)
    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
        // Called when the user discards a scene session.
        // If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
        // Use this method to release any resources that were specific to the discarded scenes, as they will not return.
    }


}

