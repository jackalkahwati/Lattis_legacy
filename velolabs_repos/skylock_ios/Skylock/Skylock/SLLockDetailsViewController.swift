//
//  SLLockDetailsViewController.swift
//  Skylock
//
//  Created by Andre Green on 6/6/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit
import Crashlytics


class SLLockDetailsViewController:
SLBaseViewController,
UITableViewDelegate,
UITableViewDataSource,
SLLockSettingViewControllerDelegate
{
    var connectedLock:SLLock?
    
    let utilities = SLUtilities()
    
    let lockManager = SLLockManager.sharedManager
    
    var previousLockToConnect:SLLock?
    
    var previousLockTimer:Timer?
    
    let previousConnectionTimeout:Double = 15.0
    
    let locksService = LocksService()
    
    private var unshareLock: SLLock?
    
    lazy var tableView:UITableView = {
        let table:UITableView = UITableView(frame: self.view.bounds, style: .grouped)
        table.delegate = self
        table.dataSource = self
        table.separatorStyle = UITableViewCellSeparatorStyle.none
        table.backgroundColor = UIColor.white
        table.rowHeight = 92.0
        table.isScrollEnabled = true
        table.register(
            SLLockDetailsTableViewCell.self,
            forCellReuseIdentifier: String(describing: SLLockDetailsTableViewCell.self)
        )
        table.contentInset = UIEdgeInsets(top: 0, left: 0, bottom: 66, right: 0)
        
        return table
    }()
    
    private var unconnectedLocks = SLLockManager.sharedManager.allPreviouslyConnectedLocksForCurrentUser()
    
    lazy var dateFormatter:DateFormatter = {
        let df:DateFormatter = DateFormatter()
        df.dateFormat = "MMM d, H:mm a"
        
        return df
    }()
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = UIColor.white
        
        let addLockButton = UIBarButtonItem(
            title: NSLocalizedString("ADD NEW", comment: ""),
            style: .plain,
            target: self,
            action: #selector(addLock(_:))
        )
        self.navigationItem.rightBarButtonItem = addLockButton
        
        self.connectedLock = self.lockManager.getCurrentLock()
        
        addMenuButton(action: #selector(menuButtonPressed))
        self.navigationItem.title = NSLocalizedString("ELLIPSES", comment: "")
        self.view.addSubview(self.tableView)
        
        NotificationCenter.default.addObserver(self, selector: #selector(lockDisconnected(notification:)) , name: NSNotification.Name(rawValue: kSLNotificationLockManagerDisconnectedLock),object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(lockConnected(notification:)) , name: NSNotification.Name(rawValue: kSLNotificationLockManagerConnectedLock),object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(foundLock(notification:)) , name: NSNotification.Name(rawValue: kSLNotificationLockManagerDiscoverdLock), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(connectedLock(notificaiton:)) , name: NSNotification.Name(rawValue: kSLNotificationLockPaired), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(lockConnectionError(notification:)) , name: NSNotification.Name(rawValue: kSLNotificationLockManagerErrorConnectingLock), object: nil)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        refreshLocks(local: false)
        
        Answers.logCustomEvent(withName: "Ellipses screen open", customAttributes: nil)
    }
    
    private func refreshLocks(local: Bool) {
        locksService.locks(updateCache: true) { [weak self] (_, isServer, _) in
            if isServer == false && local == false { return }
            DispatchQueue.main.async {
                let lockManager = SLLockManager.sharedManager
                self?.connectedLock = lockManager.getCurrentLock()
                self?.unconnectedLocks = lockManager.allPreviouslyConnectedLocksForCurrentUser()
                self?.tableView.reloadData()
            }
        }
    }
    
    func menuButtonPressed() {
        if let root = parent?.presentingViewController {
            root.dismiss(animated: true, completion: nil)
        } else if let navController = self.navigationController {
            navController.dismiss(animated: true, completion: nil)
        } else {
            self.dismiss(animated: true, completion: nil)
        }
    }
    
    @objc private func addLock(_ sender: UIBarButtonItem) {
        let slwb = WelcomViewController(title: sender.title)
        self.navigationController?.pushViewController(slwb, animated: true)
    }
    
    func connectToPreviouslyConnected(lock: SLLock) {
        guard lockManager.isBlePoweredOn() else {
            presentWarningViewControllerWithTexts(texts: [.Header: "Bluetooth Turned OFF".localized(),
                                                          .Info: "Please turn ON Bluetooth in order to pair your Ellipse.".localized(),
                                                          .CancelButton: "OK".localized()], cancelClosure: nil)
            return
        }
        self.previousLockToConnect = lock
        self.navigationItem.rightBarButtonItem?.isEnabled = false
        self.navigationItem.leftBarButtonItem?.isEnabled = false
        
        self.lockManager.endActiveSearch()
        
        if self.connectedLock == nil {
            self.lockManager.startActiveSearch()
        } else {
            self.lockManager.disconnectFromCurrentLock(completion: {
                self.lockManager.startActiveSearch()
            })
        }
        
        let message = NSLocalizedString("Connecting to ", comment: "") + lock.displayName
        self.presentLoadingViewWithMessage(message: message)
        
        self.previousLockTimer = Timer.scheduledTimer(
            timeInterval: self.previousConnectionTimeout,
            target: self,
            selector: #selector(previousConnectedLockNotFound(timer:)),
            userInfo: nil,
            repeats: false
        )
    }
    
    func rowActionTextForIndexPath(indexPath: IndexPath) -> String {
        return "             "
    }
    
    func lockConnected(notification: Notification) {
        self.unconnectedLocks = self.lockManager.allPreviouslyConnectedLocksForCurrentUser()
        self.connectedLock = self.lockManager.getCurrentLock()
        
        self.tableView.reloadData()
    }
    
    func lockDisconnected(notification: Notification) {
        guard let disconnectedAddress = notification.object as? String else {
            return
        }
        
        self.unconnectedLocks = self.lockManager.allPreviouslyConnectedLocksForCurrentUser()
        if disconnectedAddress == self.connectedLock?.macId {
            self.connectedLock = nil
        }
        
        self.tableView.reloadData()
    }
    
    func foundLock(notification: Notification) {
        guard let lock = notification.object as? SLLock else {
            print("Error: found lock but it was not included in notification")
            return
        }
        
        guard let previousLock = self.previousLockToConnect else {
            return
        }
        
        if lock.macId != previousLock.macId {
            return
        }
        
        self.lockManager.connectToLockWithMacAddress(macAddress: previousLock.macId!)
    }
    
    func connectedLock(notificaiton: Notification) {
        self.previousLockTimer?.invalidate()
        guard let previousLock = self.previousLockToConnect else {
            return
        }
        
        self.lockManager.endActiveSearch()
        self.dismissLoadingViewWithCompletion(completion: { [weak self] in
            self?.connectedLock = previousLock
            if let previousLocks = self?.lockManager.allPreviouslyConnectedLocksForCurrentUser() {
                self?.unconnectedLocks = previousLocks
            }
            
            self?.tableView.reloadData()
            self?.previousLockToConnect = nil
            self?.previousLockTimer = nil
            self?.navigationItem.rightBarButtonItem?.isEnabled = true
            self?.navigationItem.leftBarButtonItem?.isEnabled = true
        })
    }
    
    func lockConnectionError(notification: Notification) {
        if self.previousLockToConnect == nil {
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
        
        self.previousLockTimer?.invalidate()
        self.previousLockTimer = nil
        self.previousLockToConnect = nil
        self.lockManager.endActiveSearch()
        
        self.dismissLoadingViewWithCompletion {
            let texts:[SLWarningViewControllerTextProperty:String?] = [
                .Header: NSLocalizedString("FAILED TO CONNECT", comment: "") ,
                .Info: info,
                .CancelButton: NSLocalizedString("OK", comment: ""),
                .ActionButton: nil
            ]
            
            self.presentWarningViewControllerWithTexts(texts: texts, cancelClosure: { [weak self] in
                if let navController = self?.navigationController {
                    navController.popViewController(animated: true)
                } else {
                    self?.dismiss(animated: true, completion: nil)
                }
                
                self?.navigationItem.rightBarButtonItem?.isEnabled = true
                self?.navigationItem.leftBarButtonItem?.isEnabled = true
            })
        }
    }
    
    func previousConnectedLockNotFound(timer: Timer) {
        self.lockManager.endActiveSearch()
        timer.invalidate()
        
        guard let previousLock = self.previousLockToConnect else {
            return
        }
        
        let info = NSLocalizedString("Sorry, We couldn't connect to ", comment: "")
            + previousLock.displayName + NSLocalizedString(" at this time", comment: "")
        self.dismissLoadingViewWithCompletion(completion: { [weak self] in
            let texts:[SLWarningViewControllerTextProperty:String?] = [
                .Header: NSLocalizedString("Couldn't Connect", comment: ""),
                .Info: info,
                .CancelButton: NSLocalizedString("OK", comment: ""),
                .ActionButton: nil
            ]
            
            self?.presentWarningViewControllerWithTexts(texts: texts, cancelClosure: nil)
            self?.previousLockTimer = nil
            self?.previousLockToConnect = nil
            self?.navigationItem.rightBarButtonItem?.isEnabled = true
            self?.navigationItem.leftBarButtonItem?.isEnabled = true
        })
    }
    
    private func unshare(lock: SLLock) {
        unshareLock = lock
        presentWarningViewControllerWithTexts(texts: [.Header : "CONFIRM ACTION".localized(),
                                                      .Info: "If you remove this Ellipse from your account, you will not be able to re-connect to it unless the owner grants you permission again.".localized(),
                                                      .ActionButton: "UNSHARE".localized(),
                                                      .CancelButton: "CANCEL".localized()], cancelClosure: nil)
    }
    
    // MARK: UITableView Delegate & Datasource methods
    func numberOfSections(in tableView: UITableView) -> Int {
        return 2
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return section == 0 ? 1 : self.unconnectedLocks.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cellId = String(describing: SLLockDetailsTableViewCell.self)
        let optionsDotsImage:UIImage = UIImage(named: "icon_more_dots_gray_horizontal_Ellipses")!
        let optionsDotsView:UIImageView = UIImageView(image: optionsDotsImage)
        var mainText:String?
        var detailText:String?
        var isConnected = false
        var cellEnabled = true
        
        if let lock = self.connectedLock , indexPath.section == 0 {
            mainText = lock.displayName
            detailText = NSLocalizedString("Connected", comment: "")
            isConnected = true
        } else if indexPath.section == 0 {
            cellEnabled = false
        } else {
            let unconnectedLock = self.unconnectedLocks[indexPath.row]
            mainText = unconnectedLock.displayName
            if let lastConnectedDate = unconnectedLock.lastConnected {
                detailText = NSLocalizedString("Last connected on", comment: "") + " "
                    + self.dateFormatter.string(from: lastConnectedDate as Date)
            }
        }
        
        let cell:SLLockDetailsTableViewCell = tableView.dequeueReusableCell(
            withIdentifier: cellId
            ) as! SLLockDetailsTableViewCell
        
        cell.setProperties(isConnected: isConnected, mainText: mainText, detailText: detailText)
        cell.accessoryView = mainText == nil ? nil : optionsDotsView
        cell.selectionStyle = .none
        cell.isUserInteractionEnabled = cellEnabled
        
        return cell
    }


    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        return 0.001
    }
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 65.0
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let text:String
        let backgroundColor:UIColor
        let textColor:UIColor
        
        if section == 0 {
            text = NSLocalizedString("CURRENTLY CONNECTED", comment: "")
            backgroundColor = self.utilities.color(colorCode: .Color60_83_119)
            textColor = self.utilities.color(colorCode: .Color239_239_239)
        } else {
            text = NSLocalizedString("PREVIOUS CONNECTIONS", comment: "")
            backgroundColor = self.utilities.color(colorCode: .Color247_247_248)
            textColor = self.utilities.color(colorCode: .Color140_140_140)
        }
        
        let frame = CGRect(
            x: 0,
            y: 0,
            width: tableView.bounds.size.width,
            height: self.tableView(tableView, heightForHeaderInSection: section)
        )
        
        let view:UIView = UIView(frame: frame)
        view.backgroundColor = backgroundColor
        
        let height:CGFloat = 14.0
        let labelFrame = CGRect(
            x: 0,
            y: 0.5*(view.bounds.height - height),
            width: view.bounds.width,
            height: height
        )
        
        let label:UILabel = UILabel(frame: labelFrame)
        label.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 14.0)
        label.textColor = textColor
        label.text = text
        label.textAlignment = .center
        label.backgroundColor = UIColor.clear
        
        view.addSubview(label)
        
        return view
    }
    
    func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        return true
    }
    
    func tableView(_ tableView: UITableView, editActionsForRowAt indexPath: IndexPath) -> [UITableViewRowAction]? {
        var actions: [UITableViewRowAction] = []
        var lock:SLLock?
        if indexPath.section == 0 && self.connectedLock != nil {
            lock = self.connectedLock
        } else if indexPath.section == 1 {
            lock = self.unconnectedLocks[indexPath.row]
        }
        var deleteImage = UIImage(named: "locks_delete_lock_button")!
        if let lock = lock, lock.isShared {
            deleteImage = UIImage(named: "locks_unshare_lock_button")!
        }
        let deleteAction = UITableViewRowAction(
            style: .normal,
            title: self.rowActionTextForIndexPath(indexPath: indexPath))
        { (rowAction, index) in
            guard let lock = lock else { return }
            if lock.isShared {
                self.unshare(lock: lock)
            } else {
                let lrodvc = SLLockResetOrDeleteViewController(
                    type: .Delete,
                    lock: lock
                )
                self.navigationController?.pushViewController(lrodvc, animated: true)
            }
        }
        deleteAction.backgroundColor = UIColor(patternImage: deleteImage)
        
        if indexPath.section == 0 {
            let settingsImage = UIImage(named: "locks_setting_button")!
            let settingsAction = UITableViewRowAction(
                style: .normal,
                title: self.rowActionTextForIndexPath(indexPath: indexPath))
            { (rowAction, index) in
                if let lock = self.connectedLock {
                    let lsvc = SLLockSettingsViewController(lock: lock)
                    lsvc.delegate = self
                    self.navigationController?.pushViewController(lsvc, animated: true)
                }
            }
            settingsAction.backgroundColor = UIColor(patternImage: settingsImage)
            actions.append(settingsAction)
        } else {
            let connectImage = UIImage(named: "locks_connect_button")!
            let connectAction = UITableViewRowAction(
                style: .normal,
                title: self.rowActionTextForIndexPath(indexPath: indexPath))
            { (rowAction, index) in
                self.connectToPreviouslyConnected(lock: self.unconnectedLocks[indexPath.row])
            }
            connectAction.backgroundColor = UIColor(patternImage: connectImage)
            actions.append(connectAction)
        }
        
        actions.append(deleteAction)
        
        return actions
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if let lock = self.connectedLock , indexPath.section == 0 && indexPath.row == 1 {
            let lsvc = SLLockSettingsViewController(lock: lock)
            self.navigationController?.pushViewController(lsvc, animated: true)
        }
    }
    
    // MARK - SLLockSettingsViewControllerDelegate
    func lockSettingVCValueChanged(fieldValue: SLLockSettingsViewControllerValue) {
        switch fieldValue {
        case .LockName:
            self.unconnectedLocks = self.lockManager.allPreviouslyConnectedLocksForCurrentUser()
            self.connectedLock = self.lockManager.getCurrentLock()
            self.tableView.reloadData()
        }
    }
    
    override func warningVCTakeActionButtonPressed(wvc: SLWarningViewController) {
        guard let lock = unshareLock, let userId = lock.user?.userId else { return }
        closeWarning {
            self.presentLoadingViewWithMessage(message: "Unsharing lock...".localized())
            SLLockManager.sharedManager.revoke(lock: lock, unshareFrom: userId) { [weak self] (success) in
                self?.dismissLoadingViewWithCompletion(completion: nil)
                self?.refreshLocks(local: true)
                
                Answers.logShare(withMethod: "Unshare lock (by borrower)", contentName: nil, contentType: nil, contentId: nil, customAttributes: nil)
            }
        }
    }
}
