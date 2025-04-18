//
//  AppDelegate+Lattis.swift
//  Lattis
//
//  Created by Ravil Khusainov on 19/04/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import LattisSDK
import Oval
import CoreLocation
import FirebaseCrashlytics
import EasyTipView
import UserNotifications
import KeychainSwift
import Firebase
import Stripe

fileprivate let pushTokenKey = "pushTokenKey"
fileprivate let loginKey = "isLoggedIn"

extension AppDelegate {
    
    var pushToken: String {
        set {
            UserDefaults.standard.set(newValue, forKey: pushTokenKey)
        }
        get {
            let token = UserDefaults.standard.string(forKey: pushTokenKey)
            return token ?? ""
        }
    }
    
    var isLoggedIn: Bool {
        get {
            return Session.shared.storage.restToken != nil && UserDefaults.standard.bool(forKey: loginKey)
        }
        set {
            UserDefaults.standard.set(newValue, forKey: loginKey)
            UserDefaults.standard.synchronize()
        }
    }
    
    static var shared: AppDelegate {
        return UIApplication.shared.delegate as! AppDelegate
    }
    var navigation: UINavigationController {
        return window!.rootViewController as! UINavigationController
    }
    
    private func loggedIn(userId: Int) {
        Crashlytics.crashlytics().setUserID("\(userId)")
        CoreDataStack.shared.setup(userId: userId, completion: AppRouter.shared.checkCurrentStatus)
        isLoggedIn = true
        widget.loggedIn = true
    }
    
    func start() {
        configureStripe()
        configureFirebase()
        AppRouter.shared.handleInternetConnection()
        customizeDefaultAppearance()
        registerForNotifications()
        let nav: UINavigationController
        if let userId = Session.shared.storage.userId, isLoggedIn {
            loggedIn(userId: userId)
            nav = UINavigationController(rootViewController: FindRideRouter.menu(configure: AppRouter.setup(with:)))
        } else {
            nav = UINavigationController(rootViewController: WelcomeRouter.instantiate(mapInitializer: AppRouter.setup(with:), loginSucceded: {
                self.loggedIn(userId: Session.shared.storage.userId!)
            }))
        }
        nav.isNavigationBarHidden = true
        
        window = UIWindow(frame: UIScreen.main.bounds)
        window?.rootViewController = nav
        window?.makeKeyAndVisible()
        checkAppVersion()
    }
    
    func customizeDefaultAppearance() {
        // Tip view
        var preferences = EasyTipView.Preferences()
        preferences.drawing.font = .systemFont(ofSize: 12)
        preferences.drawing.foregroundColor = .white
        preferences.drawing.backgroundColor = .lsCoolGreyThree
        preferences.drawing.arrowPosition = .any
        preferences.drawing.textAlignment = .right
        EasyTipView.globalPreferences = preferences
        
        //
        Session.shared.storage = KeychainSwift()
        EllipseManager.shared.network = Session.shared
        EllipseManager.shared.restoringStrategy = .disconnect
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
    
    func configureStripe() {
        let key: String
        #if RELEASE
        key = "pk_live_BsQdrCcaqSjqF9pbVUTBpOZL"
        #elseif BETA
        key = "pk_live_BsQdrCcaqSjqF9pbVUTBpOZL"
        #else
        key = "pk_test_T3mQMkP37Tcn0bWLwQfQnrAn"
        #endif
        Stripe.setDefaultPublishableKey(key)
    }
    
    func checkAppVersion() {
        func showDialog() {
            let alert = ActionAlertView.alert(title: "app_update_avaliable_title".localized(), subtitle: "app_update_avaliable_text".localized())
            alert.action = AlertAction(title: "app_update_avaliable_action".localized(), action: { UIApplication.shared.open(URL(string: "itms://itunes.apple.com/us/app/lattis/id1235042268?ls=1&mt=8")!, options: [:], completionHandler: nil) })
            alert.cancel = AlertAction(title: "app_update_avaliable_cancel".localized(), action: {})
            alert.show()
        }
        DispatchQueue.global(qos: .default).asyncAfter(deadline: .now() + 5) {
            guard let version = Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String,
                let appId = Bundle.main.object(forInfoDictionaryKey: "CFBundleIdentifier") as? String,
                let url = URL(string: String(format: "http://itunes.apple.com/lookup?bundleId=%@", appId)) else { return }
            struct Result: Decodable {
                let resultCount: Int
                let results: [Ver]
                
                struct Ver: Decodable {
                    let version: String
                }
            }
            do {
                let data = try Data(contentsOf: url)
                let json = try JSONDecoder().decode(Result.self, from: data)
                guard let appVer = json.results.first?.version else {
                    return
                }
                if appVer > version {
                    DispatchQueue.main.async(execute: showDialog)
                }
            } catch {
                print(error)
            }
        }
    }

    
    func logout() {
        AppRouter.shared.currentState = .none
        EllipseManager.shared.clean()
        Session.shared.logout()
        KeychainSwift().clear()
        isLoggedIn = false
        let welcome = WelcomeRouter.instantiate(mapInitializer: AppRouter.shared.logout(with:), loginSucceded: {
            self.loggedIn(userId: Session.shared.storage.userId!)
        })
        navigation.setViewControllers([welcome], animated: true)
        
        widget.loggedIn = false
    }
    
    private func registerForNotifications() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.sound, .alert, .badge]) { (granted, error) in
            
        }
        UIApplication.shared.registerForRemoteNotifications()
    }
}

