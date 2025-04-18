//
//  SLAvailableLocksViewController.swift
//  Skylock
//
//  Created by Andre Green on 5/29/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

@objc class SLAvailableLocksViewController: SLBaseViewController,
UITableViewDelegate, UITableViewDataSource {
    var locks:[SLLock] = []
    
    let buttonTagShift: Int = 1000
    
    var tempButtons: [UIButton] = []
    
    var hideBackButton = false
    
    var dismissConcentricCirclesViewController: Bool?
    
    weak var ellipleBarController: BottomBarPresenting? {
        didSet {
            ellipleBarController?.canShowBottomBar = false
            ellipleBarController?.hideBottomBar()
        }
    }
        
    lazy var tableView: UITableView = {
        let table:UITableView = UITableView(frame: self.view.bounds, style: .plain)
        table.rowHeight = 110.0
        table.backgroundColor = UIColor.clear
        table.delegate = self
        table.dataSource = self
        
        return table
    }()
    
    lazy var headerLabel: UILabel = {
        let frame = CGRect(
            x: 0,
            y: 0,
            width: self.tableView.bounds.size.width,
            height: self.tableView(self.tableView, heightForHeaderInSection: 0)
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.textAlignment = .center
        label.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 18.0)
        label.textColor = UIColor(red: 130, green: 156, blue: 178)
        label.numberOfLines = 2

        return label
    }()
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.navigationItem.title = NSLocalizedString("SELECT AN ELLIPSE", comment: "")
        
        self.view.backgroundColor = UIColor.white
        
        self.view.addSubview(self.tableView)
        
        let backButton = UIBarButtonItem(
            image: UIImage(named: "lock_screen_close_icon"),
            style: UIBarButtonItemStyle.plain,
            target: self,
            action: #selector(backButtonPressed)
        )

        self.navigationItem.leftBarButtonItem = backButton
        
        self.setHeaderText(numberOfLocks: self.locks.count)
        
        NotificationCenter.default.addObserver(self, selector: #selector(foundLock(notification:)), name: NSNotification.Name(rawValue: kSLNotificationLockManagerDiscoverdLock), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(lockShallowlyConntected(notificaiton:)), name: NSNotification.Name(rawValue: kSLNotificationLockManagerShallowlyConnectedLock), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(bleHardwarePoweredOn(notificaiton:)), name: NSNotification.Name(rawValue: kSLNotificationLockManagerBlePoweredOn), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(ledTurnedOff(notification:)), name: NSNotification.Name(rawValue: kSLNotificationLockLedTurnedOff), object: nil)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        let lockManager = SLLockManager.sharedManager
        if lockManager.isBlePoweredOn() && !lockManager.isInActiveSearch() {
            lockManager.startActiveSearch()
        }
        
        self.locks = SLLockManager.sharedManager.locksInActiveSearch()
        self.tableView.reloadData()
    }
    
    func addLock(lock: SLLock) {
        var addLock = true
        for listedLock in self.locks {
            if lock.macId == listedLock.macId {
                addLock = false
                break
            }
        }
        
        if addLock {
            self.locks.append(lock)
        }
    }
    
    func foundLock(notification: Notification) {
        guard let lock = notification.object as? SLLock else {
            print("Error: found lock but it was not included in notification")
            return
        }
        
        var addLock = true
        for knownLock in self.locks where knownLock.macId == lock.macId {
            addLock = false
            break
        }
        
        if addLock {
            self.locks.append(lock)
            // If this is the first lock found, we need to update the header text.
            self.setHeaderText(numberOfLocks: self.locks.count)
            
            let indexPath:IndexPath = IndexPath(row: self.locks.count - 1, section: 0)
            self.tableView.beginUpdates()
            self.tableView.insertRows(at: [indexPath as IndexPath], with: .left)
            self.tableView.endUpdates()
        }
    }
    
    func lockShallowlyConntected(notificaiton: Notification) {
        guard let connectedLock = notificaiton.object as? SLLock else {
            return
        }
        
        for (index, lock) in self.locks.enumerated() {
            if lock.macId == connectedLock.macId {
                self.enableButtonAtIndex(index: index)
                break
            }
        }
    }
    
    func bleHardwarePoweredOn(notificaiton: Notification) {
        let lockManager = SLLockManager.sharedManager
        lockManager.startActiveSearch()
    }
    
    func blinkLockButtonPressed(button: UIButton) {
        for (i, lock) in self.locks.enumerated() {
            let indexPath:IndexPath = IndexPath(row: i, section: 0)
            guard let cell:UITableViewCell = self.tableView.cellForRow(at: indexPath) else   {
                continue
            }
            
            if let accessoryButton:UIButton = cell.accessoryView as? UIButton {
                let accessoryButtonTag = accessoryButton.tag
                let buttonTag = button.tag
                if accessoryButtonTag == buttonTag {
                    if let lockMacAddress = lock.macId {
                        presentLoadingViewWithMessage(message: "")
                        SLLockManager.sharedManager.shallowlyConnectToLock(macAddress: lockMacAddress)
                        break
                    }
                }
            }
        }
    }
    
    func connectButtonPressed(button: UIButton) {
        for (i, lock) in self.locks.enumerated() {
            let indexPath:IndexPath = IndexPath(row: i, section: 0)
            guard let cell:UITableViewCell = self.tableView.cellForRow(at: indexPath) else {
                continue
            }
            
            if let accessoryButton:UIButton = cell.accessoryView as? UIButton {
                let accessoryButtonTag = accessoryButton.tag
                let buttonTag = button.tag
                if accessoryButtonTag == buttonTag {
                    let action:SLConcentricCirclesViewControllerAction
                    if let dismiss = self.dismissConcentricCirclesViewController {
                        action = dismiss ? .dismiss : .showSuccessVC
                    } else {
                        if lock.hasConnected {
                            action = .dismiss
                        } else {
                            action = lock.onboard ? .dismiss : .showSuccessVC
                        }
                    }
                    
                    let ccvc = SLConcentricCirclesViewController(action: action)
                    self.navigationController?.pushViewController(ccvc, animated: true)
                    let lockManager = SLLockManager.sharedManager
                    DispatchQueue.main.asyncAfter(deadline: .now() + 1, execute: {
                        lockManager.connectToLockWithMacAddress(macAddress: lock.macId!)
                    })
                    break
                }
            }
        }
    }
    
    func backButtonPressed() {
        guard let navController = self.navigationController else {
            return
        }
        
        if navController.viewControllers.first == self {
            navController.dismiss(animated: true, completion: nil)
        } else {
            navController.popViewController(animated: true)
        }
        
        let lockManager = SLLockManager.sharedManager
        lockManager.endActiveSearch()
        lockManager.deleteAllNeverConnectedAndNotConnectingLocks()
    }
    
    func enableButtonAtIndex(index: Int) {
        if index < tempButtons.count {
            let button:UIButton = self.tempButtons[index]
            button.isEnabled = true
        }
    }
    
    func skipDatShit() {
        if self.hideBackButton || self.navigationController?.viewControllers.first! == self {
            let lvc = SLLockViewController()
            self.present(lvc, animated: true, completion: nil)
        }
    }
    
    func setHeaderText(numberOfLocks: Int) {
        self.headerLabel.text = numberOfLocks == 0 ? NSLocalizedString("Searching...", comment: "")
            : NSLocalizedString("We've found the following Ellipses", comment: "")
    }
    
    // MARK: UITableView delegate and datasource methods
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.locks.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cellId = "SLAvaliableLocksViewControllerCell"
        var cell: UITableViewCell? = tableView.dequeueReusableCell(withIdentifier: cellId)
        if cell == nil {
            cell = UITableViewCell(style: .subtitle, reuseIdentifier: cellId)
        }
        
        let lock = self.locks[indexPath.row]
        let image:UIImage = UIImage(named: "lock_onboarding_connect_button")!
        let button:UIButton = UIButton(frame: CGRect(
            x: 0,
            y: 0,
            width: image.size.width,
            height: image.size.height)
        )
        button.setImage(image, for: .normal)
        button.addTarget(self, action: #selector(connectButtonPressed(button:)), for: .touchDown)
        button.tag = indexPath.row + self.buttonTagShift
        
        self.tempButtons.append(button)
        let blinkLEDButton:UIButton = UIButton(frame: CGRect(
            x: -50,
            y: 70,
            width: 273,
            height: 29
            )
        )
        
        blinkLEDButton.setTitle(NSLocalizedString("Blink This Ellipse", comment:""), for: .normal)
        blinkLEDButton.setTitleColor(UIColor.color(87, green: 216, blue: 255), for: .normal)
        blinkLEDButton.addTarget(self, action: #selector(blinkLockButtonPressed(button:)), for: .touchDown)
        blinkLEDButton.tag = indexPath.row + self.buttonTagShift
        
        cell?.contentView.addSubview(blinkLEDButton)
        
        cell?.textLabel?.text = lock.displayName
        cell?.textLabel?.textColor = UIColor(white: 155.0/255.0, alpha: 1.0)
        cell?.textLabel?.font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 17.0)
        cell?.accessoryView = button
        cell?.selectionStyle = .none
        
        return cell!
    }
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 90.0
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let viewFrame = CGRect(
            x: 0,
            y: 0,
            width: tableView.bounds.size.width,
            height: self.tableView(tableView, heightForHeaderInSection: section)
        )
        
        let view:UIView = UIView(frame: viewFrame)
        view.backgroundColor = UIColor.white
        
        view.addSubview(self.headerLabel)
        
        let inset:UIEdgeInsets = tableView.separatorInset
        let lineViewFrame = CGRect(
            x: inset.left,
            y: view.bounds.size.height - 1.0,
            width: view.bounds.size.width - inset.left - inset.right,
            height: 1.0
        )
        
        let lineView = UIView(frame: lineViewFrame)
        lineView.backgroundColor = tableView.separatorColor
        
        view.addSubview(lineView)
        
        let tgr:UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(skipDatShit))
        tgr.numberOfTapsRequired = 5
        view.addGestureRecognizer(tgr)
        view.isUserInteractionEnabled = true
        
        return view
    }
    
    func ledTurnedOff(notification : Notification) {
        dismissLoadingViewWithCompletion(completion: nil)
    }
}

extension SLAvailableLocksViewController: BottomBarPresentable {}
