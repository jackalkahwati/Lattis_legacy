//
//  CreditCardCreditCardViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 30/06/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit
import Oval

class CreditCardViewController: ViewController {
    var interactor: CreditCardInteractorInput!
    let cardView = CreditCardView.nib() as! CreditCardView
    
    var barStyle: UIStatusBarStyle = .lightContent
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return barStyle
    }
    
    override func loadView() {
        view = cardView
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "credit_card_title_add".localized()
        navigationItem.leftBarButtonItem = .back(target: self, action: #selector(back))
        
        cardView.saveButton.addTarget(self, action: #selector(save), for: .touchUpInside)
        cardView.scanButton.addTarget(self, action: #selector(scan), for: .touchUpInside)
        
        cardView.numberField.delegate = self
        cardView.dateField.delegate = self
        cardView.cvvField.delegate = self
        
        interactor.viewLoaded()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        if let responder = interactor.firstResponder {
            switchFocusTo(field: responder)
        }
    }
    
    @IBAction func textChanged(_ sender: UITextField) {
        
    }
    
    @objc private func back() {
        navigationController?.popViewController(animated: true)
    }
    
    @objc private func scan() {
        interactor.scan()
    }
    
    @objc private func save() {
        view.endEditing(true)
        interactor.save()
    }
}

extension CreditCardViewController: CreditCardInteractorOutput {
    func show(card: CreditCard, edit: Bool) {
        cardView.dateField.text = card.expire
        if edit {
            title = "credit_card_title_edit".localized()
            cardView.numberField.isEnabled = false
            cardView.numberField.textColor = .lsWarmGrey
            cardView.numberField.text = card.maskNumber
            cardView.scanButton.isEnabled = false
//            navigationItem.rightBarButtonItem = UIBarButtonItem(title: "credit_card_delete".localized(), style: .plain, target: self, action: #selector(remove))
        } else {
            cardView.numberField.text = card.number
            cardView.cvvField.text = card.cvv
            
            if let  text = interactor.validate(text: cardView.numberField.text!, type: .card) {
                cardView.numberField.text = text
            }
            if let  text = interactor.validate(text: cardView.cvvField.text!, type: .cvv) {
                cardView.cvvField.text = text
            }
            if let  text = interactor.validate(text: cardView.dateField.text!, type: .date) {
                cardView.dateField.text = text
            }
        }
    }
    
    func show(type: CreditCard.CardType?) {
        cardView.cardIconView.image = type?.icon ?? #imageLiteral(resourceName: "icon_credit_card")
    }
    
    func set(canSave: Bool) {
        cardView.canSave = canSave
    }
    
    
    func switchFocusTo(field: CreditCard.Validation) {
        switch field {
        case .date:
            cardView.dateField.becomeFirstResponder()
        case .cvv:
            cardView.cvvField.becomeFirstResponder()
        default:
            cardView.numberField.becomeFirstResponder()
        }
    }
    
    override func show(error: Error, file: String, line: Int) {
        if let err = error as? SessionError {
            Analytics.report(error, file: file, line: line)
            switch err.code {
            case .conflict:
                warning(with: "credit_card_invalid_title".localized(), subtitle: "credit_card_invalid_text".localized())
            default:
                return
            }
        } else if let err = error as? ServerError, err.code == 410 { // Existing card adding attempt
            Analytics.report(error)
            warning(with: "credit_card_exists_title".localized(), subtitle: "credit_card_exists_text".localized())
        } else {
            super.show(error: error, file: file, line: line)
        }
    }
}

extension CreditCardViewController: UITextFieldDelegate {
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        guard let newText = (textField.text as NSString?)?.replacingCharacters(in: range, with: string) else { return true }
        guard let result = interactor.validate(text: newText, type: validationType(for: textField)) else { return true }
        textField.text = result
        return false
    }
    
    func textFieldShouldBeginEditing(_ textField: UITextField) -> Bool {
        cardView.keyboardWillShow(textField)
        return true
    }
}

private extension CreditCardViewController {
    func validationType(for textField: UITextField) -> CreditCard.Validation {
        switch textField {
        case cardView.dateField: return .date
        case cardView.cvvField: return .cvv
        default: return .card
        }
    }
    
    @objc func remove() {
        view.endEditing(true)
        let dialog = ActionAlertView.alert(title: "credit_card_delete_title".localized(), subtitle: "credit_card_delete_subtitle".localized())
        dialog.action = AlertAction(title: "credit_card_delete_action".localized(), action: interactor.delete)
        dialog.cancel = AlertAction(title: "credit_card_delete_cancel".localized(), action: {})
        dialog.show()
    }
}
