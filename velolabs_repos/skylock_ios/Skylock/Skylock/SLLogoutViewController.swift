//
//  SLLogoutViewController.swift
//  Skylock
//
//  Created by Andre Green on 8/3/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import Crashlytics
import KeychainSwift
import FBSDKLoginKit
import RestService

class SLLogoutViewController: UIViewController {
    let buttonHeight:CGFloat = 55.0
    let userId: Int32
    lazy var closeButton:UIButton = {
        let padding:CGFloat = 20.0
        let image:UIImage = UIImage(named: "close_x_white_icon")!
        let frame = CGRect(
            x: self.view.bounds.size.width - image.size.width - padding,
            y: UIApplication.shared.statusBarFrame.size.height + padding,
            width: image.size.width,
            height: image.size.height
        )
        
        let button:UIButton = UIButton(frame: frame)
        button.addTarget(self, action: #selector(exit), for: .touchDown)
        button.setImage(image, for: .normal)
        
        return button
    }()
    
    lazy var cancelButton:UIButton = {
        let frame = CGRect(
            x: 0.0,
            y: self.view.bounds.size.height - self.buttonHeight,
            width: 0.5*self.view.bounds.size.width,
            height: self.buttonHeight
        )
        
        let button:UIButton = UIButton(type: .system)
        button.frame = frame
        button.setTitle(NSLocalizedString("CANCEL", comment: ""), for: .normal)
        button.setTitleColor(UIColor(red: 188, green: 188, blue: 187), for: .normal)
        button.backgroundColor = UIColor(red: 231, green: 231, blue: 233)
        button.addTarget(self, action: #selector(exit), for: .touchDown)
        button.titleLabel?.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 12)
        
        return button
    }()
    
    lazy var logoutButton:UIButton = {
        let frame = CGRect(
            x: 0.5*self.view.bounds.size.width,
            y: self.view.bounds.size.height - self.buttonHeight,
            width: 0.5*self.view.bounds.size.width,
            height: self.buttonHeight
        )
        
        let button:UIButton = UIButton(type: .system)
        button.frame = frame
        button.setTitle(NSLocalizedString("LOG OUT", comment: ""), for: .normal)
        button.setTitleColor(UIColor(red: 255, green: 255, blue: 255), for: .normal)
        button.backgroundColor = UIColor(red: 87, green: 216, blue: 255)
        button.addTarget(self, action: #selector(logoutButtonPressed), for: .touchDown)
        button.titleLabel?.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 12.0)
        
        return button
    }()
    
    lazy var logoutLabel:UILabel = {
        let width:CGFloat = 200.0
        let text: String = NSLocalizedString("Are you sure you\nwant to log out?", comment: "")
        let font:UIFont = UIFont(name: SLFont.MontserratRegular.rawValue, size: 22.0)!
        let utility: SLUtilities = SLUtilities()
        let size: CGSize = utility.sizeForLabel(
            font: font,
            text:text,
            maxWidth: width,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        
        let frame = CGRect(
            x: 0.5*(self.view.bounds.size.width - size.width),
            y: self.cancelButton.frame.minY - size.height - 110.0,
            width: size.width,
            height: size.height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.text = text
        label.font = font
        label.textColor = UIColor.white
        label.textAlignment = .center
        label.numberOfLines = 0
        
        return label
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = UIColor(red: 160, green: 200, blue: 224)
        
        self.view.addSubview(self.closeButton)
        self.view.addSubview(self.cancelButton)
        self.view.addSubview(self.logoutButton)
        self.view.addSubview(self.logoutLabel)
    }
    
    func exit() {
        self.dismiss(animated: true, completion: nil)
    }
    
    func logoutButtonPressed() {
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
            
            let parent = presentingViewController
            dismiss(animated: true, completion: { 
                parent?.dismiss(animated: false, completion: { 
                    let svc = SLSignInViewController()
                    let appDelegate = UIApplication.shared.delegate as! SLAppDelegate
                    let navigation = appDelegate.window.rootViewController as? UINavigationController
                    navigation?.setViewControllers([svc], animated: true)
                })
            })
            
            Answers.logCustomEvent(withName: "LogOut", customAttributes: nil)
        }
    }
    
    init(userId: Int32) {
        self.userId = userId
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
