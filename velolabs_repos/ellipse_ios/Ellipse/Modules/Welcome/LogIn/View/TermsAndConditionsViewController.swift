//
//  TermsAndConditionsViewController.swift
//  Ellipse
//
//  Created by Rupesh Kumar S on 05/02/17.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import UIKit
import Cartography

protocol TermsAndConditionsDelegate: class {
    func getTermsAndConditions()
    func acceptTermsAndConditions(_ isAccepted: Bool)
}

class TermsAndConditionsViewController : ViewController {
    var isLogin = false
    weak var delegate: TermsAndConditionsDelegate?
    
    fileprivate let textView = UITextView()
    fileprivate let buttonsContainer = UIView()
    fileprivate let acceptButton = UIButton(type: .custom)
    fileprivate let declineButton = UIButton(type: .custom)
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "terms_and_conditions_title".localized().capitalized
        view.backgroundColor = .white
        navigationItem.leftBarButtonItem = UIBarButtonItem(title: nil, style: .plain, target: nil, action: nil)
        
        configureUI()
        
        delegate?.getTermsAndConditions()
    }
    
    fileprivate func configureUI() {
        
        view.addSubview(textView)
        view.addSubview(buttonsContainer)
        buttonsContainer.addSubview(declineButton)
        buttonsContainer.addSubview(acceptButton)
        buttonsContainer.isHidden = !isLogin
        
        smallRoundedCornersStyle(acceptButton)
        acceptButton.setTitle("accept_uppercased".localized(), for: .normal)
        acceptButton.backgroundColor = .elDarkSkyBlue
        acceptButton.setTitleColor(.white, for: .normal)
        
        smallNegativeStyle(declineButton)
        declineButton.setTitle("decline".localized(), for: .normal)
        
        acceptButton.addTarget(self, action: #selector(acceptTerms), for: .touchUpInside)
        declineButton.addTarget(self, action: #selector(declineTerms), for: .touchUpInside)

        let margin: CGFloat = 20
        constrain(textView, buttonsContainer, declineButton, acceptButton, view) { text, container, decline, accept, view in
            text.top == view.safeAreaLayoutGuide.top
            text.left == view.left + margin
            text.right == view.right - margin
            
            container.left == text.left
            container.right == text.right
            container.top == text.bottom
            
            decline.left == container.left
            decline.right == accept.left - margin
            accept.right == container.right
            accept.width == decline.width
            decline.top == container.top + margin
            decline.bottom == container.bottom - margin
            accept.top == decline.top
            
            container.bottom == view.safeAreaLayoutGuide.bottom + (isLogin ? 0 : margin*4)
        }
        
        if isLogin == false {
            addCloseButton()
        }
    }

    @objc fileprivate func acceptTerms() {
        delegate?.acceptTermsAndConditions(true)
    }
    
    @objc fileprivate func declineTerms() {
        delegate?.acceptTermsAndConditions(false)
    }
}

extension TermsAndConditionsViewController : TermsInteractorOutput {
    func showTermsAndConditions(header: String, body: String) {
        textView.text = body
    }
}
