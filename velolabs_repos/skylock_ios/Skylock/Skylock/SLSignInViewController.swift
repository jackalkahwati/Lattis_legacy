//
//  SLSignInViewController.swift
//  Skylock
//
//  Created by Andre Green on 5/28/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit
import Localize_Swift
import SwiftyTimer
import Crashlytics
import RestService

class SLSignInViewController: SLBaseViewController {
    let buttonSpacer:CGFloat = 20
    
    lazy var logoView:UIImageView = {
        let image = UIImage(named: "ellipse_logo")!
        let view = UIImageView(image: image)
        view.frame = CGRect(
            x: 0.5*(self.view.bounds.size.width - image.size.width),
            y: self.view.bounds.midY - image.size.height,
            width: image.size.width,
            height: image.size.height
        )
        
        return view
    }()
    
    lazy var signUpWithFacebookButton:UIButton = {
        let image = UIImage(named: "button_sign_up_facebook_Onboarding")!
        let frame = CGRect(
            x: 0.5*(self.view.bounds.size.width - image.size.width),
            y: self.view.bounds.size.height - image.size.height - self.buttonSpacer,
            width: image.size.width,
            height: image.size.height
        )
        
        let button:UIButton = UIButton(frame: frame)
        button.setImage(image, for: UIControlState.normal)
        button.addTarget(
            self,
            action: #selector(signUpWithFacebookButtonPressed),
            for: UIControlEvents.touchDown
        )
        
        return button
    }()

    lazy var existingUserButton:UIButton = {
        let frame = CGRect(
            x: self.signUpWithFacebookButton.frame.minX,
            y: self.signUpWithFacebookButton.frame.minY
                - self.signUpWithFacebookButton.bounds.size.height - self.buttonSpacer,
            width: self.signUpWithFacebookButton.bounds.size.width,
            height: self.signUpWithFacebookButton.bounds.size.height
        )
        
        let button:UIButton = UIButton(type: UIButtonType.system)
        button.frame = frame
        button.backgroundColor = UIColor.clear
        button.setTitle(NSLocalizedString("LOG IN", comment: ""), for: .normal)
        button.setTitleColor(UIColor.color(87, green: 216, blue: 255), for: .normal)
        button.titleLabel?.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 14.0)
        button.layer.borderWidth = 1
        button.layer.borderColor = UIColor.color(87, green: 216, blue: 255).cgColor
        button.addTarget(
            self,
            action: #selector(existingUserButtonPressed),
            for: .touchDown
        )
        
        return button
    }()

    lazy var signUpWithEmailButton:UIButton = {
        let frame = CGRect(
            x: self.signUpWithFacebookButton.frame.minX,
            y: self.existingUserButton.frame.minY
                - self.signUpWithFacebookButton.bounds.size.height - self.buttonSpacer,
            width: self.signUpWithFacebookButton.bounds.size.width,
            height: self.signUpWithFacebookButton.bounds.size.height
        )
        
        let button:UIButton = UIButton(type: UIButtonType.system)
        button.frame = frame
        button.backgroundColor = UIColor.color(87, green: 216, blue: 255)
        button.setTitle(NSLocalizedString("SIGN UP", comment: ""), for: .normal)
        button.setTitleColor(UIColor.white, for: .normal)
        button.titleLabel?.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 14.0)
        button.addTarget(
            self,
            action: #selector(signUpWithEmailButtonPressed),
            for: .touchDown
        )
        
        return button
    }()
    
    class func navigation() -> UINavigationController {
        let clvc = SLSignInViewController()
        let nc = UINavigationController(rootViewController: clvc)
        nc.isNavigationBarHidden = true
        return nc
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = UIColor.white
        
        self.view.addSubview(self.logoView)
        self.view.addSubview(self.existingUserButton)
        self.view.addSubview(self.signUpWithEmailButton)
        self.view.addSubview(self.signUpWithFacebookButton)
    }
    
    func existingUserButtonPressed() {
        LogInRouter(self).present(logIn: .logIn).configure = { $0.router.delegate = self }
    }
    
    func signUpWithEmailButtonPressed() {
        LogInRouter(self).present(logIn: .signUp).configure = { $0.router.delegate = self }
    }
    
    func signUpWithFacebookButtonPressed() {
        func fail() {
            let texts:[SLWarningViewControllerTextProperty:String?] = [
                .Header: NSLocalizedString("Hmmm...Login Failed", comment: ""),
                .Info: NSLocalizedString(
                    "Sorry. We couldn't log you in through Facebook right now. " +
                    "Please try again later, or you can sign in using your phone number and email.",
                    comment: ""
                ),
                .CancelButton: NSLocalizedString("OK", comment: ""),
                .ActionButton: nil
            ]
            
            self.dismissLoadingViewWithCompletion { 
                self.presentWarningViewControllerWithTexts(texts: texts, cancelClosure: nil)
            }
            
            Answers.logSignUp(withMethod: "Facebook Failed", success: NSNumber(value: false), customAttributes: nil)
        }
        
        func login(router: LogInRouter) {
            let userDefaults = UserDefaults.standard
            userDefaults.set(true, forKey: SLUserDefaultsSignedIn)
            userDefaults.set(true, forKey: SLUserDefaultsEverSignedIn)
            userDefaults.synchronize()
            router.pushToLockController()
            Oval.users.user(success: { (user) in
                SLDatabaseManager.shared().save(ovalUser: user, setAsCurrent: true)
            }, fail: { error in
                print(error)
            })
            
            Answers.logSignUp(withMethod: "Facebook Succeded", success: NSNumber(value: true), customAttributes: nil)
        }
        
        FacebookService.shared.login(viewController: self, registration: { [weak self] in
            self?.presentLoadingViewWithMessage(message: "Logging in with Facebook".localized())
        }, completion: { [weak self] success in
            if let `self` = self, success {
                let router = LogInRouter(self)
                Oval.users.checkTerms(success: { [weak self] success in
                    guard let `self` = self else { return }
                    guard success == false else { return login(router: router) }
                    self.dismissLoadingViewWithCompletion(completion: nil)
                    router.presentTerms(from: self).configure = { interactor in
                        interactor.router.delegate = self
                        if let terms = interactor.view as? TermsAndConditionsViewController {
                            terms.isLogin = true
                        }
                    }
                    }, fail: { error in
                        fail()
                })
                
            } else {
                fail()
            }
        })
    }
}

extension SLSignInViewController: LogInRouterDelegate {
    func logInSucceded(hasLocks: Bool) {
        let lvc = SLLockViewController()
        self.navigationController?.setViewControllers([lvc], animated: true)
        if hasLocks == false {
            let nc = WelcomViewController.navigation()
            Timer.after(0.1.second, { 
                lvc.present(nc, animated: true, completion: nil)
            })
        }
    }
}



extension Oval.Users.Request {
    init?(facebook: [AnyHashable: Any]) {
        
        guard let id = facebook["id"] as? String,
            let countryCode = Locale.current.regionCode?.lowercased() else { return nil }
        self.init(usersId: id, firstName: facebook["first_name"] as? String, lastName: facebook["last_name"] as? String, regId: facebook["googlePushId"] as? String, userType: .facebook, password: facebook["id"] as? String, isSigningUp: true, countryCode: countryCode, email: facebook["email"] as? String)
    }
}
