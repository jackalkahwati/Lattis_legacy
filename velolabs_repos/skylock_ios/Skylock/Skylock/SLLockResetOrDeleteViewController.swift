//
//  SLLockResetOrDeleteViewController.swift
//  Skylock
//
//  Created by Andre Green on 6/10/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit
import Crashlytics
import Localize_Swift

enum SLLockResetOrDeleteViewControllerType {
    case Reset
    case Delete
}

class SLLockResetOrDeleteViewController: SLBaseViewController {
    var type:SLLockResetOrDeleteViewControllerType
    
    let lock:SLLock
    
    let xPadding:CGFloat = 21.0
    
    lazy var infoLabel:UILabel = {
        let labelWidth = self.affirmativeButton.bounds.size.width
        let utility = SLUtilities()
        let font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 14.0)!
        let text:String
        if self.type == .Reset {
            text = NSLocalizedString(
                "Doing a factory reset erases all settings including Ellipse name, " +
                "pin code, sharing information and restores your Ellipse back to its factory " +
                "default settings. Your Ellipse must unlocked to perform this action.",
                comment: ""
            )
        } else {
            text = NSLocalizedString(
                "You are permanently deleting this Ellipse from your account." +
                "If you are not connected to this Ellipse it will not be factory reset." +
                "You will need to reset it before you can transfer it. " +
                "Reset code can be find in the help section and can only be enterd in the unlock position.",
                comment: ""
            )
        }
        
        let labelSize:CGSize = utility.sizeForLabel(
            font: font,
            text: text,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        
        let frame = CGRect(
            x: 0.5*(self.view.bounds.size.width - labelSize.width),
            y: (self.navigationController?.navigationBar.bounds.size.height)!
                + UIApplication.shared.statusBarFrame.size.height + 33.0,
            width: labelSize.width,
            height: labelSize.height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = UIColor(white: 155.0/255.0, alpha: 1.0)
        label.text = text
        label.font = font
        label.numberOfLines = 0
        
        return label
    }()
    
    lazy var affirmativeButton:UIButton = {
        let width = (self.view.bounds.size.width - 2.0*self.xPadding)
        let frame = CGRect(
            x: self.xPadding,
            y: self.view.bounds.midY,
            width: width,
            height: 44.0
        )
        
        let button:UIButton = UIButton(frame: frame)
        button.addTarget(self, action: #selector(affirmativeButtonPressed), for: .touchDown)
        button.setTitle(NSLocalizedString("DELETE ELLIPSE", comment: ""), for: .normal)
        button.setTitleColor(UIColor.white, for: .normal)
        button.backgroundColor = UIColor(red: 87, green: 216, blue: 255)
        button.titleLabel?.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 12.0)
        
        return button
    }()
    
    init(
        nibName nibNameOrNil: String?,
                bundle nibBundleOrNil: Bundle?,
                       type: SLLockResetOrDeleteViewControllerType,
                       lock: SLLock
        )
    {
        self.lock = lock
        self.type = type
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }
    
    convenience init(type: SLLockResetOrDeleteViewControllerType, lock: SLLock) {
        self.init(nibName: nil, bundle: nil, type: type, lock: lock)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = UIColor.white
        
        addBackButton()
        title = "DELETE THIS ELLIPSE".localized()

        self.view.addSubview(self.affirmativeButton)
        self.view.addSubview(self.infoLabel)
        
        NotificationCenter.default.addObserver(self, selector: #selector(handleLockRemoved(notifciation:)), name: Notification.Name(rawValue: kSLNotificationLockManagerDeletedLock), object: nil)
    }

    
    func affirmativeButtonPressed() {
        let lockManager = SLLockManager.sharedManager
        switch self.type {
        case .Delete:
            if let macId = lock.macId {
                self.navigationItem.hidesBackButton = true
                let message = "Deleting".localized() + " " + self.lock.displayName + "..."
                lockManager.deleteLockFromCurrentUserAccountWithMacAddress(macAddress: macId)
                self.presentLoadingViewWithMessage(message: message)
            } else {
                presentWarningViewControllerWithTexts(texts: [.Header: "Deleting error".localized(),
                                                              .Info: "Sorry. Could not delete this lock. Try again later.".localized(),
                                                              .CancelButton: "OK".localized()], cancelClosure: {
                                                                 _ = self.navigationController?.popViewController(animated: true)
                })
            }
        case .Reset:
            lockManager.factoryResetCurrentLock()
        }
    }
    
    func handleLockRemoved(notifciation: Notification) {
        self.dismissLoadingViewWithCompletion(completion: {
            _ = self.navigationController?.popToRootViewController(animated: true)
        })
        
        Answers.logCustomEvent(withName: "Delete lock", customAttributes: nil)
    }
}
