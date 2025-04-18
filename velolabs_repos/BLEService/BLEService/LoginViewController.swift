//
//  LoginViewController.swift
//  BLEService
//
//  Created by Ravil Khusainov on 14/02/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import RestService

class LoginViewController: UIViewController {
    @IBOutlet weak var emailField: UITextField!
    @IBOutlet weak var passField: UITextField!

    override func viewDidLoad() {
        super.viewDidLoad()

        
    }
    
    @IBAction func ravil(_ sender: Any) {
        emailField.text = "+79600448886"
        passField.text = "vefq2nfq"
    }
    
    @IBAction func logIn(_ sender: UIButton) {
        let ravil = Oval.Users.Request(usersId: emailField.text, regId: "", userType: .ellipse, phoneNumber: emailField.text, password: passField.text, isSigningUp: false)
        login(user: ravil)
    }
    
    private func login(user: Oval.Users.Request) {
        Oval.users.registration(user: user, success: { (userId, _) in
            Oval.users.getTokens(userId: userId, password: user.password!, success: {}, fail: {_ in})
            UserDefaults.standard.set(String(userId), forKey: "userId")
            UserDefaults.standard.synchronize()
            self.dismiss(animated: true, completion: nil)
        }) { (_) in
            
        }
    }
}
