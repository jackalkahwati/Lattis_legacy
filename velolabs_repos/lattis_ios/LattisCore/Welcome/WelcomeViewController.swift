//
//  WelcomeViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 24/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import IHKeyboardAvoiding
import SafariServices

var welcomeEmail: String?
var welcomeFirstName: String?
var welcomeLastName: String?

protocol WelcomeDelegate: AnyObject {
    func signIn(user: User.LogIn, with loadingText: String)
    func switchCard(controller: WelcomeOptionController)
}

protocol WelcomeOptionController: UIViewController {
    var delegate: WelcomeDelegate? {get set}
    var keyboardPadding: CGFloat {get}
}

class WelcomeViewController: UIViewController {
    
    init(_ initialCard: @escaping () -> WelcomeOptionController = SignUpViewController.init) {
        self.initialCard = initialCard
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    fileprivate let initialCard: () -> WelcomeOptionController
    fileprivate var cardView: UIView?
    fileprivate var bottomLayout: NSLayoutConstraint?
    fileprivate var logoBottomLayout: NSLayoutConstraint?
    fileprivate let logo = UIImageView(image: UITheme.theme.logo)
    fileprivate let contentView = KeyboardDismissingView()
    
    fileprivate let network: UserAPI = AppRouter.shared.api()

    override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        
        KeyboardAvoiding.avoidingView = contentView
        
        view.backgroundColor = .background
        logo.tintColor = .accent
        logo.contentMode = .scaleAspectFit
        
        view.addSubview(logo)
        view.addSubview(contentView)
        
        logo.setContentCompressionResistancePriority(.defaultLow, for: .vertical)
        
        constrain(contentView, logo, view) { content, logo, view in
            content.edges == view.edges
            
            logo.top == view.safeAreaLayoutGuide.top + .margin
            logo.left == view.left + .margin
            logo.right == view.right - .margin
        }
        loadCard(content: initialCard())
    }
    
    fileprivate func loadCard(content: WelcomeOptionController) {
        content.delegate = self
        
        KeyboardAvoiding.paddingForCurrentAvoidingView = content.keyboardPadding
        
        let cardView = UIView()
        cardView.backgroundColor = .background
        cardView.layer.cornerRadius = 50
        cardView.layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        cardView.addShadow(offcet: .init(width: 0, height: -2))
        contentView.addSubview(cardView)
        
        content.willMove(toParent: self)
        addChild(content)
        content.didMove(toParent: self)
        cardView.addSubview(content.view)
        
        var bottom: NSLayoutConstraint?
        
        let oldLogo = logoBottomLayout
        
        constrain(cardView, content.view, logo, contentView) { card, content, logoView, view in
            bottom = card.bottom == view.bottom + (self.cardView == nil ? 0 : 500)
            
            card.left == view.left
            card.right == view.right ~ .defaultHigh
            
            self.logoBottomLayout = logoView.bottom <= card.top - .margin ~ .dragThatCanResizeScene
            
            content.edges == card.edges.inseted(horizontally: .margin)
        }
        
        view.layoutIfNeeded()
        if let old = oldLogo {
            NSLayoutConstraint.deactivate([old])
        }
        if let old = self.cardView {
            bottom?.constant = 0
            bottomLayout?.constant = 500
            UIView.animate(withDuration: 0.3, animations: {
                self.view.layoutIfNeeded()
            }, completion: { _ in
                self.bottomLayout = bottom
                old.removeFromSuperview()
                if let child = self.children.first(where: {$0.view == old.subviews.first}) {
                    child.removeFromParent()
                }
            })
        }
        
        self.cardView = cardView
        self.bottomLayout = bottom
    }
}

extension WelcomeViewController: WelcomeDelegate {
    func signIn(user: User.LogIn, with loadingText: String) {
        startLoading(loadingText)
        network.logIn(user: user) { [weak self] (result) in
            self?.stopLoading {
                switch result {
                case .success(let isVerified):
                    if isVerified {
                        AppRouter.shared.openDashboard()
                    } else {
                        self?.switchCard(controller: EmailConfirmationViewController(user.email))
                    }
                case .failure(let error):
                    self?.handle(error)
                }
            }
        }
    }
    
    func switchCard(controller: WelcomeOptionController) {
        loadCard(content: controller)
    }
    
    override func handle(_ error: Error, from viewController: UIViewController, retryHandler: @escaping () -> Void) {
//        if let controller = children.first(where: { $0 is WelcomeOptionController }) {
//            return controller.handle(error, from: viewController, retryHandler: retryHandler)
//        }
        if error.isInvalidConfirmationCode {
            let alert = AlertController(title: "general_error_title".localized(), body: "action_loginfailed_description".localized())
            return viewController.present(alert, animated: true, completion: nil)
        }
        super.handle(error, from: viewController, retryHandler: retryHandler)
    }
}

