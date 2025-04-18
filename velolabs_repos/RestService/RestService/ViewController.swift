//
//  ViewController.swift
//  RestService
//
//  Created by Ravil Khusainov on 18/12/2016.
//  Copyright © 2016 Lattis. All rights reserved.
//

import UIKit

class ViewController: UIViewController {
    @IBOutlet weak var codeField: UITextField!
    @IBOutlet weak var forgotCodeField: UITextField!
    @IBOutlet weak var newPassField: UITextField!
    @IBOutlet weak var newPhoneField: UITextField!
    
    let regUser = Oval.Users.Request(usersId: "+79600448886", regId: "", userType: .ellipse, phoneNumber: "+79600448886", password: "vefq2nfq", isSigningUp: true, countryCode: "ru", email: "ravil@lattis.io")
//    let logUser = Oval.Users.Request(usersId: "+79600448886", regId: "", userType: .ellipse, phoneNumber: "+79600448886", password: "vefq2nfq", isSigningUp: false, countryCode: "ru", email: "ravil@lattis.io")
    let logUser = Oval.Users.Request(usersId: "+79046785003", regId: "", userType: .ellipse, phoneNumber: "+79046785003", password: "vefq2nfq", isSigningUp: false, countryCode: "ru", email: "zver.kaban@gmail.com")

    override func viewDidLoad() {
        super.viewDidLoad()
        
//        Oval.users.refreshTokens(success: {}, fail: {print($0)})
    }

    @IBAction func signUp(_ sender: Any) {
        Oval.users.registration(user: regUser, success: { (result,_) in
            print(result)
        }) { (error) in
            
        }
    }
    
    @IBAction func Login(_ sender: Any) {
        Oval.users.registration(user: logUser, success: { (userId, isVerified) in
            Oval.users.getTokens(userId: userId, password: self.logUser.password!, success: {
                Oval.users.user(success: {_ in}, fail: {print($0)})
            }, fail: {print($0)})
        }) { (error) in
            
        }
    }
    
    @IBAction func confirm(_ sender: Any) {
        Oval.users.confirm(signIn: codeField.text!, success: {
            
        }) { (error) in
            
        }
    }
    
    @IBAction func forgot(_ sender: Any) {
        Oval.users.forgotPassword(phone: "+79600448886", success: {}, fail: {_ in})
    }
    
    @IBAction func confirmForgot(_ sender: Any) {
//        Oval.users.confirm(forgot: forgotCodeField.text!, phone: "+79600448886", password: "vefq2nfq", success: {_, _ in}, fail: {_ in})
    }
    @IBAction func requestPassCode(_ sender: Any) {
        Oval.users.getUpdatePasswordCode(success: {}, fail: {_ in})
    }
    @IBAction func confirmPassCode(_ sender: Any) {
        Oval.users.update(password: "vefq2nfq", with: newPassField.text!, success: {}, fail: {_ in})
    }
    @IBAction func requestPhoneCode(_ sender: Any) {
        Oval.users.getUpdateCode(for: "+79600448886", success: {}, fail: { print($0) })
    }
    @IBAction func confirmPhoneCode(_ sender: Any) {
        Oval.users.update(phoneNumber: "+79600448886", with: newPhoneField.text!, success: {}, fail: { print($0) })
    }
    @IBAction func accept(_ sender: Any) {
        Oval.users.acceptTermsAndConditions(success: {}, fail: {print($0)})
    }
    
    @IBAction func upload(_ sender: Any) {
        let url = Bundle.main.url(forResource: "IMG_0448", withExtension: "JPG")!
        let data = try! Data(contentsOf: url)
        Oval.misc.upload(data: data, for: .bike, success: {print($0)}, fail: {print($0)})
    }
}

