//
//  SubscriptionDetailsViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 05.08.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Model

class SubscriptionDetailsViewController: UIViewController {
    
    fileprivate let subscription: Subscription
    fileprivate let cancelButton = ActionButton()
    fileprivate let stackView = UIStackView()
    fileprivate let network: SubscriptionsAPI = AppRouter.shared.api()
    fileprivate let paymentButton = UIButton(type: .custom)
    fileprivate let storage = CardStorage()
    
    init(_ subscription: Subscription) {
        self.subscription = subscription
        super.init(nibName: nil, bundle: nil)
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
        title = subscription.membership.fleet.name
        
        view.addSubview(cancelButton)
        view.addSubview(stackView)
        
        stackView.axis = .vertical
        stackView.spacing = .margin
        
        constrain(cancelButton, stackView, view) { cancel, stack, view in
            cancel.bottom == view.safeAreaLayoutGuide.bottom - .margin
            cancel.left == view.left + .margin
            cancel.right == view.right - .margin
            
            stack.bottom <= cancel.top - .margin
            stack.left == cancel.left
            stack.right == cancel.right
            stack.top == view.safeAreaLayoutGuide.top + .margin
        }
        
        let formatter = DateFormatter()
        formatter.dateStyle = .long
        formatter.doesRelativeDateFormatting = true
        
        let titleFont: UIFont = .theme(weight: .medium, size: .small)
        let infoFont: UIFont = .theme(weight: .book, size: .body)
        
        let startTitleLabel =  UILabel.label(text: "membership_start_date".localized(), font: titleFont)
        stackView.addArrangedSubview(startTitleLabel)
        stackView.setCustomSpacing(.margin/2, after: startTitleLabel)
        
        let startLabel = UILabel.label(text: formatter.string(from: subscription.periodStart), font: infoFont)
        stackView.addArrangedSubview(stack(from: startLabel, imageName: "icon_person"))
        
        if subscription.deactivatedAt != nil {
            let endTitleLabel =  UILabel.label(text: "membership_end_date".localized(), font: titleFont)
            stackView.addArrangedSubview(endTitleLabel)
            stackView.setCustomSpacing(.margin/2, after: endTitleLabel)
            
            let endLabel = UILabel.label(text: formatter.string(from: subscription.periodEnd), font: infoFont)
            stackView.addArrangedSubview(stack(from: endLabel, imageName: "icon_person"))
            cancelButton.isHidden = true
        }
        
        // Last billing
        if let lastBill = subscription.membership.activeSubs?.lastBilling {
            let lastBillingTitleLabel =  UILabel.label(
                text: "membership_last_billing".localized(), font: titleFont)
            stackView.addArrangedSubview(lastBillingTitleLabel)
            stackView.setCustomSpacing(.margin/2, after: lastBillingTitleLabel)
            
            let lastBillingLabel = UILabel.label(text: formatter.string(from: lastBill), font: infoFont)
            stackView.addArrangedSubview(stack(from: lastBillingLabel, imageName: "icon_person"))
        }
        
        if subscription.deactivatedAt == nil {
            if let nexBill = subscription.membership.activeSubs?.nextBilling {
                // Next billing
                let nextBillingTitleLabel =  UILabel.label(
                    text: "membership_next_billing".localized(), font: titleFont)
                stackView.addArrangedSubview(nextBillingTitleLabel)
                stackView.setCustomSpacing(.margin/2, after: nextBillingTitleLabel)
                
                let nextBillingLabel = UILabel.label(text: formatter.string(from: nexBill), font: infoFont)
                stackView.addArrangedSubview(stack(from: nextBillingLabel, imageName: "icon_person"))
            }
        }
        
        // Cycle
        let cycleTitleLabel = UILabel.label(text: "billing_cycle".localized(), font: titleFont)
        stackView.addArrangedSubview(cycleTitleLabel)
        stackView.setCustomSpacing(.margin/2, after: cycleTitleLabel)
        
        let cycleLabel = UILabel.label(text: subscription.membership.frequency.cycle, font: infoFont)
        stackView.addArrangedSubview(stack(from: cycleLabel, imageName: "icon_reservation"))
        
        let mem = subscription.membership
        if let price = mem.priceString {
            let chargeTitleLabel = UILabel.label(text: "charge".localized(), font: titleFont)
            stackView.addArrangedSubview(chargeTitleLabel)
            stackView.setCustomSpacing(.margin/2, after: chargeTitleLabel)
            
            let chargeLabel = UILabel.label(text: "membership_pricing_template".localizedFormat(price, mem.frequency.priceCycle), font: infoFont)
            stackView.addArrangedSubview(stack(from: chargeLabel, imageName: "icon_billing"))
        }
        
        let perk = subscription.membership.incentive
//        if  {
            let perkTitleLabel = UILabel.label(text: "perk".localized(), font: titleFont)
            stackView.addArrangedSubview(perkTitleLabel)
            stackView.setCustomSpacing(.margin/2, after: perkTitleLabel)
            
        let perkLabel = UILabel.label(text: "perk_template".localizedFormat(perk.string()), font: infoFont)
            stackView.addArrangedSubview(stack(from: perkLabel, imageName: "icon_membership"))
//        }
        
        let paymentMethodsTitleLabel = UILabel.label(text: "payment_method".localized(), font: titleFont)
        stackView.addArrangedSubview(paymentMethodsTitleLabel)
        stackView.setCustomSpacing(.margin/2, after: paymentMethodsTitleLabel)
        
        paymentButton.tintColor = .black
        paymentButton.contentHorizontalAlignment = .leading
        paymentButton.setTitleColor(.black, for: .normal)
        paymentButton.titleLabel?.font = infoFont
        paymentButton.titleEdgeInsets = .init(top: 0, left: .margin/2, bottom: 0, right: 0)
        paymentButton.addTarget(self, action: #selector(openPaymentMethods), for: .touchUpInside)
        stackView.addArrangedSubview(paymentButton)
        
        fetchCreditCatd()
        
        cancelButton.action = .plain(title: "cancel_membership".localized()) { [unowned self] in
            self.askForConfirmation()
        }
        
        NotificationCenter.default.addObserver(self, selector: #selector(fetchCreditCatd), name: .creditCardUpdated, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(fetchCreditCatd), name: .creditCardAdded, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(fetchCreditCatd), name: .creditCardRemoved, object: nil)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }

    
    @objc
    fileprivate func fetchCreditCatd() {
        storage.fetch { (cards) in
            self.refresh(card: cards.first(where: {Payment.card($0).isCurrent}))
        }
    }
    
    fileprivate func refresh(card: Payment.Card?) {
        paymentButton.setTitle(card?.title ?? Payment.addNew.title, for: .normal)
        paymentButton.setImage(card?.icon ?? Payment.addNew.icon, for: .normal)
    }
    
    fileprivate func askForConfirmation() {
        let alert = AlertController(title: "confirm".localized(), body: "cancel_membership_confirmation".localized())
        alert.actions = [
            .plain(title: "confirm".localized()) { [unowned self] in
                self.cancelSubscription()
            },
            .cancel
        ]
        present(alert, animated: true, completion: nil)
    }
    
    fileprivate func presentSuccess() {
        let alert = AlertController(title: "success".localized(), body: "cancel_membership_success".localized()) { [unowned self] in
            self.dismiss(animated: true, completion: nil)
        }
        present(alert, animated: true, completion: nil)
    }
    
    fileprivate func cancelSubscription() {
        let member = subscription.membership// else { return }
        startLoading("cancelling_subscription".localized())
        network.unsubscribe(from: member) { [weak self] (result) in
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
    
    fileprivate func stack(from label: UILabel, imageName: String) -> UIView {
        let imageView = UIImageView(image: .named(imageName))
        imageView.tintColor = .black
        imageView.setContentHuggingPriority(.required, for: .horizontal)
        let stack = UIStackView(arrangedSubviews: [imageView, label])
        stack.axis = .horizontal
        stack.spacing = .margin/2
        return stack
    }
    
    @objc
    fileprivate func openPaymentMethods() {
        let payment = PaymentMethodsViewController(logic: .init(subscription: subscription))
        navigationController?.pushViewController(payment, animated: true)
    }
}

extension PaymentMethodsLogicController {
    convenience init(subscription: Subscription) {
        self.init(gateway: subscription.membership.fleet.paymentSettings?.paymetnGateway ?? .stripe)
    }
}
