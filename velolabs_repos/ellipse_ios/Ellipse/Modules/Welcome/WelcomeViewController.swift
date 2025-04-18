//
//  WelcomeViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/9/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography

class WelcomeViewController: ViewController {
    var interactor: LogInInteractorInput!
    
    fileprivate let logo = UIImageView(image: UIImage(named: "splash_logo_dark"))
    fileprivate let signUpButton: UIButton = UIButton(type: .custom)
    fileprivate let loginButton = UIButton(type: .custom)
    fileprivate let facebookButton = UIButton(type: .custom)
    
    override func viewDidLoad() {
        
        super.viewDidLoad()
        view.backgroundColor = .white
        view.addSubview(logo)
        constrain(logo) { view in
            view.centerX == view.superview!.centerX
            view.centerY == view.superview!.centerY - 70
        }
        configureButtons()
    }
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .default
    }
    
    fileprivate func configureButtons() {
        
        view.addSubview(facebookButton)
        facebookStyle(facebookButton)
        
        view.addSubview(loginButton)
        bigRoundCorners(loginButton)
        loginButton.backgroundColor = .elWindowsBlue
        loginButton.setTitle("action_login_in_short".localized().lowercased().capitalized, for: .normal)
        
        view.addSubview(signUpButton)
        bigRoundCorners(signUpButton)
        signUpButton.backgroundColor = .elDarkSkyBlue
        signUpButton.setTitle("action_sign_in_short".localized().lowercased().capitalized, for: .normal)
        
        constrain(facebookButton, loginButton, signUpButton, view) { facebook, login, signUp, view in
            facebook.left == view.left + .margin ~ .defaultLow
            facebook.right == view.right - .margin ~ .defaultLow
            facebook.bottom == view.safeAreaLayoutGuide.bottom - .margin
            
            login.left == facebook.left ~ .defaultLow
            login.right == facebook.right ~ .defaultLow
            login.bottom == facebook.top - .margin
            
            signUp.left == login.left ~ .defaultLow
            signUp.right == login.right ~ .defaultLow
            signUp.bottom == login.top - .margin
            signUp.height == login.height
            
            facebook.width == login.width
            login.width == signUp.width
            facebook.centerX == view.centerX
            login.centerX == view.centerX
            signUp.centerX == view.centerX
        }
        
        loginButton.addTarget(self, action: #selector(logIn(_:)), for: .touchUpInside)
        signUpButton.addTarget(self, action: #selector(signUp(_:)), for: .touchUpInside)
        facebookButton.addTarget(self, action: #selector(facebookLogIn(_:)), for: .touchUpInside)
    }
    
    @objc func signUp(_ sender: Any) {
        present(LogInRouter.navigation(for: .signUp), animated: true, completion: nil)
    }
    
    @objc func logIn(_ sender: Any) {
        present(LogInRouter.navigation(for: .logIn), animated: true, completion: nil)
    }
    
    @objc func facebookLogIn(_ sender: Any) {
        interactor.facebookLogin()
    }
}

extension WelcomeViewController: LogInInteractorOutput {
    
}
