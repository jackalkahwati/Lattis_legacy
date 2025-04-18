//
//  AppDelegate+OM.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 08/03/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import FirebaseCrashlytics
import LattisSDK
import Oval
import Reachability
import UserNotifications
import KeychainSwift
import Firebase

fileprivate let pushTokenKey = "pushTokenKey"
fileprivate let loginKey = "isLoggedIn"

extension AppDelegate {
    static var shared: AppDelegate {
        return UIApplication.shared.delegate as! AppDelegate
    }
    
    var navigation: UINavigationController {
        return window!.rootViewController as! UINavigationController
    }
    
    func start() {
        configureFirebase()
        registerForNotifications()
        UIBarButtonItem.appearance().setTitleTextAttributes([NSAttributedString.Key.font: UIFont.systemFont(ofSize: 13)], for: .normal)
        window = UIWindow(frame: UIScreen.main.bounds)
        Session.shared.storage = KeychainSwift()
        EllipseManager.shared.restoringStrategy = .disconnect
        EllipseManager.shared.cashingStrategy = .never
        EllipseManager.shared.network = Session.shared
        if let userId = Session.shared.storage.userId {
            CoreDataStack.shared.setup(userId: userId, completion: {})
        }
        let navigation = UINavigationController(rootViewController: initialController)
        navigation.isNavigationBarHidden = true
        window?.rootViewController = navigation
        window?.makeKeyAndVisible()
        handleInternetConnection()
//        Session.shared.removeLock(macId: "F16CDF328973", success: {}, fail: {print($0)})
    }
    
    var initialController: UIViewController {
        return isLoggedIn ? DashboardRouter.navigation() : WelcomeRouter.instantiate()
    }
    
    func logout() {
        EllipseManager.shared.clean()
        isLoggedIn = false
        let welcome = WelcomeRouter.instantiate()
        navigation.setViewControllers([welcome], animated: true)
    }
    
    
    var isLoggedIn: Bool {
        get {
            return Session.shared.storage.restToken != nil && UserDefaults.standard.bool(forKey: loginKey)
        }
        set {
            if newValue == false {
                 Session.shared.logout()
            }
            UserDefaults.standard.set(newValue, forKey: loginKey)
            UserDefaults.standard.synchronize()
        }
    }
    
    var pushToken: String {
        set {
            UserDefaults.standard.set(newValue, forKey: pushTokenKey)
        }
        get {
            let token = UserDefaults.standard.string(forKey: pushTokenKey)
            return token ?? ""
        }
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
    
    private func registerForNotifications() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.sound, .alert, .badge]) { (granted, error) in
            
        }
        UIApplication.shared.registerForRemoteNotifications()
    }
    
    func handleInternetConnection() {
        reachability?.stopNotifier()
        reachability?.whenReachable = { _ in
            self.connectionAlert?.hide()
        }
        reachability?.whenUnreachable = showNoInternet(reach: )
        try? reachability?.startNotifier()
    }
    
    fileprivate func showNoInternet(reach: Reachability) {
        let alert = StaticAlertView.alert(with: "general_no_internet_text".localized())
        alert.show()
        self.connectionAlert = alert
    }
}
