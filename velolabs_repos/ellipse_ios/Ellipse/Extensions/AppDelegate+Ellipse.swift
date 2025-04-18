//
//  AppDelegate+Ellipse.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/11/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import LattisSDK
import KeychainSwift
import UserNotifications
import Fabric
import Crashlytics
import Oval
import EasyTipView
import Firebase
import LGSideMenuController

fileprivate let pushTokenKey = "pushTokenKey"
fileprivate let loginKey = "SLUserDefaultsSignedIn"

var isMultyLockAlertShown: Bool {
    get {
        return UserDefaults.standard.bool(forKey: "isMultyLockAlertShown")
    }
    set {
        UserDefaults.standard.set(newValue, forKey: "isMultyLockAlertShown")
        UserDefaults.standard.synchronize()
    }
}

extension AppDelegate {
    static var shared: AppDelegate {
       return UIApplication.shared.delegate as! AppDelegate
    }
   
    var navigation: UINavigationController? {
        return window?.rootViewController as? UINavigationController
    }
    
    fileprivate var storage: EllipseStorage & UserStorage {
        return CoreDataStack.shared
    }
    
    var pushToken: String {
        set {
            UserDefaults.standard.set(newValue, forKey: pushTokenKey)
            UserDefaults.standard.synchronize()
        }
        get {
            let token = UserDefaults.standard.string(forKey: pushTokenKey)
            return token ?? ""
        }
    }
    
    func start() {
        isMultyLockAlertShown = false
        isFwAlertShown = false
        configureFirebase()
        Fabric.with([Crashlytics.self])
        settingAppearance()
        registerForNotifications()
        Session.shared.storage = keychain
        EllipseManager.shared.network = Session.shared
        let conrtoller: UIViewController
        if let userId = keychain.userId, isLoggedIn {
            loggedIn(userId: userId)
            conrtoller = DashboardRouter.menu()
        } else {
            conrtoller = LogInRouter.onboarding
        }
        window = UIWindow(frame: UIScreen.main.bounds)
        let navigation = NavigationController(rootViewController: conrtoller)
        navigation.isNavigationBarHidden = true
        window?.rootViewController = navigation
        window?.makeKeyAndVisible()
    }
    
    func openDashboard() throws {
        guard let userId = keychain.userId else { throw SessionError.Code.unauthorized }
        loggedIn(userId: userId)
        navigation?.setViewControllers([DashboardRouter.menu(true)], animated: true)
    }
    
    func navigateHome() {
        if let controller = navigation?.viewControllers.last as? LGSideMenuController,
            let nav = controller.rootViewController as? UINavigationController,
            let _ = nav.viewControllers.last as? DashboardViewController {
            print("Already home")
        } else {
            navigation?.setViewControllers([DashboardRouter.menu(true)], animated: true)
        }
    }
    
    func settingAppearance() {
        UIBarButtonItem.appearance().setTitleTextAttributes([.font: UIFont.systemFont(ofSize: 13)], for: .normal)
        UIBarButtonItem.appearance().setTitleTextAttributes([.font: UIFont.systemFont(ofSize: 13)], for: .highlighted)
        UIBarButtonItem.appearance().setTitleTextAttributes([.font: UIFont.systemFont(ofSize: 13), .foregroundColor: UIColor(white: 1.0, alpha: 0.5)], for: .disabled)
        UISearchBar.appearance().tintColor = .elSteel
        
            // Tip view
        var preferences = EasyTipView.Preferences()
        preferences.drawing.font = .systemFont(ofSize: 12)
        preferences.drawing.foregroundColor = .white
        preferences.drawing.backgroundColor = .elCoolGreyThree
        preferences.drawing.arrowPosition = .any
        preferences.drawing.textAlignment = .right
        EasyTipView.globalPreferences = preferences
    }
    
    func logOut() {
        EllipseManager.shared.clean()
        KeychainSwift().clear()
        isLoggedIn = false
        Ellipse.removeCurrent()
        navigation?.setViewControllers([LogInRouter.welcome], animated: true)
        log(.custom(.logOut))
    }
    
    private func loggedIn(userId: Int) {
        CoreDataStack.shared.setup(userId: userId){
            self.updateLocks()
            self.update(userId: userId)
        }
        isLoggedIn = true
    }
    
    func updateLocks() {
        Session.shared.locks { (result) in
            switch result {
            case .success(let groups):
                let locks = groups.all
                self.storage.update(locks)
                for lock in locks {
                    if self.storage.isUserExists(with: lock.userId) == false {
                        self.update(userId: lock.userId)
                    }
                    if let userId = lock.sharedToUserId, self.storage.isUserExists(with: userId) == false {
                        self.update(userId: userId)
                    }
                }
            case .failure(let error):
                self.handle(error: error)
            }
        }
    }
    
    func update(userId: Int?) {
        Session.shared.user(userId) { (result) in
            switch result {
            case .success(let user):
                self.storage.save(user)
            case .failure(let error):
                self.handle(error: error)
            }
        }
    }
    
    func handle(error: Error) {
        
    }
    
    private func registerForNotifications() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.sound, .alert, .badge]) { (granted, error) in
            
        }
        UIApplication.shared.registerForRemoteNotifications()
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
}

extension AppDelegate {
    var isLoggedIn: Bool {
        get {
            return keychain.restToken != nil && UserDefaults.standard.bool(forKey: loginKey)
        }
        set {
            UserDefaults.standard.set(newValue, forKey: loginKey)
            UserDefaults.standard.synchronize()
        }
    }
}
