//
//  MembershipDetailsViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 04.08.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Atributika
import Model
import OvalAPI

class MembershipDetailsViewController: UIViewController {
    
    fileprivate let membership: Membership
    
    fileprivate let network: SubscriptionsAPI = AppRouter.shared.api()
    fileprivate let confirmButton = ActionButton()
    fileprivate let storage = CardStorage()
    fileprivate var currentCard: Payment.Card?
    fileprivate let stackView = UIStackView()
    fileprivate let paymentButton = UIButton(type: .custom)
    fileprivate var legalLabel: AttributedLabel!
    
    init(_ membership: Membership) {
        self.membership = membership
        super.init(nibName: nil, bundle: nil)
        if let url = membership.fleet.legal {
            self.legalLabel = .legal(self, text: "bike_details_terms_policy".localizedFormat(url))
        } else {
            self.legalLabel = .init()
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        
        view.backgroundColor = .white
        title = membership.fleet.name
        
        view.addSubview(legalLabel)
        view.addSubview(confirmButton)
        view.addSubview(stackView)
        
        stackView.axis = .vertical
        stackView.spacing = .margin
        
        constrain(legalLabel, confirmButton, stackView, view) { legal, confirm, stack, view in
            legal.bottom == view.safeAreaLayoutGuide.bottom - .margin
            legal.left == view.left + .margin
            legal.right == view.right - .margin
            
            confirm.bottom == legal.top - .margin
            confirm.left == view.left + .margin
            confirm.right == view.right - .margin
            
            stack.bottom <= confirm.top - .margin
            stack.left == confirm.left
            stack.right == confirm.right
            stack.top == view.safeAreaLayoutGuide.top + .margin
        }
        
        let becomeLabel = UILabel.label(text: "become_a_member".localized(), font: .theme(weight: .light, size: .body), allignment: .center, lines: 0)
        stackView.addArrangedSubview(becomeLabel)
        
        let joinLabel = UILabel.label(text: "join_today".localized(), font: .theme(weight: .light, size: .body), allignment: .center, lines: 0)
        stackView.addArrangedSubview(joinLabel)
        stackView.setCustomSpacing(0, after: joinLabel)
        
        if let price = membership.priceString {
            let priceLabel = UILabel.label(text: "membership_price".localizedFormat(price, membership.frequency.priceCycle), font: .theme(weight: .bold, size: .body), allignment: .center, lines: 0)
            stackView.addArrangedSubview(priceLabel)
        }
        
        let perkLabel = UILabel.label(text: "membership_perk".localizedFormat(membership.incentive.string()), font: .theme(weight: .light, size: .body), allignment: .center, lines: 0)
        stackView.addArrangedSubview(perkLabel)
        
        paymentButton.tintColor = .black
        paymentButton.setTitleColor(.black, for: .normal)
        paymentButton.titleLabel?.font = .theme(weight: .bold, size: .text)
        paymentButton.titleEdgeInsets = .init(top: 0, left: .margin, bottom: 0, right: 0)
        paymentButton.addTarget(self, action: #selector(openPaymentMethods), for: .touchUpInside)
        stackView.addArrangedSubview(paymentButton)
        
        storage.fetch { [unowned self] (cards) in
            self.currentCard = cards.first(where: {Payment.card($0).isCurrent})
            self.renderActions()
        }
        
        NotificationCenter.default.addObserver(self, selector: #selector(renderActions), name: .creditCardUpdated, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(renderActions), name: .creditCardAdded, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(renderActions), name: .creditCardRemoved, object: nil)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    @objc
    fileprivate func renderActions() {
        if let card = currentCard {
            paymentButton.setImage(card.icon, for: .normal)
            paymentButton.setTitle(card.title, for: .normal)
            confirmButton.action = .plain(title: "confirm".localized()) { [unowned self] in
                self.subscribe()
            }
        } else {
            paymentButton.setImage(nil, for: .normal)
            paymentButton.setTitle(nil, for: .normal)
            confirmButton.action = .plain(title: "add_credit_card".localized()) { [unowned self] in
                self.openPaymentMethods()
            }
        }
    }
    
    fileprivate func presentSuccess() {
        let alert = AlertController(title: "success".localized(), body: "subscription_success".localized()) {
            self.dismiss(animated: true, completion: nil)
        }
        present(alert, animated: true, completion: nil)
        NotificationCenter.default.post(name: .subscriptionsUpdated, object: nil)
    }
    
    @objc
    fileprivate func openPaymentMethods() {
        let payment = PaymentMethodsViewController(logic: .init(membership: membership))
        navigationController?.pushViewController(payment, animated: true)
    }
    
    fileprivate func subscribe() {
        startLoading("subscribing".localized())
        network.subscribe(to: membership) { [weak self] (result) in
            self?.stopLoading {
                switch result {
                case .failure(let error):
                    self?.handle(error)
                case .success:
                    self?.presentSuccess()
                }
            }
        }
    }
    
    override func handle(_ error: Error, from viewController: UIViewController, retryHandler: @escaping () -> Void) {
        if let e = error as? SessionError, case .conflict = e.code {
            let alert = AlertController(title: "general_error_title".localized(), body: "duplicated_membership_alert".localized(), handler: retryHandler)
            viewController.present(alert, animated: true, completion: nil)
            return
        }
        super.handle(error, from: viewController, retryHandler: retryHandler)
    }
}

extension PaymentMethodsLogicController {
    convenience init(membership: Membership) {
        self.init(gateway: membership.fleet.paymentSettings?.paymetnGateway ?? .stripe)
    }
}
