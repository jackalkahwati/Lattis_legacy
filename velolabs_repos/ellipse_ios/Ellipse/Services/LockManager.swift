//
//  LockManager.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/21/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import LattisSDK
import CoreLocation
import AudioToolbox
import UserNotifications
import Oval

extension UIApplication {
    var inForeground: Bool {
        return applicationState == .active
    }
}

class LockManager: NSObject {
    static let shared = LockManager()
    
    override init() {
        super.init()
        UNUserNotificationCenter.current().delegate = self
        registerNotificatoinCategory()
        self.handleError = { [weak self] error in
            self?.handle(error: error)
        }
        EllipseManager.shared.subscribe(handler: self)
    }
    
    var navigate: (String) -> () = {_ in}
    
    fileprivate let storage: EllipseStorage & ContactStorage = CoreDataStack.shared
    fileprivate var handler: StorageHandler?
    fileprivate var connected: Set<String> = []
    fileprivate var timer: Timer?
    fileprivate var emergenctTimer: Timer?
    fileprivate let network: LocksNetwork = Session.shared
    fileprivate var handleError: ((Error) -> ())!
    fileprivate weak var ellipse: Peripheral?
    fileprivate var crash: Crash? {
        didSet {
            sendEmergencyMessage()
        }
    }
    fileprivate var canSendCrash: Bool = false {
        didSet {
            sendEmergencyMessage()
        }
    }
    
    func check(peripheral: Peripheral) {
        if let p = ellipse, p != peripheral, p.isPaired {
            p.disconnect()
        }
        self.ellipse = peripheral
    }
    
    fileprivate func subscribeLaunch() {
        NotificationCenter.default.addObserver(self, selector: #selector(showBLEWarning), name: UIApplication.didBecomeActiveNotification, object: nil)
    }
    
    fileprivate func unsubscribeLaunch() {
        NotificationCenter.default.removeObserver(self)
    }
    
    @objc fileprivate func showBLEWarning() {
        AlertView.alert(title: "warning".localized(), text: "request_bluetooth_alert".localized()).show()
    }
}

extension LockManager: EllipseManagerDelegate {
    func manager(_ lockManager: EllipseManager, didUpdateConnectionState connected: Bool) {
        if connected {
            unsubscribeLaunch()
            lockManager.scan()
        } else if UIApplication.shared.inForeground {
            showBLEWarning()
        } else {
            subscribeLaunch()
        }
    }
}

extension LockManager: EllipseDelegate {
    func ellipse(_ ellipse: Peripheral, didUpdate connection: Peripheral.Connection) {
        switch connection {
        case .paired:
            handler = storage.ellipse(macId: ellipse.macId) { (ell) in
                ellipse.accelerometer.theftLimit = .init(ell.sensorSensitivity)
                if ell.isCrashEnabled {
                    ellipse.accelerometer.subscribeCrash(handler: self)
                }
                if ell.isTheftEnabled {
                    ellipse.accelerometer.subscribeTheft(handler: self)
                }
            }
        case .unpaired:
            connected.remove(ellipse.macId)
        default:
            break
        }
        log(.custom(.lock), attributes: [.status("\(connection)")])
    }
    
    func ellipse(_ ellipse: Peripheral, didUpdate security: Peripheral.Security) {
        switch security {
        case .locked:
            connected.insert(ellipse.macId)
        default:
            connected.remove(ellipse.macId)
        }
        log(.custom(.lock), attributes: [.status("\(security)")])
    }
}

extension LockManager: CrashPresentable, TheftPresentable {
    func handleCrash(value: Accelerometer.Value, for peripheral: Peripheral) {
        guard timer == nil else { return }
        log(.custom(.crashAlert), attributes: [.status("oserved")])
        AudioServicesPlayAlertSound(kSystemSoundID_Vibrate)
        LocationTracker.shared.location { [unowned self] location in
            let info = Crash.Info(macId: peripheral.macId, accelerometerValue: Ellipse.AccelerometerValue(value), location: location.coordinate)
            self.network.crashDetected(info: info) { [weak self] result in
                switch result {
                case .success(let crash):
                    self?.crash = crash
                case .failure:
                    break
                }
            }
        }
        let state = UIApplication.shared.applicationState
        if state == .background || state == .inactive {
            presentLocalNotification(category: .crash)
            emergenctTimer = Timer.scheduledTimer(withTimeInterval: 30, repeats: false, block: { [unowned self] (_) in
                self.emergenctTimer?.invalidate()
                self.emergenctTimer = nil
                self.canSendCrash = true
            })
        } else {
            CrashAlertView.alert { [unowned self] in
                self.canSendCrash = true
                }.show()
        }
    }
    
