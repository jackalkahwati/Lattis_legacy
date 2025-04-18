//
//  AppRouter.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 07/06/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import OvalAPI
import EllipseLock
import KeychainSwift
import SideMenu
import Wrappers
import Reachability
import Stripe
import CoreLocation
import FirebaseCrashlytics
import FirebaseMessaging
import UserNotifications
import TapkeyMobileLib

public class AppRouter: NSObject {
    typealias Networks = UserAPI & PaymentNetwork
        & BikeAPI & TripAPI & FileNetwork & ParkingAPI
        & ServiceNetwork & ReservationsNetwork & GeofenceAPI & SubscriptionsAPI & FleetsNetwork & HubsAPI & AppsAPI & PromotionAPI
    public static let shared = AppRouter()
    fileprivate let reachability = try? Reachability()
    fileprivate let locationManager = CLLocationManager()
    fileprivate var cardStorage: CardStorage?
    fileprivate weak var app: UIApplication?
//    fileprivate let tapkey = TKMServiceFactoryBuilder().build()
    var root: UIViewController? {
        var rootController = UIApplication.shared.windows.first(where: {$0.isKeyWindow})?.rootViewController
        while let presented = rootController?.presentedViewController {
            rootController = presented
        }
        return rootController
    }
        
    override init() {
        Session.userAgent = UITheme.theme.userAgent
        print("User Agent:", Session.userAgent)
        if let lang = Locale.current.languageCode {
            Session.contentLanguage = lang
        }
        Session.shared.storage = KeychainSwift()
        #if RELEASE
        Session.shared.debugLogs = false
        #endif
        super.init()
        installCheck()
        handleInternetConnection()
        configureBLE()
        configureStripe()
        logoutTapkey()
    }
    
    public var mapClass: MapRepresentable.Type!
    public var notificationToken: String = ""
    public var userDirUrl: URL {
        return FileManager.default.userDirectoryUrl(for: Session.shared.storage.userId)
    }
    
    weak var activity: ActivityViewController?
    weak var alert: AlertController?
    
    @UserDefaultsBacked(key: "connectedMacId", defaultValue: nil)
    var macId: String?
    
    @UserDefaultsBacked(key: "wasInstalledBefore", defaultValue: false)
    fileprivate var wasInstalledBefore: Bool

    fileprivate var rootController: UINavigationController!
    fileprivate weak var connectionAlert: MessageOverlayView?
    fileprivate weak var bleAlert: AlertController?
    fileprivate var ble: EllipseManager? = nil
    @UserDefaultsBacked(key: "tripIdToHandleDocking")
    fileprivate var tripIdToHandleDocking: Int?
    @UserDefaultsBacked(key: "tripIdToUnlock")
    fileprivate var tripIdToUnlock: Int?
    @UserDefaultsBacked(key: "tripIdToEndRide")
    fileprivate var tripIdToEndRide: Int?
    @UserDefaultsBacked(key: "tripIdToShowSummary")
    public var tripIdToShowSummary: Int?
    fileprivate var isGeofenceAlertShown: Bool = false
    fileprivate var needToCheckNotificationPermissions: Bool = false
    
    public func map(_ root: OverMap) -> MapRepresentable {
        let map = mapClass.init(root)
        if let r = root as? MapTopViewController {
            map.topController = r
        }
        return map
    }
    
    public func launch(application: UIApplication, launchOptions: [UIApplication.LaunchOptionsKey: Any]?) {
        if let notification = launchOptions?[UIApplication.LaunchOptionsKey.remoteNotification] as? [AnyHashable: Any],
           let trip = notification["trip_id"] as? String, notification["action"] == nil,
                 let tripId = Int(trip) {
            tripIdToHandleDocking = tripId
            tripIdToShowSummary = tripId
        }
        tripIdToShowSummary = nil
        app = application
        if needToCheckNotificationPermissions {
            askNotificationPermission(app: application)
        }
    }
    
    public func rootController(with mapClass: MapRepresentable.Type) -> UIViewController {
        self.mapClass = mapClass
        let root: UIViewController
        if Session.shared.storage.userId == nil {
            root = WelcomeViewController()
        } else {
            root = self.createDashboard()
        }
        rootController = .init(rootViewController: root)
        rootController.isNavigationBarHidden = true
        return rootController
    }
    
    public var addPrivateNetwork: () -> () = {}
    public var onLogin: () -> () = {}
    var lockInfo: LockSwitchInfo? {
        didSet {
            self.connectionAlert?.update(info: lockInfo)
        }
    }
    var onInfoUpdate: (Status.Info?, Status) -> () = {_, _ in}
    weak var dashboard: DashboardViewController?
    
