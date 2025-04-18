//
//  AddPaymentViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 15/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Stripe
import Model

class AddPaymentViewController: EditViewController {
    
    fileprivate let cardField = STPPaymentCardTextField()
    fileprivate let logic: PaymentMethodsLogicController
    fileprivate let card: Payment.Card?
    
    init(_ logic: PaymentMethodsLogicController, replacing card: Payment.Card? = nil) {
        self.logic = logic
        self.card = card
        super.init(nibName: nil, bundle: nil)
        self.logic.context = self
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        cardField.delegate = self
        cardField.font = .theme(weight: .medium, size: .body)
        cardField.textColor = .black
        cardField.placeholderColor = .lightGray
        cardField.borderColor = .lightGray
        cardField.cursorColor = .gray
        cardField.postalCodeEntryEnabled = false

        actionContainer.update(
            left: .plain(title: "cancel".localized(), style: .plain, handler: { [unowned self] in
                self.close()
            }),
            right: .plain(title: "save".localized(), handler: { [unowned self] in
                self.save()
            }),
            priority: .right
        )

        contentView.insertArrangedSubview(cardField, at: 1)
        
        if let card = card {
            infoLabel.text = card.title
            iconView.image = .named("icon_menu_reservation")
            iconView.contentMode = .scaleAspectFit
            infoLabel.textAlignment = .left
        }
    }
    
    @objc fileprivate func save() {
        guard cardField.isValid else { return }
        Analytics.log(.saveCard())
        view.endEditing(true)
        startSaving()
        logic.createIntent(with: cardField.cardParams) { [weak self] result, title in
            self?.stopSaving()
            switch result {
            case .success:
                self?.success(title: title)
            case .failure(let error):
                if error.isInvalidCreditCard {
                    self?.warning(title: title, message: "credit_card_invalid_message".localized())
                } else if error.isCardExists {
                    self?.warning(title: title, message: "card_already_registered".localized())
                } else {
                    self?.warning()
                }
            }
            
        }
    }
    
    fileprivate func success(title: String) {
        stopSaving()
        NotificationCenter.default.post(name: .creditCardAdded, object: nil, userInfo: nil)
        let alert = AlertController(title: title, message: .plain("payment_card_added_message".localized()))
        alert.actions.append(.plain(title: "ok".localized()) { [unowned self] in
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                self.dismiss(animated: false, completion: nil)
            }
        })
        self.present(alert, animated: true, completion: nil)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        cardField.becomeFirstResponder()
    }
    
    override func startSaving() {
        super.startSaving()
        cardField.isEnabled = false
    }
    
    override func stopSaving() {
        super.stopSaving()
        cardField.isEnabled = true
    }
}

extension AddPaymentViewController: STPAuthenticationContext {
    func authenticationPresentingViewController() -> UIViewController {
        return self
    }
}

extension AddPaymentViewController: STPPaymentCardTextFieldDelegate {
    func paymentCardTextFieldDidChange(_ textField: STPPaymentCardTextField) {
//        actionButton.isActive = textField.isValid
    }
}
