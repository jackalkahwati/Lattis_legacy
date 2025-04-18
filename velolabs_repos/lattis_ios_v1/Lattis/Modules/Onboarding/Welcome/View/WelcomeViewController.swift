//
//  WelcomeWelcomeViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit
import SafariServices

fileprivate let terms = "https://lattis.io/pages/terms-of-use"
fileprivate let privacy = "https://lattis.io/pages/privacy-policy"

class WelcomeViewController: ViewController {
    @IBOutlet weak var textView: UITextView!
    @IBOutlet weak var logInButton: UIButton!
    @IBOutlet weak var signUpButton: UIButton!
    var interactor: WelcomeInteractorInput!

    override func viewDidLoad() {
        super.viewDidLoad()
        
        let text = NSMutableAttributedString(string: "welcome_terms_and_privacy_text".localized(), attributes: [NSAttributedString.Key.font: UIFont.systemFont(ofSize: 11), NSAttributedString.Key.foregroundColor: UIColor.white])
        var range = (text.string as NSString).range(of: "welcome_terms_text".localized())
        text.addAttributes([.link: terms], range: range)
        range = (text.string as NSString).range(of: "welcome_privacy_text".localized())
        text.addAttributes([.link: privacy], range: range)
        textView.attributedText = text
        textView.linkTextAttributes = [.underlineStyle: NSUnderlineStyle.single.rawValue,
                                       .foregroundColor: UIColor.white,
                                       .underlineColor: UIColor.white]
        textView.delegate = self
    }
    
    @IBAction func signUp(_ sender: Any) {
        interactor.openSignUp()
    }
    
    @IBAction func logIn(_ sender: Any) {
        interactor.openLogIn()
    }
    
    override var prefersStatusBarHidden: Bool {
        return true
    }
}

extension WelcomeViewController: WelcomeInteractorOutput {
	
}

extension WelcomeViewController: UITextViewDelegate {
    func textView(_ textView: UITextView, shouldInteractWith URL: URL, in characterRange: NSRange) -> Bool {
        let web = SFSafariViewController(url: URL)
        present(web, animated: true, completion: nil)
        return false
    }
}

private extension URL {
    var title: String {
        switch absoluteString {
        case terms:
            return "welcome_terms_text".localized()
        case privacy:
            return "welcome_privacy_text".localized()
        default:
            return ""
        }
    }
}
