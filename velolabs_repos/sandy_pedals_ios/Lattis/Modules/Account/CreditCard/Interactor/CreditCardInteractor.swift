//
//  CreditCardCreditCardInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 30/06/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Foundation
import CreditCardValidator
import Oval
import Stripe

let creditCardAdded = Notification.Name("creditCardAdded")

class CreditCardInteractor: NSObject {
    weak var view: CreditCardInteractorOutput!
    var router: CreditCardRouter!
    var card: CreditCard!
    
    var postAction: (CreditCard) -> () = {_ in}
    
    fileprivate let validator = CreditCardValidator()
    fileprivate var validationStore: Set<CreditCard.Validation> = []
    fileprivate let storage: CreditCardStorage = CoreDataStack.shared
    fileprivate let network: CardsNetwork = Session.shared
    fileprivate var prevDate = ""
}

extension CreditCardInteractor: CreditCardInteractorInput {
    var firstResponder: CreditCard.Validation? {
        if card.number == nil {
            return .card
        }
        if card.expire == nil {
            return .date
        }
        if card.cvv == nil {
            return .cvv
        }
        return nil
    }
    
    func viewLoaded() {
        if let card = card {
            validationStore = [.card, .date]
            view.show(card: card, edit: true)
        } else {
            card = CreditCard()
            let idx = storage.cards.last?.cardId ?? storage.cards.count
            card.cardId = idx + 1
        }
    }
    
    func validate(text: String, type: CreditCard.Validation) -> String? {
        var result: String? = nil
        switch type {
        case .card: result = validate(cardNumber: text)
        case .cvv: result = validate(cvv: text)
        case .date: result = validate(date: text)
        }
        view.set(canSave: validationStore.count == 3)
        return result
    }
    
