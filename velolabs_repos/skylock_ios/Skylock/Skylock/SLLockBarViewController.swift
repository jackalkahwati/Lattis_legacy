//
//  SLLockBarViewController.swift
//  Skylock
//
//  Created by Andre Green on 6/27/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

private enum LockState {
    case Locked
    case Unlocked
    case Disconnected
}

private enum Element {
    case TapActionText
    case LockImageName
    case LockNameText
}

protocol SLLockBarViewControllerDelegate:class {
    func lockBarTapped(lockBar: SLLockBarViewController)
}

class SLLockBarViewController: UIViewController {
    weak var delegate:SLLockBarViewControllerDelegate?
    
    private var lock:SLLock?
    
    private let xPadding:CGFloat = 10.0
    
    private lazy var lockNameLabel:UILabel = {
        let width = 0.75*self.view.bounds.size.width
        let height:CGFloat = 20.0
        let font = UIFont.systemFont(ofSize: 13)
        let frame = CGRect(
            x: self.xPadding,
            y: 0.5*self.view.bounds.size.height - height,
            width: width,
            height: height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = UIColor.white
        label.text = self.lock?.displayName
        label.textAlignment = NSTextAlignment.left
        label.font = font
        label.numberOfLines = 1
        
        return label
    }()
    
    private lazy var lockImageView:UIImageView = {
        // TODO: get the unlocked image
        let image:UIImage = UIImage(named: "lock_bar_lock_icon")!
        let frame = CGRect(
            x: self.view.bounds.size.width - image.size.width - self.xPadding,
            y: 0.5*(self.view.bounds.size.height - image.size.height),
            width: image.size.width,
            height: image.size.height
        )
        let imageView:UIImageView = UIImageView(frame: frame)
        imageView.image = image
        
        return imageView
    }()
    
    private lazy var tapActionLabel:UILabel = {
        let width = self.lockNameLabel.bounds.size.width
        let height:CGFloat = 20.0
        let font = UIFont.systemFont(ofSize: 13)
        let frame = CGRect(
            x: self.xPadding,
            y: 0.5*self.view.bounds.size.height,
            width: width,
            height: height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = UIColor.white
        label.text = NSLocalizedString("Tap to unlock", comment: "")
        label.textAlignment = .left
        label.font = font
        label.numberOfLines = 1
        
        return label
    }()
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = UIColor(red: 60, green: 83, blue: 119)
        
        let tgr:UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(lockBarTapped))
        tgr.numberOfTapsRequired = 1
        self.view.addGestureRecognizer(tgr)
        
        NotificationCenter.default.addObserver(self, selector: #selector(lockOpened(notification:)), name: NSNotification.Name(rawValue: kSLNotificationLockPositionOpen), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(lockLocked(notification:)), name: NSNotification.Name(rawValue: kSLNotificationLockPositionLocked), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(lockDisconneted(notification:)), name: NSNotification.Name(rawValue: kSLNotificationLockManagerDisconnectedLock), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(lockPaired(notification:)), name: NSNotification.Name(rawValue: kSLNotificationLockPaired), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(lockDisconneted(notification:)), name: NSNotification.Name(rawValue: kSLNotificationRemoveLockForUser), object: nil)
    }
    
    func setUpViews() {
        self.lock = SLLockManager.sharedManager.getCurrentLock()
        
        if !self.view.subviews.contains(self.lockNameLabel) {
            self.view.addSubview(self.lockNameLabel)
        }
        
        if !self.view.subviews.contains(self.lockImageView) {
            self.view.addSubview(self.lockImageView)
        }
        
        if !self.view.subviews.contains(self.tapActionLabel) {
            self.view.addSubview(self.tapActionLabel)
        }
        
        guard let lock = lock else { return setUpForDisconnectedState() }
        if lock.isLocked {
            setUpForLockedState()
        } else {
            setUpForUnlockedState()
        }
    }
    
    private func currentElementState() -> LockState {
        return .Locked
    }
    
    private func valueForLockState(lockState: LockState, element: Element) -> String {
        let value:String
        switch lockState {
        case .Locked:
            switch element {
            case .TapActionText:
                value = NSLocalizedString("TAP TO UNLOCK", comment: "")
            case .LockImageName:
                value = "lock_bar_lock_icon"
            case .LockNameText:
                value = self.lock!.displayName
            }
        case .Unlocked:
            switch element {
            case .TapActionText:
                value = NSLocalizedString("TAP TO LOCK", comment: "")
            case .LockImageName:
                value = "lock_bar_lock_icon"
            case .LockNameText:
                value = self.lock!.displayName
            }
        case .Disconnected:
            switch element {
            case .TapActionText:
                value = ""
            case .LockImageName:
                value = ""
            case .LockNameText:
                value = NSLocalizedString("No Ellipse connected", comment: "")
            }
        }
        
        return value
    }
    
    private func setUpForDisconnectedState() {
        self.lockImageView.image = UIImage(named: self.valueForLockState(
            lockState: .Disconnected,
            element: .LockImageName
            )
        )
        self.lockNameLabel.text = self.valueForLockState(lockState: .Disconnected, element: .LockNameText)
        self.tapActionLabel.text = self.valueForLockState(lockState: .Disconnected, element: .TapActionText)
    }
    
    private func setUpForLockedState() {
        self.lockImageView.image = UIImage(named: self.valueForLockState(lockState: .Locked, element: .LockImageName))
        self.lockNameLabel.text = self.valueForLockState(lockState: .Locked, element: .LockNameText)
        self.tapActionLabel.text = self.valueForLockState(lockState: .Locked, element: .TapActionText)
    }
    
    private func setUpForUnlockedState() {
        self.lockImageView.image = UIImage(named: self.valueForLockState(lockState: .Unlocked, element: .LockImageName))
        self.lockNameLabel.text = self.valueForLockState(lockState: .Unlocked, element: .LockNameText)
        self.tapActionLabel.text = self.valueForLockState(lockState: .Unlocked, element: .TapActionText)
    }
    
    @objc private func lockBarTapped() {
        self.delegate?.lockBarTapped(lockBar: self)
    }
    
    @objc private func lockOpened(notification: Notification) {
        if self.lock == nil {
            self.setUpForDisconnectedState()
        } else {
            self.setUpForUnlockedState()
        }
    }
    
    @objc private func lockLocked(notification: Notification) {
        if self.lock == nil {
            self.setUpForDisconnectedState()
        } else {
            self.setUpForUnlockedState()
        }
    }
    
    @objc private func lockDisconneted(notification: Notification) {
        // TODO Set up view to handl when there is no lock
        guard let disconnectedAddress = notification.object as? String else {
            return
        }
        
        if let currentLock = self.lock, disconnectedAddress == currentLock.macId {
            self.lock = nil
            self.setUpForDisconnectedState()
        }
    }
    
    @objc private func lockPaired(notification: Notification) {
        let lockManager = SLLockManager.sharedManager
        if let lock:SLLock = lockManager.getCurrentLock() {
            self.lock = lock
            lockManager.checkCurrentLockOpenOrClosed()
        }
    }
}