    public func loggedIn(userId: Int, completion: @escaping () -> ()) {
        Crashlytics.crashlytics().setUserID("\(userId)")
        CoreDataStack.shared.setup(Bundle(identifier: "io.lattis.LattisCore")!, name: "Lattis", userId: userId) {
            completion()
            self.refreshCards()
            self.onLogin()
            Analytics.set(user: userId)
        }
    }
    
    public func logOut() {
        Analytics.log(.logout)
        tripIdToUnlock = nil
        tripIdToEndRide = nil
        tripIdToHandleDocking = nil
        rootController.setViewControllers([WelcomeViewController(LogInViewController.init)], animated: true)
        KeychainSwift().clear()
    }
    
    public func checIfLoggedIn(completion: @escaping (Bool) -> ()) {
        #if DEBUG
        // Jack - Dev
//        Session.shared.storage.restToken = "a3e340e6b6f0b29b9e13fd60be19be0e619f5e3c76e99073e0b47ecb74916b9fc38019703b689cf89ed3f287faf22730"
//        Session.shared.storage.userId = 888
//        Session.shared.storage.refreshToken = nil
        
        // Jack - Prod
//        Session.shared.storage.restToken = "a3e340e6b6f0b29b9e13fd60be19be0e05234c3b011af1de0573fb7ec7a2c132c47899cb51afb4f4d2bc50edaccf079d"
//        Session.shared.storage.userId = 8771
//        Session.shared.storage.refreshToken = nil
        
        // Jeremy - Prod
//        Session.shared.storage.restToken = "f63d73d6749734f58222a6e931ea5b78b1f61196bf26434dec00c7c882b4cca17a6387abf519a8945fdbd9645aa65ca9"
//        Session.shared.storage.userId = 5195
//        Session.shared.storage.refreshToken = nil
        
        // Marcus - Prod
//        Session.shared.storage.restToken = "66cc47318c545a2c9bab59f8783dd573b313aa4ff2d8cded69d702e1611d8cbf2954fc0a14400e1caaebbcdb8f5c2959"
//        Session.shared.storage.userId = 14134
//        Session.shared.storage.refreshToken = nil
        
//        Session.shared.storage.restToken = "b71ee2fbfeba71c266956e3d68126e79bc1eab9186cb74ee4c99b43eb1801b5da02324befebdf6d7e7d8f0fc7d45225e"
//        Session.shared.storage.userId = 1280
//        Session.shared.storage.refreshToken = nil

        // Jeremy - Dev
//        Session.shared.storage.restToken = "f63d73d6749734f58222a6e931ea5b78cc709389977e2808e07c4e66f4d192a01b17df6d6427aad6b098def6029d0772"
//        Session.shared.storage.userId = 970
//        Session.shared.storage.refreshToken = nil
        
        // Jeremy - Dev jeremyricard@gmail.com
//        Session.shared.storage.restToken = "ccbeeb99a5e499c607f7b91651d456afb3ba1fb894ce725027109e4b5672ee1ad8a449e62aa63e064793aeeb64402ea2"
//        Session.shared.storage.userId = 877
//        Session.shared.storage.refreshToken = nil
        
        // Marcus - Dev
//        Session.shared.storage.restToken = "b71ee2fbfeba71c266956e3d68126e79bc1eab9186cb74ee4c99b43eb1801b5d032bbc3902d3febab35f9e7119811c13"
//        Session.shared.storage.refreshToken = nil
//        Session.shared.storage.userId = 1267
        
        // Marcus - Dev  marcus@lattis.io
//        Session.shared.storage.restToken = "66cc47318c545a2c9bab59f8783dd57366a10fb50735db7c06a58ce99f38378b2987015c55c183d1217b1d7b1a2c8ba5"
//        Session.shared.storage.refreshToken = nil
//        Session.shared.storage.userId = 1269
        
        // Generic
//        Session.shared.storage.restToken = "90e86d0b9992f8a54a315514a2069827a2cf0bcd2cc502d1670a9fa1739181f49d0b93f3201d1ec6d12b255b5b366e96"
//        Session.shared.storage.refreshToken = nil
//        Session.shared.storage.userId = 14045
        
        #endif
        if let userId = Session.shared.storage.userId {
            loggedIn(userId: userId) {
                completion(true)
            }
        } else {
            completion(false)
        }
    }
    
    func api() -> Networks {
        return Session.shared
    }
    
    func openDashboard() {
        self.rootController.setViewControllers([self.createDashboard()], animated: false)
    }
    