    func save() {
        view.startLoading(with: "credit_card_saving_loading".localized())
        network.creteIntent { [weak self] (result) in
            switch result {
            case .success(let secret):
                self?.handle(secret: secret)
            case .failure(let error):
                self?.view.show(error: error, file: #file, line: #line)
            }
        }
        
    }
    
    fileprivate func handle(secret: String) {
        let paymentMethodParams = STPPaymentMethodParams(card: card.stripeParams, billingDetails: nil, metadata: nil)
        STPAPIClient.shared().createPaymentMethod(with: paymentMethodParams) { [weak self] paymentMethod, error in
            if let e = error {
                self?.view.show(error: e, file: #file, line: #line)
                return
            }
            guard let `self` = self else { return }
            self.view.stopLoading {
                let setupIntentParams = STPSetupIntentConfirmParams(clientSecret: secret)
                setupIntentParams.paymentMethodID = paymentMethod?.stripeId
                let paymentManager = STPPaymentHandler.shared()
                paymentManager.confirmSetupIntent(withParams: setupIntentParams, authenticationContext: self, completion: { [weak self] (status, int, error) in
                    if let e = error {
                        self?.view.show(error: e, file: #file, line: #line)
                        return
                    }
                    guard let intent = int else { return }
                    switch status {
                    case .succeeded:
                        self?.card.intent = intent
                        self?.finish()
                    default:
                        break
                    }
                })
            }
        }
    }
    
    fileprivate func finish() {
        view.startLoading(with: "credit_card_saving_loading".localized())
        network.add(card: card) { [weak self]  (result) in
            switch result {
            case .success:
                self?.view.stopLoading{}
                NotificationCenter.default.post(name: creditCardAdded, object: nil)
                self?.router.pop()
            case .failure(let error):
                self?.view.show(error: error, file: #file, line: #line)
            }
        }
    }
    
    func scan() {
//        router.scan(with: self)
    }
    
    func delete() {
        storage.delete(card: card)
    }
}

//extension CreditCardInteractor: CardIOPaymentViewControllerDelegate {
//    func userDidCancel(_ paymentViewController: CardIOPaymentViewController!) {
//        paymentViewController.dismiss(animated: true, completion: nil)
//    }
//
//    func userDidProvide(_ cardInfo: CardIOCreditCardInfo!, in paymentViewController: CardIOPaymentViewController!) {
//        card = CreditCard(cardInfo: cardInfo)
//        view.show(card: card, edit: false)
//        paymentViewController.dismiss(animated: true, completion: nil)
//    }
//}

private extension CreditCardInteractor {
    func validate(cardNumber: String) -> String? {
        var number = validator.onlyNumbers(string: cardNumber)
        
        let typeName = validator.type(from: number)?.name
        card.cardType = typeName != nil ? CreditCard.CardType(rawValue: typeName!) : nil
        view.show(type: card?.cardType)
        if let type = card.cardType, number.count > type.limit {
            number = number.substring(to: type.limit)
        } else if number.count > 19 {
            number = number.substring(to: 19)
        }
        if validator.validate(string: number) {
            validationStore.insert(.card)
            if number.count == card.cardType?.limit {
                swithcFocus()
            }
        } else {
            validationStore.remove(.card)
        }
        card.number = number
        return card.formattedNumber
    }
    
    func validate(cvv: String) -> String? {
        if cvv.count >= 3 {
            validationStore.insert(.cvv)
            if cvv.count == 4 {
                swithcFocus()
            }
        } else {
            validationStore.remove(.cvv)
        }
        card.cvv = cvv
        return cvv.count > 4 ? cvv.substring(to: 4) : cvv
    }
    
    func validate(date: String) -> String? {
        let components = date.components(separatedBy: "/")
        if date.count > 3 && components.count == 2 {
            if date.count > 5 {
                return date.substring(to: 5)
            }
            if let year = UInt(components[1]) {
                if year < 17  {
                    return date.substring(to: 4)
                } else {
                    card.year = year
                }
            }
        } else if components.count == 1 && date.count > 0 {
            if let month = UInt(components[0]) {
                if month > 12 {
                    return date.substring(to: 1)
                } else {
                    card.month = month
                    if month > 1 && month < 10 && date.count == 1 {
                        prevDate = "0\(month)/"
                        return prevDate
                    }
                }
            }
            if date.count == 2 && prevDate.count < date.count {
                prevDate = date + "/"
                return prevDate
            }
        } else {
            card.month = nil
            card.year = nil
        }
        if card.month != nil && card.year != nil {
            validationStore.insert(.date)
            if date.count == 5 {
                swithcFocus()
            }
        } else {
            validationStore.remove(.date)
        }
        prevDate = date
        return date
    }
    
    func swithcFocus() {
        if validationStore.contains(.card) == false {
            view.switchFocusTo(field: .card)
        } else if validationStore.contains(.date) == false {
            view.switchFocusTo(field: .date)
        } else if validationStore.contains(.cvv) == false {
            view.switchFocusTo(field: .cvv)
        }
    }
}

extension CreditCardInteractor: STPAuthenticationContext {
    func authenticationPresentingViewController() -> UIViewController {
        return view as! ViewController
    }
}

extension String {
    func substring(to index: Int) -> String {
        guard count >= index else { return self }
        let temp = self as NSString
        return temp.substring(to: index) as String
    }
    
    func substring(from index: Int) -> String {
        guard count >= index else { return self }
        let temp = self as NSString
        return temp.substring(from: index) as String
    }
    
    func substring(fromReverse index: Int) -> String {
        guard count >= index else { return self }
        let temp = self as NSString
        return temp.substring(from: count - index) as String
    }
}

extension CreditCard {
    var stripeParams: STPPaymentMethodCardParams {
        let params = STPPaymentMethodCardParams()
        params.number = number
        params.expYear = year != nil ? NSNumber(value: year!) : nil
        params.expMonth = month != nil ? NSNumber(value: month!) : nil
        params.cvc = cvv
        return params
    }
}

//extension CreditCard {
//    init(cardInfo: CardIOCreditCardInfo) {
//        number = cardInfo.cardNumber
//        if cardInfo.expiryMonth > 0 {
//            month = cardInfo.expiryMonth
//        }
//        if cardInfo.expiryYear > 0 {
//            year = cardInfo.expiryYear - 2000
//        }
//        cvv = cardInfo.cvv
//        systemId = nil
//    }
//}
