//
//  HelpViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 03.06.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import MessageUI
import OvalAPI

class HelpViewController: UIViewController, MFMailComposeViewControllerDelegate {
    
    let phoneNumber: String
    fileprivate let email: String
    fileprivate let topStack = UIStackView()
    fileprivate let bottomStack = UIStackView()
    
    init(_ phoneNumber: String) {
        self.phoneNumber = phoneNumber
        self.email = Status.email
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        Analytics.log(.help())
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }

        title = "help".localized()
        addCloseButton()
        view.backgroundColor = .white
        view.addSubview(topStack)
        view.addSubview(bottomStack)
        
        
        constrain(topStack, bottomStack, view) { top, bottom, view in
            top.top == view.safeAreaLayoutGuide.top + .margin
            top.left == view.left + .margin
            top.right == view.right - .margin
            
            bottom.bottom == view.safeAreaLayoutGuide.bottom - .margin
            bottom.left == view.left + .margin
            bottom.right == view.right - .margin
            
            top.bottom <= bottom.top - .margin
        }
        
        topStack.axis = .vertical
        topStack.spacing = .margin
//        stackView.distribution = .equalCentering
        
        topStack.addArrangedSubview(UILabel.label(text: "help_message".localized(), font: .theme(weight: .light, size: .body), allignment: .center, lines: 0))
        
        let emailTitle = UILabel.label(text: "email".localized(), font: .theme(weight: .light, size: .body), allignment: .center)
        topStack.addArrangedSubview(emailTitle)
        
        let emailButton = UIButton(type: .custom)
        emailButton.setTitle(email, for: .normal)
        emailButton.titleLabel?.font = .theme(weight: .bold, size: .body)
        emailButton.titleEdgeInsets = .init(top: 0, left: .margin/2, bottom: 0, right: 0)
        emailButton.setTitleColor(.black, for: .normal)
        emailButton.tintColor = .black
        emailButton.addTarget(self, action: #selector(handleEmail), for: .touchUpInside)
        topStack.addArrangedSubview(emailButton)
        topStack.setCustomSpacing(0, after: emailTitle)
        
        let phoneTitle = UILabel.label(text: "phone_number".localized(), font: .theme(weight: .light, size: .body), allignment: .center)
        topStack.addArrangedSubview(phoneTitle)
        
        let phoneButton = UIButton(type: .custom)
        phoneButton.setTitle(phoneNumber, for: .normal)
        phoneButton.titleLabel?.font = .theme(weight: .bold, size: .body)
        phoneButton.titleEdgeInsets = .init(top: 0, left: .margin/2, bottom: 0, right: 0)
        phoneButton.setTitleColor(.black, for: .normal)
        phoneButton.tintColor = .black
        phoneButton.addTarget(self, action: #selector(handlePhone), for: .touchUpInside)
        topStack.addArrangedSubview(phoneButton)
        topStack.setCustomSpacing(0, after: phoneTitle)
        
        if let string = Status.weblink, let weblink = URL(string: string) {
            let faqTitle = UILabel.label(text: "faq".localized(), font: .theme(weight: .light, size: .body), allignment: .center)
            topStack.addArrangedSubview(faqTitle)
            
            let faqButton = UIButton(type: .custom)
            faqButton.setTitle(weblink.absoluteString, for: .normal)
            faqButton.titleLabel?.font = .theme(weight: .bold, size: .body)
            faqButton.titleEdgeInsets = .init(top: 0, left: .margin/2, bottom: 0, right: 0)
            faqButton.setTitleColor(.black, for: .normal)
            faqButton.tintColor = .black
            faqButton.addTarget(self, action: #selector(handleWelink), for: .touchUpInside)
            topStack.addArrangedSubview(faqButton)
            topStack.setCustomSpacing(0, after: faqTitle)
        }
        
        if !TutorialManager.shared.files.isEmpty {
            let tutorials = ActionButton(action: .plain(title: "tutorial_label".localized()) { [unowned self] in
                TutorialManager.shared.present(from: self, compleiton: {})
            })
            bottomStack.axis = .vertical
            bottomStack.spacing = .margin
            bottomStack.addArrangedSubview(tutorials)
        }
    }
    
    @objc
    fileprivate func handlePhone() {
        if let url = URL(string: "tel://" + self.phoneNumber) {
            if UIApplication.shared.canOpenURL(url) {
                UIApplication.shared.open(url)
            }
        }
    }
    
    @objc
    fileprivate func handleEmail() {
        if MFMailComposeViewController.canSendMail() {
            let mail = MFMailComposeViewController()
            mail.mailComposeDelegate = self
            mail.setToRecipients([email])
            mail.setSubject("Need help with the App!")

            present(mail, animated: true)
        }
    }
    
    @objc
    fileprivate func handleWelink() {
        if let weblink = Status.weblink, let url = URL(string: weblink) {
            if UIApplication.shared.canOpenURL(url) {
                UIApplication.shared.open(url)
            }
        }
    }
    
    func mailComposeController(_ controller: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error: Error?) {
        controller.dismiss(animated: true)
    }
}