    func handleInternetConnection() {
        reachability?.stopNotifier()
        reachability?.whenReachable = { _ in
            self.connectionAlert?.hide()
            NotificationCenter.default.post(name: .internetConnection, object: true)
        }
        reachability?.whenUnreachable = showNoInternet(reach: )
        try? reachability?.startNotifier()
    }
    
    func showBleAlert(from controller: UIViewController) {
        if connectionAlert != nil {
            connectionAlert?.hide()
        }
        let alert = AlertController.bluetooth
        bleAlert = alert
        controller.present(alert, animated: true, completion: nil)
    }
    
    func showGeofenceAlert() {
        guard !isGeofenceAlertShown else { return }
        let alert = AlertController(title: "notice".localized(), body: "geo_fence_warning".localized())
        alert.actions = [
            .plain(title: "ok".localized()) {
                self.isGeofenceAlertShown = false
            },
        ]
        root?.present(alert, animated: true)
        isGeofenceAlertShown = true
    }
    
    func showInvalidQRCodeAlert() {
        guard !isGeofenceAlertShown else { return }
        let alert = AlertController(title: "general_error_title".localized(), body: "qr_code_not_match".localized())
        alert.actions = [
            .plain(title: "ok".localized()) {
                self.isGeofenceAlertShown = false
            },
        ]
        root?.present(alert, animated: true)
        isGeofenceAlertShown = true
    }
    
    var hintMessage: String? {
        #if targetEnvironment(simulator)
        return nil
        #endif
        
        // Check if location enabled
        guard CLLocationManager.locationServicesEnabled(),
            locationManager.authorizationStatus != .denied else {
                return "location_access_hint".localized()
        }
        
        // Check if BLE initialize otherwise its not supported
        if ble != nil {
            // Check if bluetooth is on
            if !ble!.isOn {
                return "bluetooth_access_alert_message".localized()
            }
            return nil
        }
        return nil
    }

    fileprivate func configureStripe() {
        #if RELEASE
        StripeAPI.defaultPublishableKey = "pk_live_BsQdrCcaqSjqF9pbVUTBpOZL"
        #elseif BETA
        StripeAPI.defaultPublishableKey = "pk_live_BsQdrCcaqSjqF9pbVUTBpOZL"
        #else
        StripeAPI.defaultPublishableKey = "pk_test_T3mQMkP37Tcn0bWLwQfQnrAn"
        #endif
    }
    
    func configureBLE() {
        #if NONBLE
        print("NO BLE SUPPORT for this App")
        #else
        print("BLE SUPPORTED for this App")
        ble = .shared
        ble!.api = Session.shared
        ble!.restoringStrategy = .reconnect
        ble!.subscribe(handler: self)
        #endif
    }
    
    fileprivate func requestLocationUpdates() {
        guard locationManager.authorizationStatus == .notDetermined else { return }
        locationManager.requestWhenInUseAuthorization()
    }
    
    fileprivate func showNoInternet(reach: Reachability) {
        guard connectionAlert == nil else { return }
        let title = (lockInfo != nil || macId != nil) ? "internet_is_required_in_ride" : "internet_is_required"
        let alert = MessageOverlayView(title.localized(), info: lockInfo)
        alert.show()
        self.connectionAlert = alert
        NotificationCenter.default.post(name: .internetConnection, object: false)
    }
    
    fileprivate func installCheck() {
        guard !wasInstalledBefore else { return }
        wasInstalledBefore = true
        KeychainSwift().clear()
    }
    
    fileprivate func createDashboard() -> UIViewController {
        let menu = SideMenuViewController()
        let map = mapClass.init(DashboardViewController())
        let controller = SideMenuController(contentViewController: map, menuViewController: menu)
        MenuUI.shared.conroller = controller
        return controller
    }
    
    fileprivate func refreshCards() {
        cardStorage = .init()
        cardStorage?.refresh { _ in }
    }
    
