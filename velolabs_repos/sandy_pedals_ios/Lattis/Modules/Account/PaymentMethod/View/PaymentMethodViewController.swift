//
//  PaymentMethodPaymentMethodViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 30/06/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

class PaymentMethodViewController: ViewController {
    var interactor: PaymentMethodInteractorInput!
    let payView = PaymentMethodView()
    fileprivate var cards: [CreditCardCellType] = []
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
    
    override func loadView() {
        view = payView
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        title = "payment_title".localized().uppercased()
        navigationItem.leftBarButtonItem = .close(target: self, action: #selector(close))
        
        payView.tableView.delegate = self
        payView.tableView.dataSource = self
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        interactor.viewWillAppear()
    }
    
    @objc private func close() {
        dismiss(animated: true, completion: nil)
        interactor.close()
    }
}

extension PaymentMethodViewController: PaymentMethodInteractorOutput {
    func show(cards: [CreditCard]) {
        self.cards = cards.count > 0 ? cards.map({CreditCardCellType.card($0)}) : [.empty]
        self.cards.append(.add)
        payView.tableView.reloadData()
    }
}

extension PaymentMethodViewController: UITableViewDataSource, UITableViewDelegate {
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return cards.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let type = cards[indexPath.row]
        let cell = tableView.dequeueReusableCell(withIdentifier: type.identifire, for: indexPath)
        if let cell = cell as? CreditCardCell, case let .card(card) = type  {
            cell.accessory = interactor.accessory
            cell.card = card
        }
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        switch cards[indexPath.row] {
        case .card(let card) where interactor.accessory == .select && card.isCurrent == false:
            interactor.select(card: card)
        case .add: interactor.open(card: nil)
        default:
            break
        }
    }
    
    func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        return indexPath.row < (cards.count - 1)
    }
    
    func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCell.EditingStyle, forRowAt indexPath: IndexPath) {
        let cellType = cards[indexPath.row]
        guard editingStyle == .delete, case let .card(card) = cellType else { return }
        let dialog = ActionAlertView.alert(title: "profile_verification_title".localized(), subtitle: "credit_card_delete_confirmation".localized())
        dialog.action = AlertAction(title: "menu_logout_confirm".localized()) {
            self.interactor.delete(card: card)
        }
        dialog.cancel = AlertAction(title: "menu_logout_cancel".localized(), action: {})
        dialog.show()
    }
    
    func tableView(_ tableView: UITableView, titleForDeleteConfirmationButtonForRowAt indexPath: IndexPath) -> String? {
        return "delete_account_submit".localized()
    }
}
