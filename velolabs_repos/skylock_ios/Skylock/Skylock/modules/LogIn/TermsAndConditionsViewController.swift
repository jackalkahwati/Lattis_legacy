//
//  TermsAndConditionsViewController.swift
//  Ellipse
//
//  Created by Rupesh Kumar S on 05/02/17.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import Foundation
import SwiftyJSON
import Localize_Swift
import RestService

class TermsAndConditionsViewController : SLBaseViewController {
    
    @IBOutlet weak var termsAndConditionsHeading: UILabel!
    @IBOutlet weak var termsAndConditionsDetails: UITextView!
    @IBOutlet weak var buttonsBottomLayout: NSLayoutConstraint!
    @IBOutlet weak var buttonsContainer: UIView!
    
    var isLogin = false
    var interactor: LogInInteractorInput!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        title = "TERMS AND CONDITIONS".localized()
        addMenuButton()
        interactor.getTermsAndConditions()
        if isLogin == false {
            buttonsBottomLayout.constant = -92
            view.layoutIfNeeded()
            buttonsContainer.isHidden = true
        }
    }
    
    @IBAction func dontAccept(_ sender: Any) {
        interactor.acceptTermsAndConditions(false)
    }
    
    @IBAction func accept(_ sender: Any){
        interactor.acceptTermsAndConditions(true)
    }
}

extension TermsAndConditionsViewController : LogInInteractorOutput {
    func showTermsAndConditions(header: String, body: String) {
        termsAndConditionsDetails.text = body
    }
}