    func handleTheft(value: Accelerometer.Value, for peripheral: Peripheral) {
        guard connected.contains(peripheral.macId), timer == nil else { return }
        log(.custom(.theftAlert), attributes: [.status("observed")])
        AudioServicesPlayAlertSound(kSystemSoundID_Vibrate);
        let state = UIApplication.shared.applicationState
        if state == .background || state == .inactive {
            presentLocalNotification(category: .theft, userInfo: ["macId": peripheral.macId])
        } else {
            AlertView.theft { [unowned self] in
                self.navigate(peripheral.macId)
                log(.custom(.theftAlert), attributes: [.status("located")])
                }.show()
        }
    }
}

private extension LockManager {
    func sendEmergencyMessage() {
        guard let crash = crash, canSendCrash else { return }
        let contacts = storage.getEmergency()
        let message = Contact.Emergency(crashId: crash.crahId, contacts: contacts)
        network.send(emergency: message) { [weak self] result in
            switch result {
            case .success:
                self?.canSendCrash = false
                self?.crash = nil
                log(.custom(.crashAlert), attributes: [.status("sent")])
                AlertView.alert(title: "alert.crash.emergency.sent.success.title", text: "alert.crash.emergency.sent.success.message").show()
            case .failure:
                AlertView.alert(title: "alert.crash.emergency.sent.error.title", text: "alert.crash.emergency.sent.error.message").show()
            }
        }
    }
    
    func presentLocalNotification(category: NotificationCategory, userInfo: [AnyHashable: Any] = [:]) {
        let content = UNMutableNotificationContent()
        content.title = category.title
        content.body = category.body
        content.sound = .default
        content.categoryIdentifier = category.rawValue
        content.userInfo = userInfo
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 0.1, repeats: false)
        let request = UNNotificationRequest(identifier: category.rawValue, content: content, trigger: trigger)
        UNUserNotificationCenter.current().add(request, withCompletionHandler: { (error) in
            
        })
    }
    
    func handle(error: Error) {
        canSendCrash = false
        crash = nil
    }
}


extension Ellipse.AccelerometerValue {
    init(_ value: Accelerometer.Value) {
        self.ave = Coordinate(x: value.mav.x, y: value.mav.y, z: value.mav.z)
        self.dev = Coordinate(x: value.deviation.x, y: value.deviation.y, z: value.deviation.z)
    }
}

enum NotificationAction: String {
    case ignore, locate, emergency
}

enum NotificationCategory: String {
    case crash, theft
    
    var title: String {
        switch self {
        case .crash:
            return "crash_alert".localized()
        default:
            return "theft_alert".localized()
        }
    }
    
    var body: String {
        switch self {
        case .crash:
            return "crash_alert_notification_body".localized()
        default:
            return "theft_alert_notification_body".localized()
        }
    }
}

extension LockManager: UNUserNotificationCenterDelegate {
    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        guard let action = NotificationAction(rawValue: response.actionIdentifier) else { return completionHandler() }
        switch action {
        case .locate:
            guard let macId = response.notification.request.content.userInfo["macId"] as? String else { return }
            navigate(macId)
        case .emergency:
            canSendCrash = true
            emergenctTimer?.invalidate()
            emergenctTimer = nil
        case .ignore:
            emergenctTimer?.invalidate()
            emergenctTimer = nil
        }
        completionHandler()
    }
}

fileprivate func registerNotificatoinCategory() {
    let ignore = UNNotificationAction(identifier: NotificationAction.ignore.rawValue, title: "ignore".localized(), options: [])
    let emergency = UNNotificationAction(identifier: NotificationAction.emergency.rawValue, title: "send_emergency".localized(), options: [])
    let locate = UNNotificationAction(identifier: NotificationAction.locate.rawValue, title: "locate_ellipse".localized(), options: [.foreground])
    let crash = UNNotificationCategory(identifier: NotificationCategory.crash.rawValue, actions: [ignore, emergency], intentIdentifiers: [], options: [])
    let theft = UNNotificationCategory(identifier: NotificationCategory.theft.rawValue, actions: [ignore, locate], intentIdentifiers: [], options: [])
    UNUserNotificationCenter.current().setNotificationCategories([crash, theft])
}