    fileprivate func askNotificationPermission(app: UIApplication) {
        needToCheckNotificationPermissions = false
        let center = UNUserNotificationCenter.current()
        center.requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            if granted {
                DispatchQueue.main.async(execute: app.registerForRemoteNotifications)
                self.setupPushNotifications()
            } else if let e = error {
                print(e)
            }
            self.requestLocationUpdates()
        }
    }
    
    func setupPushNotifications() {
        Messaging.messaging().token { token, error in
            // eVM40S9jiUc9oD1KBfyfpt:APA91bHtNpjA3snTmZSrc_7nvkK9AWN31Pmjpg8OnLXGo3Xa4C19UCdppZBVeQdE9VPV8lXxPGQ_c8mot2i3fymX3vmKQLNmZLm3WnJjwGxaC2ZLTdtrS7Fr19NoWuqm92dahValpLPa
            print("APNS: Token: \(token)")
            self.notificationToken = token ?? ""
        }
        
        let unlock = UNNotificationAction(identifier: UNNotificationResponse.Action.dockingUnlock.rawValue, title: "unlock".localized(), options: .foreground)
        let endRide = UNNotificationAction(identifier: UNNotificationResponse.Action.dockingEndRide.rawValue, title: "end_ride".localized(), options: .foreground)
        let docking = UNNotificationCategory(identifier: UNNotification.Category.docking.rawValue, actions: [unlock, endRide], intentIdentifiers: [])
        UNUserNotificationCenter.current().setNotificationCategories([docking])
        UNUserNotificationCenter.current().delegate = self
    }
    
    fileprivate func handle(notification: UNNotification) {
        print("Notification Payload: \(notification.request.content.userInfo)")
        if notification.doesMatch(category: .docking) {
            NotificationCenter.default.post(name: .vehicleDocked, object: nil, userInfo: notification.request.content.userInfo)
            if let tripId: Int = notification.userInfo(key: .trip_id) {
                tripIdToHandleDocking = tripId
            }
        }
        if notification.doesMatch(category: .docked) {
            NotificationCenter.default.post(name: .smartDocking, object: nil, userInfo: notification.request.content.userInfo)
            if let tripId: Int = notification.userInfo(key: .trip_id) {
                tripIdToShowSummary = tripId
            }
        }
        if notification.doesMatch(category: .locked) {
            NotificationCenter.default.post(name: .vehicleLocked, object: nil, userInfo: notification.request.content.userInfo)
        }
        if notification.doesMatch(category: .reservationEndingSoon) {
            NotificationCenter.default.post(name: .reservationEndingSoon, object: nil, userInfo: notification.request.content.userInfo)
        }
        if notification.doesMatch(category: .sentinelOpen) {
            NotificationCenter.default.post(name: .sentinelOpen, object: nil, userInfo: notification.request.content.userInfo)
        }
        if notification.doesMatch(category: .sentinelClosed) {
            NotificationCenter.default.post(name: .sentinelClose, object: nil, userInfo: notification.request.content.userInfo)
        }
        if notification.doesMatch(category: .sentinelOnline) {
            NotificationCenter.default.post(name: .sentinelOnline, object: nil, userInfo: notification.request.content.userInfo)
        }
    }
    
    fileprivate func logoutTapkey() {
//        let userManager = tapkey.userManager
//        for id in userManager.users {
//            userManager.logOutAsync(userId: id, cancellationToken: TKMCancellationTokens.None)
//                .conclude()
//        }
    }
}

extension AppRouter: EllipseManagerDelegate {
    public func manager(_ lockManager: EllipseManager, didUpdateConnectionState connected: Bool) {
        if connected {
            bleAlert?.dismiss(animated: true, completion: nil)
            if let reach = reachability, reach.connection == .unavailable {
                showNoInternet(reach: reach)
            }
        } else if macId != nil {
            showBleAlert(from: rootController)
        }
        if let application = app {
            askNotificationPermission(app: application)
        } else {
            needToCheckNotificationPermissions = true
        }
        NotificationCenter.default.post(name: .ble, object: connected)
    }
}

extension AppRouter: UNUserNotificationCenterDelegate {
    public func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        handle(notification: notification)
        completionHandler([.sound, .banner])
    }
    
    public func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        guard let action = response.action else { return handle(notification: response.notification) }
        switch action {
        case .dockingUnlock:
            NotificationCenter.default.post(name: .dockingUnlock, object: nil, userInfo: response.notification.request.content.userInfo)
            if let tripId: Int = response.notification.userInfo(key: .trip_id){
                tripIdToUnlock = tripId
            }
        case .dockingEndRide:
            NotificationCenter.default.post(name: .dockingEndRide, object: nil, userInfo: response.notification.request.content.userInfo)
            if let tripId: Int = response.notification.userInfo(key: .trip_id) {
                tripIdToEndRide = tripId
            }
        }
        completionHandler()
    }
}

extension User {
    var photoPath: String? {
        guard let userId = Session.shared.storage.userId else { return nil }
        let directory = FileManager.default.userDirectory(for: userId) + "/profile_photo"
        return directory
    }
    
    func save(photo: UIImage?) {
        guard let userId = Session.shared.storage.userId else { return }
        let url = FileManager.default.userDirectoryUrl(for: userId).appendingPathComponent("profile_photo")
        if let p = photo {
            let data = p.jpegData(compressionQuality: 1)
            try? data?.write(to: url)
        } else {
            try? FileManager.default.removeItem(at: url)
        }
    }
}

