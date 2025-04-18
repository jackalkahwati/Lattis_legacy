//
//  SLLockViewController.swift
//  Skylock
//
//  Created by Andre Green on 6/5/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit
import Crashlytics
import RestService
import MapKit
import KeychainSwift
import FBSDKLoginKit
import SwiftyJSON

let hideMenuNotification = NSNotification.Name(rawValue: "hideMenuNotification")

@objc class SLLockViewController:
SLBaseViewController,
SLSlideViewControllerDelegate,
SLLocationManagerDelegate,
SLAcceptNotificationsViewControllerDelegate,
SLThinkerViewControllerDelegate,
SLNotificationViewControllerDelegate,
SLCrashNotificationViewControllerDelegate,
SLLockBarViewControllerDelegate
{
    let xPadding:CGFloat = 13.0
    
    var lock:SLLock?
    fileprivate var crashAlertLock = false
    let lockManager = SLLockManager.sharedManager
    
    let databaseManager = SLDatabaseManager.shared()
    
    var isMapShowing = false
    var isSharing = false
    let locksService = LocksService()
    var emergencyMessage: Oval.Locks.EmergencyMessage? {
        didSet {
            sendEmergrncyMessage()
        }
    }
    var shouldSendEmergency = false
    var theft: (Int?, Bool)? {
        didSet {
            confirmTheft()
        }
    }
    var shouldConfirmTheft = false
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
    
    lazy var acceptNotificationViewController:SLAcceptNotificationsViewController = {
        let anvc:SLAcceptNotificationsViewController = SLAcceptNotificationsViewController()
        anvc.delegate = self
        
        return anvc
    }()
    
    lazy var locationManager:SLLocationManager = {
        let locManager:SLLocationManager = SLLocationManager()
        locManager.delegate = self
        
        return locManager
    }()
    
    lazy var menuButton: UIButton = {
        let image:UIImage = UIImage(named: "lock_screen_hamburger_menu")!
        let button = UIButton(type: .custom)
        button.frame = CGRect(
            x: self.xPadding,
            y: UIApplication.shared.statusBarFrame.size.height + 20.0,
            width: 2*image.size.width,
            height: 2*image.size.height
        )
        button.addTarget(self, action: #selector(menuButtonPressed), for: .touchDown)
        button.setImage(image, for: .normal)
        
        return button
    }()
    
    lazy var underLineView:UIView = {
        let frame = CGRect(
            x: self.xPadding,
            y: self.view.bounds.size.height - 80.0,
            width: self.view.bounds.size.width - 2.0*self.xPadding,
            height: 1.0
        )
        
        let view:UIView = UIView(frame: frame)
        view.backgroundColor = UIColor.white
        view.isHidden = true
        
        return view
    }()
    
    lazy var lockNameLabel:UILabel = {
        let labelWidth = self.underLineView.bounds.size.width
        let font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 18.0)
        let height:CGFloat = 22.0
        let frame = CGRect(
            x: 0.5*(self.view.bounds.size.width - labelWidth),
            y: self.underLineView.frame.minY - height - 7.0,
            width: labelWidth,
            height: height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = UIColor.white
        label.text = self.lock?.displayName
        label.textAlignment = .left
        label.font = font
        label.numberOfLines = 1
        label.isHidden = true
        
        return label
    }()
    
    
    let manageButtonsContainer = UIView()
    
    let crashButton: SLImageTextButton = {
        let button = SLImageTextButton(image: #imageLiteral(resourceName: "icon_crash_detection"), onText: "lock_screen_crash_detect_on".localized(), offText: "lock_screen_crash_detect_off".localized())
        return button
    }()
    
    let autoLockButton: SLImageTextButton = {
        let button = SLImageTextButton(image: #imageLiteral(resourceName: "icon_auto_lock"), onText: "lock_screen_auto_lock_on".localized(), offText: "lock_screen_auto_lock_off".localized())
        return button
    }()
    
    let autoUnLockButton: SLImageTextButton = {
        let button = SLImageTextButton(image: #imageLiteral(resourceName: "icon_auto_unlock"), onText: "lock_screen_auto_unlock_on".localized(), offText: "lock_screen_auto_unlock_off".localized())
        return button
    }()
    
    let theftButton: SLImageTextButton = {
        let button = SLImageTextButton(image: #imageLiteral(resourceName: "icon_theft_detection"), onText: "lock_screen_theft_detect_on".localized(), offText: "lock_screen_theft_detect_off".localized())
        return button
    }()
    
    lazy var batteryView:UIImageView = {
        let image:UIImage = UIImage(named: "battery4")!
        let frame = CGRect(
            x: self.underLineView.frame.maxX - image.size.width,
            y: self.lockNameLabel.frame.midY - 0.5*image.size.height,
            width: image.size.width,
            height: image.size.height
        )
        
        let view:UIImageView = UIImageView(frame: frame)
        view.image = image
        
        return view
    }()
    
    lazy var rssiView:UIImageView = {
        let image:UIImage = UIImage(named: "rssi4")!
        let frame = CGRect(
            x: self.batteryView.frame.minX - image.size.width - 20.0,
            y: self.lockNameLabel.frame.midY - 0.5*image.size.height,
            width: image.size.width,
            height: image.size.height
        )
        
        let view:UIImageView = UIImageView(frame: frame)
        view.image = image
        
        return view
    }()

    lazy var thinkerViewController:SLThinkerViewController = {
        let text:[SLThinkerViewControllerLabelTextState:String] = [
            .clockwiseTopStill: NSLocalizedString("LOCKED", comment: ""),
            .clockwiseBottomStill: NSLocalizedString("Tap to unlock", comment: ""),
            .clockwiseTopMoving: NSLocalizedString("Locking...", comment: ""),
            .counterClockwiseTopStill: NSLocalizedString("UNLOCKED", comment: ""),
            .counterClockwiseBottomStill: NSLocalizedString("Tap to lock", comment: ""),
            .counterClockwiseTopMoving: NSLocalizedString("Unlocking...", comment: ""),
            .inactiveTop: NSLocalizedString("NOT", comment: ""),
            .inactiveBottom: NSLocalizedString("CONNECTED", comment: ""),
            .connectingTop: NSLocalizedString("CONNECTING...", comment: ""),
            .connectingBottom: NSLocalizedString("", comment: "")
        ]
        
        let tvc:SLThinkerViewController = SLThinkerViewController(
            texts: text,
            firstBackgroundColor: UIColor.white,
            secondBackgroundColor: UIColor(red: 102, green: 177, blue: 227),
            foregroundColor: UIColor(red: 60, green: 83, blue: 119),
            inActiveBackgroundColor: UIColor(red: 130, green: 156, blue: 178),
            textColor: UIColor.white
        )
        tvc.delegate = self
        
        return tvc
    }()
    
    weak var mapController: MapViewController?
    var mapViewController: MapViewController {
        let map: MapViewController = mapController ?? MapViewController()
        mapController = map
        return map
    }
    
    var shareInstructionViewController:SLShareInstructionViewController = {
        let sivc:SLShareInstructionViewController = SLShareInstructionViewController()
        return sivc
    }()
    
    lazy var slideViewController:SLSlideViewController = {
        let slvc = SLSlideViewController()
        slvc.delegate = self
        
        return slvc
    }()
    
    lazy var touchCatcherView:UIView = {
        let tgr:UITapGestureRecognizer = UITapGestureRecognizer(
            target: self,
            action: #selector(removeSlideViewController)
        )
        
        let view:UIView = UIView(frame: self.view.bounds)
        view.addGestureRecognizer(tgr)
        view.backgroundColor = UIColor.clear
        
        return view
    }()
    
    lazy var unconnectedView:UIView = {
        let width = self.view.bounds.size.width - 2.0*self.xPadding
        let height:CGFloat = 60.0
        let viewFrame = CGRect(
            x: self.xPadding,
            y: self.view.bounds.size.height - height - 20.0,
            width: width,
            height: height
        )
        
        let view:UIView = UIView(frame: viewFrame)
        
        let labelFrame = CGRect(x: 0.0, y: 0.0, width: view.bounds.size.width, height: 0.5*view.bounds.size.height)
        let label:UILabel = UILabel(frame: labelFrame)
        label.text = NSLocalizedString("You are not connected to any locks.", comment: "")
        label.font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 15.0)
        label.textAlignment = .center
        label.textColor = UIColor.white
        
        view.addSubview(label)
        
        let buttonFrame = CGRect(
            x: 0.0,
            y: 0.5*view.bounds.size.height,
            width: view.bounds.size.width,
            height: 0.5*view.bounds.size.height
        )
        let button:UIButton = UIButton(type: .system)
        button.frame = buttonFrame
        button.setTitle(NSLocalizedString("Find an Ellipse to connect to.", comment: ""), for: .normal)
        button.setTitleColor(UIColor(red: 87, green: 216, blue: 255), for: .normal)
        button.titleLabel?.font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 15.0)
        button.addTarget(self, action: #selector(findEllipseButtonPressed), for: .touchDown)
        
        view.addSubview(button)
        
        return view
    }()

    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = UIColor(red: 60, green: 83, blue: 119)
        
        self.lock = self.lockManager.getCurrentLock()
        self.locationManager.beginUpdatingLocation()
        
        self.view.addSubview(self.menuButton)
        self.view.addSubview(self.unconnectedView)
        self.view.addSubview(self.underLineView)
        self.view.addSubview(self.lockNameLabel)
        self.view.addSubview(self.batteryView)
        self.view.addSubview(self.rssiView)
        
        view.addSubview(manageButtonsContainer)
        manageButtonsContainer.addSubview(crashButton)
        manageButtonsContainer.addSubview(autoLockButton)
        manageButtonsContainer.addSubview(autoUnLockButton)
        manageButtonsContainer.addSubview(theftButton)
        manageButtonsContainer.frame = {
            var frame = view.bounds
            frame.origin.x = 8
            frame.origin.y = underLineView.frame.maxY + 6
            frame.size.width -= frame.minX*2
            frame.size.height = 68
            return frame
        }()
        let distance = (manageButtonsContainer.frame.width - 60*4)/3
        autoLockButton.frame = CGRect(x: 60 + distance, y: 0, width: 60, height: 68)
        autoUnLockButton.frame = CGRect(x: autoLockButton.frame.maxX + distance, y: 0 , width: 60, height: 68)
        crashButton.frame = CGRect(x: 0, y: 0, width: 60, height: 68)
        theftButton.frame = {
            var frame = CGRect(x: 190, y: 0 , width: 60, height: 68)
            frame.origin.x = manageButtonsContainer.frame.width - frame.width
            return frame
        }()
        
        autoLockButton.addTarget(self, action: #selector(autoLockAction(_:)), for: .touchUpInside)
        autoUnLockButton.addTarget(self, action: #selector(autoUnLockAction(_:)), for: .touchUpInside)
        crashButton.addTarget(self, action: #selector(crashButtonAction(_:)), for: .touchUpInside)
        theftButton.addTarget(self, action: #selector(theftButtonAction(_:)), for: .touchUpInside)
        
        registerForNotifications()
        locksService.locks(updateCache: true) { [weak self] (_, _, error) in
            if let error = error as? Oval.Error {
                self?.reauth(with: error)
            }
        }
        checkAppVersion()
        
        if let user = SLDatabaseManager.shared().getCurrentUser() {
            autoLockButton.isOn = user.isAutoLockOn
            autoUnLockButton.isOn = user.isAutoUnlockOn
        }
    }
    
    private func reauth(with error: Oval.Error) {
        func relogin() {
            presentWarningViewControllerWithTexts(texts: [
                .Header: "WARNING".localized(),
                .Info: "You have installed the latest update of the Ellipse app. In order to finish the update please log in.".localized(),
                .CancelButton: "OK".localized()], cancelClosure: {
                if let user = SLDatabaseManager.shared().getCurrentUser() {
                    let ud:UserDefaults = UserDefaults()
                    ud.set(false, forKey: SLUserDefaultsSignedIn)
                    ud.synchronize()
                    
                    let lockManager:SLLockManager = SLLockManager.sharedManager
                    lockManager.disconnectFromCurrentLock(completion: { [unowned lockManager] in
                        lockManager.endActiveSearch()
                    })
                    KeychainSwift().clear()
                    user.isCurrentUser = false
                    if user.userType == Oval.Users.UserType.facebook.rawValue && FBSDKAccessToken.current() != nil {
                        FBSDKLoginManager().logOut()
                    }
                    SLDatabaseManager.shared().save(user, withCompletion: nil)
                    SLLockManager.sharedManager.removeAllLocks()
                    
                    
                    let svc = SLSignInViewController()
                    let login = LogInRouter.instantiateLogin(with: user.phoneNumber, configure: { $0.router.delegate = svc })
                    let appDelegate = UIApplication.shared.delegate as! SLAppDelegate
                    let navigation = appDelegate.window.rootViewController as? UINavigationController
                    navigation?.setViewControllers([svc, login], animated: true)
                    
                    Answers.logCustomEvent(withName: "LogOut", customAttributes: nil)
                }
            })
        }
        guard error != .missingRefreshToken else { return relogin() }
        guard let user = SLDatabaseManager.shared().getCurrentUser(),
            let password = user.password else { return relogin() }
        Oval.users.getTokens(userId: user.userId, password: password, success: { [weak self] in
            self?.locksService.locks(updateCache: true)
        }, fail: { _ in relogin() })
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
    
        if !self.view.subviews.contains(self.thinkerViewController.view) {
            let diameter:CGFloat = 245.0
            self.thinkerViewController.view.frame = CGRect(
                x: 0.5*(self.view.bounds.size.width - diameter),
                y: 0.5*(self.view.bounds.size.height - diameter) - 50.0,
                width: diameter,
                height: diameter
            )
            
            self.addChildViewController(self.thinkerViewController)
            self.view.addSubview(self.thinkerViewController.view)
            self.view.bringSubview(toFront: self.thinkerViewController.view)
            self.thinkerViewController.didMove(toParentViewController: self)
            self.thinkerViewController.setState(state: .inactive)
        }
        
        self.lock = self.lockManager.getCurrentLock()
        self.toggleViewsHiddenOnConnction(isConnected: self.lock != nil)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        self.lockManager.checkCurrentLockOpenOrClosed()
        self.showAcceptNotificaitonViewController()
        
    }

    func registerForNotifications() {
        // TODO: Get UI to handle the case where the lock position is invalid or middle.
        NotificationCenter.default.addObserver(self, selector: #selector(lockOpened(notification:)), name: NSNotification.Name(rawValue: kSLNotificationLockPositionOpen), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(lockLocked(notification:)), name: NSNotification.Name(rawValue: kSLNotificationLockPositionLocked), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(lockStuck(notification:)), name: NSNotification.Name(rawValue: kSLNotificationLockPositionMiddle), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(lockDisconneted(notification:)), name: NSNotification.Name(rawValue: kSLNotificationLockManagerDisconnectedLock), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(lockPaired(notification:)), name: NSNotification.Name(rawValue: kSLNotificationLockPaired), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(theftOrCrashAlert(notification:)), name: NSNotification.Name(rawValue: kSLNotificationAlertOccured), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(startedConnectingLock(notification:)), name: NSNotification.Name(rawValue: kSLNotificationLockManagerStartedConnectingLock), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(hardwareValuesUpdated(notification:)), name: NSNotification.Name(rawValue: kSLNotificationLockManagerUpdatedHardwareValues), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(lockConnectionError(notification:)), name: NSNotification.Name(rawValue: kSLNotificationLockManagerErrorConnectingLock), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(lockNameChanged(notification:)), name: NSNotification.Name(rawValue: kSLNotificationLockNameChanged), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(removeSlideViewController), name: hideMenuNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(blePoweredOff(notification:)), name: Notification.Name(rawValue: kSLNotificationLockManagerBlePoweredOff), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(firmwareRead(notification:)), name: NSNotification.Name(rawValue: kSLNotificationLockManagerReadFirmwareVersion), object: nil)
    }
    
    func showAcceptNotificaitonViewController() {
        let ud = UserDefaults.standard;
        if !ud.bool(forKey: SLUserDefaultsOnBoardingComplete) {
            self.present(
                self.acceptNotificationViewController,
                animated: true,
                completion: nil
            )
        }
    }
    
    func menuButtonPressed() {
        let width:CGFloat = self.view.bounds.size.width - 80.0
        self.slideViewController.view.frame = CGRect(
            x: -width,
            y: 0.0,
            width: width,
            height: self.view.bounds.size.height
        )
        
        self.addChildViewController(self.slideViewController)
        self.view.addSubview(self.slideViewController.view)
        self.view.bringSubview(toFront: self.slideViewController.view)
        self.slideViewController.didMove(toParentViewController: self)
        
        UIView.animate(withDuration: 0.4, animations: {
            self.slideViewController.view.frame = CGRect(
                x: 0.0,
                y: 0.0,
                width: width,
                height: self.view.bounds.size.height
            )
        }) { (finished) in
            self.view.insertSubview(
                self.touchCatcherView,
                belowSubview: self.slideViewController.view
            )
        }
    }
    
    func theftButtonAction(_ sender: SLImageTextButton) {
        guard let user = self.databaseManager.getCurrentUser() else {
            return
        }
        
        if self.lock == nil {
            return
        }
        
        user.areCrashAlertsOn = false
        user.areTheftAlertsOn = !user.areTheftAlertsOn
        self.databaseManager.save(user, withCompletion: nil)
        
        self.theftButton.isOn = user.areTheftAlertsOn
        self.crashButton.isOn = false
    }
    
    func crashButtonAction(_ sender: SLImageTextButton) {
        guard let user = self.databaseManager.getCurrentUser() else {
            return
        }
        
        if self.lock == nil {
            return
        }
        
        user.areTheftAlertsOn = false
        if user.areCrashAlertsOn == false && databaseManager.emergencyContacts().isEmpty {
            let texts:[SLWarningViewControllerTextProperty:String?] = [
                .Header: "IMPORTANT".localized(),
                .Info: "Please select your preferred emergency contacts before turning on crash alerts.".localized(),
                .CancelButton: "CANCEL".localized(),
                .ActionButton: "CONTINUE".localized()
            ]
            self.presentWarningViewControllerWithTexts(texts: texts, cancelClosure: nil, actionClosure: { [weak self] in
                self?.closeWarning()
                Timer.after(0.3, {
                    if let this = self {
                        this.handleAction(svc: this.slideViewController, action: .EmergencyContacts)
                    }
                })
            })
            return
        }
        user.areCrashAlertsOn = !user.areCrashAlertsOn
        self.databaseManager.save(user, withCompletion: nil)
        
        self.crashButton.isOn = user.areCrashAlertsOn
        self.theftButton.isOn = false
    }
    
    func autoLockAction(_ sender: SLImageTextButton) {
        let dbManager = SLDatabaseManager.shared()
        guard let user = dbManager.getCurrentUser() else {
            print("Error: could not assign auto lock/unlock property to current user. No current user in db")
            return
        }
        sender.isOn = !sender.isOn
        user.isAutoLockOn = sender.isOn
        dbManager.save(user, withCompletion: nil)
        if let macId = self.lock?.macId {
            SLLockManager.sharedManager.armLock(macId: macId)
        }
    }
    
    func autoUnLockAction(_ sender: SLImageTextButton) {
        sender.isOn = !sender.isOn
        let dbManager = SLDatabaseManager.shared()
        guard let user = dbManager.getCurrentUser() else {
            print("Error: could not assign auto lock/unlock property to current user. No current user in db")
            return
        }
        user.isAutoUnlockOn = sender.isOn
        dbManager.save(user, withCompletion: nil)
        if let macId = self.lock?.macId {
            if let lock = lock, lock.isLocked {
                SLLockManager.sharedManager.disarmLock(macId: macId)
                DispatchQueue.main.asyncAfter(deadline: .now() + 2, execute: { 
                    if user.isAutoLockOn {
                        SLLockManager.sharedManager.armLock(macId: macId)
                    }
                })
            }
        }
    }
    
    func findEllipseButtonPressed() {
        let alvc = SLAvailableLocksViewController()
        self.presentViewControllerWithNavigationController(viewController: alvc, showBottomBar: true)
    }
    
    func lockOpened(notification: Notification) {
        self.thinkerViewController.setState(state: .counterClockwiseStill)
        if let lock = self.lock, let location = self.databaseManager.getCurrentUser()?.location {
            lock.location = location
            lock.isLocked = false
            self.databaseManager.save(lock)
        }
        
        //MARK: - Analytics
        Answers.logCustomEvent(withName: "Unlocked", customAttributes: nil)
    }
    
    func lockLocked(notification: Notification) {
        self.thinkerViewController.setState(state: .clockwiseStill)
        if let lock = self.lock, let location = self.databaseManager.getCurrentUser()?.location {
            lock.location = location
            lock.isLocked = true
            self.databaseManager.save(lock)
        }
        
        //MARK: - Analytics
        Answers.logCustomEvent(withName: "Locked", customAttributes: nil)
    }
    
    func lockStuck(notification: Notification) {
        self.thinkerViewController.setState(state: .clockwiseStill)
        let texts:[SLWarningViewControllerTextProperty:String?] = [
            .Header: NSLocalizedString("Warning", comment: ""),
            .Info: "Your shackle isn't properly inserted.  Please remove and re-insert the shackle into the lock bar.".localized(),

            .CancelButton: NSLocalizedString("OK", comment: ""),
            .ActionButton: nil
        ]
        
        self.presentWarningViewControllerWithTexts(texts: texts, cancelClosure: nil)
    }
    
    func lockRemoved(notification: Notification) {
        self.thinkerViewController.setState(state: .inactive)
        self.setLockDisabled()
    }
    
    func lockPaired(notification: Notification) {
        if let lock = self.lockManager.getCurrentLock(), let macId = lock.macId, lockManager.isConnecedLock(with: macId) {
            self.lock = lock
            self.lockNameLabel.text = lock.displayName
            self.lockNameLabel.setNeedsDisplay()
            self.lockManager.checkCurrentLockOpenOrClosed()
            self.lockNameLabel.textColor = .white
            self.toggleViewsHiddenOnConnction(isConnected: true)
        }
        checkUpdate()
        Answers.logCustomEvent(withName: "Lock paired", customAttributes: nil)
    }
    
    func lockDisconneted(notification: Notification) {
        if let disconnectedAddress = notification.object as? String,
            let lock = lockManager.getCurrentLock(),
            lock.macId != disconnectedAddress {
            Answers.logCustomEvent(withName: "Lock disconnected error", customAttributes: ["user": databaseManager.getCurrentUser()!.usersId!,
                                                                                           "lock": lock.macId!,
                                                                                           "disconnect": disconnectedAddress])
            return
        }

        setLockDisabled()
    }
    
    func hardwareValuesUpdated(notification: Notification) {
        guard let macAddress = notification.object as? String else {
            return
        }
        
        if self.lock != nil && self.lock?.macId == macAddress {
            print("\(lock?.batteryVoltage), \(lock?.rssiStrength)")
            DispatchQueue.main.async {
                self.batteryView.image = self.batteryImageForCurrentLock()
                self.rssiView.image = self.rssiImageForCurrentLock()
                self.batteryView.setNeedsDisplay()
                self.rssiView.setNeedsDisplay()
            }
        }
    }
    
    func theftOrCrashAlert(notification: Notification) {
        guard let alertNotification = notification.object as? SLNotification, let lock = lock else {
            return
        }
        
        if alertNotification.type == SLNotificationType.crashPre, crashAlertLock == false {
            crashAlertLock = true
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.minute, execute: { [weak self] in
                self?.crashAlertLock = false
            })
            let cnvc = SLCrashNotificationViewController(
                takeActionButtonTitle: "ALERT MY CONTACTS",
                cancelButtonTitle: "CANCEL, I'M OK",
                titleText: NSLocalizedString("Crash detected!", comment: ""),
                infoText: NSLocalizedString("Your emergency contacts will be alerted in", comment: "")
            )
            cnvc.crashDelegate = self
            cnvc.delegate = self
            
            if let presentedController = self.presentedViewController, presentedController is UINavigationController || presentedController.isKind(of: BottomBarViewController.self) {
                presentedController.present(cnvc, animated: true, completion: nil)
            } else {
                self.present(cnvc, animated: true, completion: nil)
            }
            
            guard let macId = alertNotification.macId,
                let lock = SLDatabaseManager.shared().getLockWithMacId(macId),
                let data = notification.userInfo as? [String: Any] else { return }
            
            let accValue = AccelerometerValue(
                x: data["x_ave"] as? Float ?? 0.0,
                y: data["y_ave"] as? Float ?? 0.0,
                z: data["z_ave"] as? Float ?? 0.0,
                xDev: data["x_dev"] as? Float ?? 0.0,
                yDev: data["y_dev"] as? Float ?? 0.0,
                zDev: data["z_dev"] as? Float ?? 0.0
            )
            
            let crashInfo = Oval.Locks.CrashInfo(macId: macId, accelerometerValue: accValue, location: lock.location)
            Oval.locks.crashDetected(info: crashInfo, success: { [weak self] (crash) in
                self?.emergencyMessage?.crashId = crash.crashId
            }, fail: { error in
                
            })
            
            guard let contacts = databaseManager.emergencyContactsForCurrentUser() else {
                print(
                    "Error: could not send crash notification. " +
                    "The current user does not have any emergency contacts."
                )
                return
            }
            
            emergencyMessage = Oval.Locks.EmergencyMessage(contacts: contacts.flatMap({ $0.ovalContactValue }), macId: macId, location: lock.location)
            
            Answers.logCustomEvent(withName: "Crash alert", customAttributes: nil)
        } else if alertNotification.type == SLNotificationType.theft && lock.isLocked {
            let tnvc:SLTheftNotificationViewController = SLTheftNotificationViewController(
                takeActionButtonTitle: "LOCATE MY BIKE",
                cancelButtonTitle: "OK, GOT IT",
                titleText: NSLocalizedString("Theft detected!", comment: ""),
                infoText: NSLocalizedString("We think someone may be tampering with your bike.", comment: "")
            )
            tnvc.delegate = self
            
            if let presentedController = self.presentedViewController, presentedController is UINavigationController || presentedController.isKind(of: BottomBarViewController.self) {
                presentedController.present(tnvc, animated: true, completion: nil)
            } else {
                self.present(tnvc, animated: true, completion: nil)
            }
            
            guard let macId = alertNotification.macId,
                let lock = SLDatabaseManager.shared().getLockWithMacId(macId),
                let data = notification.userInfo as? [String: Any] else { return }
            
            let accValue = AccelerometerValue(
                x: data["x_ave"] as? Float ?? 0.0,
                y: data["y_ave"] as? Float ?? 0.0,
                z: data["z_ave"] as? Float ?? 0.0,
                xDev: data["x_dev"] as? Float ?? 0.0,
                yDev: data["y_dev"] as? Float ?? 0.0,
                zDev: data["z_dev"] as? Float ?? 0.0
            )
            
            let theftInfo = Oval.Locks.CrashInfo(macId: macId, accelerometerValue: accValue, location: lock.location)
            Oval.locks.theftDetected(info: theftInfo, success: { [weak self] (crash) in
                self?.theft = (crash.crashId, self?.theft?.1 ?? false)
                }, fail: { error in
                    
            })
            
            Answers.logCustomEvent(withName: "Theft alert", customAttributes: nil)
        }
    }
    
    func startedConnectingLock(notification: Notification) {
        self.thinkerViewController.setState(state: .connecting)
        self.unconnectedView.isHidden = true
        self.underLineView.isHidden = true
        self.lockNameLabel.isHidden = true
        manageButtonsContainer.isHidden = true
        self.batteryView.isHidden = true
        self.rssiView.isHidden = true
    }
    
    func lockConnectionError(notification: Notification) {
        setLockDisabled()
        if self.viewIfLoaded?.window == nil {
            return
        }
        
        guard let notificationObject = notification.object as? [String: Any?] else {
            print("no connection error in notification for method: lockConnectionError")
            return
        }
        
        guard let info = notificationObject["message"] as? String else {
            print("no connection error messsage in notification for method: lockConnectionError")
            return
        }
        let header = (notificationObject["header"] as? String) ?? "FAILED TO CONNECT".localized()
        let texts:[SLWarningViewControllerTextProperty:String?] = [
            .Header: header,
            .Info: info,
            .CancelButton: NSLocalizedString("OK", comment: ""),
            .ActionButton: nil
        ]
        
        self.presentWarningViewControllerWithTexts(texts: texts, cancelClosure: nil)
        
        Answers.logCustomEvent(withName: "Lock error", customAttributes: ["error": info])
    }
    
    func lockNameChanged(notification: Notification) {
        DispatchQueue.main.async {
            self.lock = self.databaseManager.getCurrentLockForCurrentUser()
            self.lockNameLabel.text = self.lock?.givenName
        }
    }
    
    func blePoweredOff(notification: Notification) {
        let texts:[SLWarningViewControllerTextProperty:String?] = [
            .Header: "Bluetooth Turned Off".localized(),
            .Info: "Please turn ON Bluetooth in order to pair to your Ellipse.",
            .CancelButton: "OK".localized(),
            .ActionButton: nil
        ]
        
        self.presentWarningViewControllerWithTexts(texts: texts, cancelClosure: nil)
    }
    
    func setLockDisabled() {
        self.lock = nil
        self.thinkerViewController.setState(state: .inactive)
        self.lockNameLabel.text = ""
        self.toggleViewsHiddenOnConnction(isConnected: false)
    }
    
    func toggleViewsHiddenOnConnction(isConnected: Bool) {
        self.unconnectedView.isHidden = isConnected
        self.underLineView.isHidden = !isConnected
        self.lockNameLabel.isHidden = !isConnected
        manageButtonsContainer.isHidden = !isConnected
        self.batteryView.isHidden = !isConnected
        self.rssiView.isHidden = !isConnected
    }
    
    func removeSlideViewController() {
        self.isMapShowing = false
        UIView.animate(withDuration: 0.4, animations: {
            self.slideViewController.view.frame = CGRect(
                x: -self.slideViewController.view.bounds.size.width,
                y: 0.0,
                width: self.slideViewController.view.bounds.size.width,
                height: self.slideViewController.view.bounds.size.height
            )
        }) { (finished) in
            self.slideViewController.view.removeFromSuperview()
            self.slideViewController.removeFromParentViewController()
            self.touchCatcherView.removeFromSuperview()
        }
    }

    
    func presentViewControllerWithNavigationController(viewController: UIViewController, showBottomBar: Bool = false) {
        let nc = UINavigationController(rootViewController: viewController)
        nc.navigationBar.barStyle = .black
        nc.navigationBar.tintColor = .white
        nc.modalPresentationStyle = .currentContext
        nc.navigationBar.barTintColor = .slBluegrey
        nc.isNavigationBarHidden = false
        present(nc, animated: true, completion: nil)
    }
    
    func rssiImageForCurrentLock() -> UIImage? {
        guard let lock = self.lock else {
            return nil
        }
        
        let imageName:String
        let range = lock.range(forParameter: .rssi)
        switch range {
        case .zero:
            imageName = "rssi0"
        case .one:
            imageName = "rssi1"
        case .two:
            imageName = "rssi2"
        case .three:
            imageName = "rssi3"
        case .four:
            imageName = "rssi4"
        }
        
        return UIImage(named: imageName)
    }
    
    func batteryImageForCurrentLock() -> UIImage? {
        guard let lock = self.lock else {
            return nil
        }
        
        let imageName:String
        let range = lock.range(forParameter: .battery)
        switch range {
        case .zero:
            imageName = "battery0"
        case .one:
            imageName = "battery1"
        case .two:
            imageName = "battery2"
        case .three:
            imageName = "battery3"
        case .four:
            imageName = "battery4"
        }
        
        return UIImage(named: imageName)
    }
    
    func sendEmergrncyMessage() {
        guard let message = emergencyMessage, shouldSendEmergency else { return }
        shouldSendEmergency = false
        self.emergencyMessage = nil
        
        Oval.locks.send(emergency: message, success: { [weak self] in
            let texts:[SLWarningViewControllerTextProperty:String?] = [
                .Header: "Emergency Message Sent".localized(),
                .Info: "Your emergency contacts have been notified that your Ellipse has detected a crash.".localized(),
                .CancelButton: "OK".localized(),
                .ActionButton: nil
            ]
            self?.presentWarningViewControllerWithTexts(texts: texts, cancelClosure: nil, actionClosure: nil)
        }, fail: { [weak self] error in
            let texts:[SLWarningViewControllerTextProperty:String?] = [
                .Header: "Emergency Message Failed".localized(),
                .Info: "Your emergency contacts could not be notified that your Ellipse has detected a crash. There was a problem contacting our servers.".localized(),
                .CancelButton: "OK".localized(),
                .ActionButton: nil
            ]
            self?.presentWarningViewControllerWithTexts(texts: texts, cancelClosure: nil, actionClosure: nil)
        })
    }
    
    func confirmTheft() {
        guard let theft = theft, let theftId = theft.0, shouldConfirmTheft else { return }
        self.theft = nil
        shouldConfirmTheft = false
        
        Oval.locks.confirm(theft: theftId, isConfirmed: theft.1, success: {
            
        }, fail: { error in
            
        })
    }
    
    // MARK: SLSLideViewControllerDelegate methods
    func handleAction(svc: SLSlideViewController, action: SLSlideViewControllerAction) {
        switch action {
        case .EllipsesPressed:
            let ldvc = SLLockDetailsViewController()
            self.presentViewControllerWithNavigationController(viewController: ldvc, showBottomBar: true)
        case .FindMyEllipsePressed:
            self.isMapShowing = true
            self.presentViewControllerWithNavigationController(viewController: self.mapViewController, showBottomBar: true)
        case .SharingPressed:
            let defaults = UserDefaults.standard
            let shareInstrcution: Bool  = defaults.bool(forKey: "shareInstructionBool")
            if !shareInstrcution {
                //let defaults = UserDefaults.standard
                defaults.set(true, forKey: "shareInstructionBool")
                defaults.synchronize()

                self.presentViewControllerWithNavigationController(viewController: self.shareInstructionViewController)
            } else {
                self.presentViewControllerWithNavigationController(viewController: SLSharingViewController())
            }

        case .ProfileAndSettingPressed:
            self.presentViewControllerWithNavigationController(viewController: SLProfileViewController())
        case .EmergencyContacts:
            guard let user = SLDatabaseManager.shared().getCurrentUser() else {
                return
            }
            
            if (((user.firstName == nil) || (user.lastName == nil)) || (user.firstName!.isEmpty) || (user.lastName!.isEmpty)){
                let texts:[SLWarningViewControllerTextProperty:String?] = [
                    .Header: "Unable to process".localized(),
                    .Info: "Please fill out your First and Last name in the Profile and Settings section.".localized(),
                    .CancelButton: "CANCEL".localized(),
                    .ActionButton: "PROFILE".localized()
                ]
                self.presentWarningViewControllerWithTexts(texts: texts, cancelClosure: nil, actionClosure:{ [weak self] in
                    self?.presentViewControllerWithNavigationController(viewController: SLProfileViewController())
                    self?.closeWarning()
                })
                return
            }
            let contactHandler = SLContactHandler()
            if contactHandler.authorizedToAccessContacts() {
                let ecvc = SLEmergencyContactsViewController()
                self.presentViewControllerWithNavigationController(viewController: ecvc)
            } else {
                let rcvc = SLRequestContactsAccessViewController()
                self.presentViewControllerWithNavigationController(viewController: rcvc)
            }
        case .HelpPressed:
            let webView = SLWebViewController(baseUrl: .Help)
            self.presentViewControllerWithNavigationController(viewController: webView)
            
            Answers.logCustomEvent(withName: "Help screen open", customAttributes: nil)
        case .RateTheAppPressed:
            print("rate the app pressed")
            Answers.logCustomEvent(withName: "Rate app", customAttributes: nil)
        case .InviteFriendsPressed:
            print("Invite friends pressed")
        case .OrderNowPressed:
            let webView = SLWebViewController(baseUrl: .Skylock)
            self.presentViewControllerWithNavigationController(viewController: webView)
            Answers.logCustomEvent(withName: "Clicks to website", customAttributes: nil)
        case .TermsAndConditions:
            presentViewControllerWithNavigationController(viewController: TermsAndConditionsViewController.stroryboard)
            Answers.logCustomEvent(withName: "Open terms and conditions", customAttributes: nil)
        case .home:
            removeSlideViewController()
        }
        
    }
    
    // MARK: SLLocationManagerDelegate methods
    func locationManagerUpdatedUserPosition(locationManager: SLLocationManager, userLocation: CLLocation) {
        let dbManager = SLDatabaseManager.shared()
        guard let user = dbManager.getCurrentUser() else {
            return
        }
        
        let oldPoint = MKMapPointForCoordinate(user.location)
        let newPoint = MKMapPointForCoordinate(userLocation.coordinate)
        let distance = MKMetersBetweenMapPoints(oldPoint, newPoint)
        
        if self.isMapShowing {
            self.mapViewController.userPosition = userLocation.coordinate
        }
        
        guard abs(distance) > 3 else { return } // Checkig if user location was changed from previous one with proximity of 3 metters
        
        user.location = userLocation.coordinate
        dbManager.save(user, withCompletion: nil)
        
        if let lock = dbManager.getCurrentLockForCurrentUser(), let macId = lock.macId, lockManager.isConnecedLock(with: macId) {
            lock.location = userLocation.coordinate
            dbManager.save(lock)
        }
    }
    
    func locationManagerDidAcceptedLocationAuthorization(locationManager: SLLocationManager, didAccept: Bool) {
       self.acceptNotificationViewController.setBackgroundImageForCurrentStep()
    }
    
    // MARK: SLAcceptNotificationViewControllerDelegate Methods
    func userWantsToAcceptLocationUse(acceptNotificationsVC: SLAcceptNotificationsViewController) {
        self.locationManager.requestAuthorization()
    }
    
    func userWantsToAcceptsNotifications(acceptNotificationsVC: SLAcceptNotificationsViewController) {
        let appDelegate:SLAppDelegate = UIApplication.shared.delegate as! SLAppDelegate
        appDelegate.setUpNotficationSettings()
    }
    
    func acceptsNotificationsControllerWantsExit(
        acceptNotiticationViewController: SLAcceptNotificationsViewController,
        animated: Bool
        )
    {
        let userDefaults = UserDefaults.standard
        userDefaults.set(true, forKey: "SLUserDefaultsOnBoardingComplete")
        userDefaults.synchronize()
        
        self.dismiss(animated: true, completion: nil)
    }
    
    // MARK: SLThinkerViewControllerDelegate methods
    func thinkerViewTapped(tvc: SLThinkerViewController) {
        guard let lockPosition = self.lock?.lockPosition else {
            print("Error: could not get lock position when thinker view was tapped.")
            return
        }
        
        guard let position = SLLockPosition(rawValue: UInt8(lockPosition)) else {
            print(
                "Error: could not get lock position when thinker view was tapped. "
                + "The value is outside SLLockPosition enum values"
            )
            return
        }
        
    self.thinkerViewController.setState(state: position == .locked ? .counterClockwiseMoving : .clockwiseMoving)
        self.lockManager.toggleLockOpenedClosedShouldLock(shouldLock: position != .locked)
        if autoLockButton.isOn {
            if position == .locked {
                if let macId = self.lock?.macId {
                    SLLockManager.sharedManager.armLock(macId: macId)
                }
            } else {
                autoLockAction(autoLockButton)
            }
        }
        if position == .locked && autoUnLockButton.isOn {
            autoUnLockAction(autoUnLockButton)
        }
    }
    
    // MARK: SLNotificationViewControllerDelegate methods
    func takeActionButtonPressed(nvc: SLNotificationViewController) {
        let notificationManager:SLNotificationManager = SLNotificationManager.sharedManager() as! SLNotificationManager
        var completion:(() -> Void)?
        if let notification:SLNotification = notificationManager.lastNotification() {
            if notification.type == SLNotificationType.crashPre {
                shouldSendEmergency = true
                sendEmergrncyMessage()
            } else if notification.type == SLNotificationType.theft {
                completion = {
                    self.isMapShowing = true
                    self.presentViewControllerWithNavigationController(viewController: self.mapViewController, showBottomBar: true)
                }
                shouldConfirmTheft = true
                theft = (theft?.0, true)
                confirmTheft()
            }
            
            notificationManager.removeLastNotification()
        }
        
        nvc.dismiss(animated: true, completion: completion)
    }
    
    func cancelButtonPressed(nvc: SLNotificationViewController) {
        let notificationManager:SLNotificationManager = SLNotificationManager.sharedManager() as! SLNotificationManager
        notificationManager.removeLastNotification()
        nvc.dismiss(animated: true, completion: nil)
        if let notification = notificationManager.lastNotification(), notification.type == .theft {
            shouldConfirmTheft = true
            theft = (theft?.0, false)
            confirmTheft()
        }
    }
    
    // MARK: SLCrashNotificationViewControllerDelegate methods
    func timerExpired(cnvc: SLCrashNotificationViewController) {
        let notificationManager:SLNotificationManager = SLNotificationManager.sharedManager() as! SLNotificationManager
        if let notification:SLNotification = notificationManager.lastNotification() {
            if notification.type == SLNotificationType.crashPre {
                shouldSendEmergency = true
                sendEmergrncyMessage()
            }
            
            notificationManager.removeLastNotification()
        }

        cnvc.dismiss(animated: true, completion: nil)
    }
    
    // MARK: SLLockBarViewControllerDelegate Methods
    func lockBarTapped(lockBar: SLLockBarViewController) {
        self.dismiss(animated: true, completion: {
            self.removeSlideViewController()
        })
    }
    
    func firmwareRead(notification: Notification) {
        guard let firmware = notification.object as? String else {
            return
        }
        
        func showDialog() {
            guard view.window != nil else { return }
            let texts:[SLWarningViewControllerTextProperty:String?] = [
                .Header: "fw_update_avaliable_title".localized(),
                .Info: "fw_update_avaliable_text".localized(),
                .CancelButton: "fw_update_avaliable_cancel".localized(),
                .ActionButton: "fw_update_avaliable_action".localized()
            ]
            presentWarningViewControllerWithTexts(texts: texts, cancelClosure: nil) {
                let controller = SLFirmwareUpdateViewController(firmwareVersionString: firmware)
                self.present(controller, animated: true, completion: nil)
            }
        }
        
        func checkLatest(versions: [String], currentFirmwareVersion: String) {
            guard let last = versions.last else { return }
            let clearLast = last.replacingOccurrences(of: ".", with: "")
            let clearCurrent = currentFirmwareVersion.replacingOccurrences(of: ".", with: "")
            if clearLast > clearCurrent {
                showDialog()
            }
        }
        
        Oval.locks.firmvareVersions(success: { (versions) in
            let trimming = CharacterSet(charactersIn: "0")
            let ver = versions.map({ $0.trimmingCharacters(in: trimming) }).sorted(by: <)
            checkLatest(versions: ver, currentFirmwareVersion: firmware)
            }, fail: { _ in})
    }
    
    func checkUpdate() {
        guard view.window != nil else { return }
        SLLockManager.sharedManager.readFirmwareDataForCurrentLock()
    }
    
    func checkAppVersion() {
        func showDialog() {
            let texts:[SLWarningViewControllerTextProperty:String?] = [
                .Header: "app_update_avaliable_title".localized(),
                .Info: "app_update_avaliable_text".localized(),
                .CancelButton: "app_update_avaliable_cancel".localized(),
                .ActionButton: "app_update_avaliable_action".localized()
            ]
            presentWarningViewControllerWithTexts(texts: texts, cancelClosure: nil) {
                UIApplication.shared.openURL(URL(string: "itms://itunes.apple.com/us/app/ellipse-lock/id1119377215?ls=1&mt=8")!)
            }
        }
        DispatchQueue.global(qos: .default).async {
            let version = Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as! String
            let appId = Bundle.main.object(forInfoDictionaryKey: "CFBundleIdentifier") as! String
            let url = URL(string: String(format: "http://itunes.apple.com/lookup?bundleId=%@", appId))!
            do {
                let data = try Data(contentsOf: url)
                let json = JSON(data: data)
                print(json)
                if json["resultCount"].intValue == 1  {
                    guard let appVer = json["results"].arrayValue[0]["version"].string else {
                        return
                    }
                    if appVer > version {
                        DispatchQueue.main.async(execute: showDialog)
                    }
                }
            } catch {
                print(error)
            }
        }
    }
}

extension SLEmergencyContact {
    var ovalContactValue: Oval.Locks.Contact? {
        guard let phoneNumber = phoneNumber else { return nil }
        return Oval.Locks.Contact(firstName: firstName, lastName: lastName, phoneNumber: phoneNumber, countryCode: countyCode)
    }
}
