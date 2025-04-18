//
//  PaymentMethodsViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 07/06/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Model

class PaymentMethodsViewController: UIViewController {
    
    fileprivate let tableView = UITableView()
    fileprivate var payments: [Payment] = []
    fileprivate let storage = CardStorage()
    fileprivate let newButton = ActionButton()
    fileprivate var currentIndexPath: IndexPath?
    fileprivate let paymentFooterView = UIView()
    fileprivate let paymentHeaderView = UIView()
    fileprivate let paymentHeaderLaber = UILabel.label(text: "add_card_description".localized(), font: .theme(weight: .book, size: .body), allignment: .center, lines: 0)
    fileprivate let logic: PaymentMethodsLogicController
    fileprivate let promoCodeButton = UIButton(type: .custom)
    fileprivate weak var editController: EditInfoViewController?
    
    init(logic: PaymentMethodsLogicController) {
        self.logic = logic
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

        title = "payment_methods".localized()
        if let nav = navigationController, nav.viewControllers.count > 1 {
//            addBackButton()
        } else {
            addCloseButton()
        }
        view.backgroundColor = .white
        
        view.addSubview(tableView)
        tableView.separatorInset = .init(top: 0, left: .margin, bottom: 0, right: .margin)
        tableView.register(PaymentCardCell.self, forCellReuseIdentifier: "payment")
        tableView.register(PromoCodeCell.self, forCellReuseIdentifier: "promotion")
        tableView.rowHeight = 44
        
        tableView.estimatedSectionHeaderHeight = 68
        tableView.sectionHeaderHeight = UITableView.automaticDimension
        
        newButton.action = .plain(title: Payment.addNew.title, handler: addNew)
        paymentFooterView.addSubview(newButton)
        paymentFooterView.backgroundColor = .white
        let lineView = UIView.line
        paymentFooterView.addSubview(lineView)
        
        constrain(newButton, lineView, paymentFooterView) { new, line, view in
            new.top == view.top + .margin
            new.left == view.left + .margin
            new.right == view.right - .margin
//            new.bottom == line.top - .margin*2
            
            line.left == new.left
            line.right == new.right
            line.bottom == view.bottom
            
//            view.width >= 200 ~ .defaultLow
        }
        
        paymentHeaderView.addSubview(paymentHeaderLaber)
        paymentHeaderView.backgroundColor = .white
        
        constrain(paymentHeaderLaber, paymentHeaderView) { label, view in
            label.edges == view.edges.inseted(by: .margin)
        }
        
        tableView.tableFooterView = UIView()
        
        tableView.dataSource = self
        tableView.delegate = self
        
        constrain(tableView, view) { table, view in
            table.edges == view.edges
        }
        
        promoCodeButton.setTitle("add_promo_code".localized(), for: .normal)
        promoCodeButton.tintColor = .neonBlue
        promoCodeButton.setTitleColor(.neonBlue, for: .normal)
        promoCodeButton.titleLabel?.font = .theme(weight: .light, size: .body)
        promoCodeButton.contentHorizontalAlignment = .leading
        promoCodeButton.titleEdgeInsets = .init(top: 0, left: .margin, bottom: 0, right: 0)
        promoCodeButton.addTarget(self, action: #selector(addPromoCode), for: .touchUpInside)
        
        storage.fetch { [weak self] (cards) in
            self?.calculate(cards: cards)
        }
        refresh()
        NotificationCenter.default.addObserver(self, selector: #selector(refresh), name: .creditCardAdded, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(refresh), name: .creditCardUpdated, object: nil)
        logic.fetch { [unowned self] in
            self.tableView.reloadData()
        }
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    @objc fileprivate func refresh() {
        storage.refresh { (error) in
            if let err = error {
                Analytics.report(err)
            }
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        navigationController?.setNavigationBarHidden(false, animated: animated)
    }
    
    fileprivate func calculate(cards: [Payment.Card]) {
        payments = cards.filter({$0.gateway == logic.gateway}).map{Payment.card($0)}
        tableView.reloadData()
    }
    
    @objc fileprivate func addNew() {
        logic.addCard(with: self)
    }
    
    fileprivate func swapCurrent(payment: inout Payment, indexPath: IndexPath) {
        payment.isCurrent = true
        let update = currentIndexPath == nil ? [indexPath] : [currentIndexPath!, indexPath]
        tableView.beginUpdates()
        tableView.reloadRows(at: update, with: .automatic)
        tableView.endUpdates()
        currentIndexPath = indexPath
        
        NotificationCenter.default.post(name: .creditCardUpdated, object: payment)
    }
    
    fileprivate func delete(card: Payment.Card, indexPath: IndexPath) {
        startLoading("removing_payment_method_loader".localized())
        storage.delete(card: card) { [weak self] (error) in
            self?.stopLoading {
                if let err = error {
                    Analytics.report(err)
                    self?.warning(title: card.title, message: "removing_payment_method_failed".localized())
                } else {
                    self?.payments.remove(at: indexPath.row)
                    self?.tableView.beginUpdates()
                    self?.tableView.deleteRows(at: [indexPath], with: .automatic)
                    self?.tableView.endUpdates()
                    if indexPath == self?.currentIndexPath {
                        self?.currentIndexPath = nil
                        if var first = self?.payments.first {
                            self?.swapCurrent(payment: &first, indexPath: .init(row: 0, section: 0))
                        }
                    }
                    NotificationCenter.default.post(name: .creditCardRemoved, object: nil)
                }
            }
        }
    }
    
    fileprivate func update(card: Payment.Card.Update, indexPath: IndexPath) {
        startLoading("update_payment_details".localized())
        storage.update(card: card) { [weak self] error in
            self?.stopLoading {
                if let err = error {
                    Analytics.report(err)
                    self?.warning(title: "general_error_title".localized(), message: "general_error_message".localized())
                } else {
                    NotificationCenter.default.post(name: .creditCardUpdated, object: nil)
                }
            }
        }
    }
    
    @objc
    fileprivate func addPromoCode() {
        let edit = EditInfoViewController(.promoCode({ [unowned self] (code) in
            self.logic.addPromo(code: code) { [unowned self] (result) in
                self.editController?.stopSaving()
                switch result {
                case .failure(let error):
                    self.editController?.showDefaultWarning()
                    Analytics.report(error)
                case .success(let paths):
                    self.editController?.dismiss(animated: true)
                    self.tableView.beginUpdates()
                    self.tableView.insertRows(at: paths, with: .automatic)
                    self.tableView.endUpdates()
                }
            }
        }), textView: .promoCode)
        editController = edit
        present(edit, animated: true)
    }
}

extension PaymentMethodsViewController: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        if indexPath.section == 0 {
            let cell = tableView.dequeueReusableCell(withIdentifier: "payment", for: indexPath) as! PaymentCardCell
            cell.payment = payments[indexPath.row]
            if cell.payment.isCurrent {
                currentIndexPath = indexPath
            }
            return cell
        } else {
            let cell = tableView.dequeueReusableCell(withIdentifier: "promotion", for: indexPath) as! PromoCodeCell
            cell.promotion = logic.promotions[indexPath.row]
            return cell
        }
    }
    
    func numberOfSections(in tableView: UITableView) -> Int { 2 }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        guard section == 0 else { return logic.promotions.count }
        return payments.count
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        guard indexPath.section == 0 else { return }
        tableView.deselectRow(at: indexPath, animated: true)
        var payment = payments[indexPath.row]

        if payment.isCurrent {
            let edit = EditCardViewController(payment: payment)
            edit.editCallBack = { card in
                edit.dismiss(animated: true)
                //TODO : API Call
                self.update(card: card, indexPath: self.currentIndexPath!)
            }
            edit.deleteCallBack = { [self] payment in
                edit.dismiss(animated: true)
                guard case let .card(card) = payment else { return }
                self.delete(card: card, indexPath: currentIndexPath!)
            }
            present(.navigation(edit), animated: true, completion: nil)
            return
        }
        
        guard !payment.isCurrent, case let .card(card) = payment else { return }
        startLoading("credit_card_selecting_loader".localized())
        storage.selectCurrent(card: card) { [weak self] (error) in
            self?.stopLoading {
                if let err = error {
                    Analytics.report(err)
                    self?.warning()
                } else {
                    self?.swapCurrent(payment: &payment, indexPath: indexPath)
                }
            }
        }
    }
    
    func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        if indexPath.section == 1 { return false } // Promocodes can't be removed
        return true
    }
    
    func tableView(_ tableView: UITableView, titleForDeleteConfirmationButtonForRowAt indexPath: IndexPath) -> String? {
//        payments.count > 1 ? "delete".localized() : "edit".localized()
        "delete".localized()
    }
    
    func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCell.EditingStyle, forRowAt indexPath: IndexPath) {
        var payment = payments[indexPath.row]
        guard editingStyle == .delete, case let .card(card) = payment else { return }
        guard payments.count > 1 else {
            let alert = AlertController(title: nil, body: "removing_single_card_alert".localized())
            present(alert, animated: true)
            return //logic.addCard(with: self, card: card)
        }
        let alert = AlertController(title: card.title, message: .plain("credit_card_delete_message".localized()))
        alert.actions = [
            .plain(title: "delete".localized()) { [unowned self] in
                payment.isCurrent = false
                self.delete(card: card, indexPath: indexPath)
            },
            .cancel
        ]
        present(alert, animated: true, completion: nil)
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        guard section == 0 else { return PromotionHeaderView() }
        return paymentHeaderView
    }
    
    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        guard section == 0 else { return 48 }
        return 88
    }

    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        guard section == 0 else { return promoCodeButton }
        return paymentFooterView
    }
}

fileprivate extension PaymentMethodsViewController {
    class PromotionHeaderView: UIView {
        let titleLabel = UILabel.label(text: "promotions".localized(), font: .theme(weight: .medium, size: .giant))
        
        init() {
            super.init(frame: .zero)
            backgroundColor = .white
            addSubview(titleLabel)
            
            constrain(titleLabel, self) { title, view in
                title.edges == view.edges.inseted(horizontally: .margin)
            }
        }
        
        required init?(coder: NSCoder) {
            fatalError("init(coder:) has not been implemented")
        }
    }
}

extension Payment {
    private static var currentKey: String {
        return String(describing: self) + ".current"
    }
    
    static var hasCurrent: Bool {
        return UserDefaults.standard.integer(forKey: Payment.currentKey) != 0
    }
    
    var isCurrent: Bool {
        set {
            guard newValue else { return }
            switch self {
            case .applePay:
                UserDefaults.standard.set(-1, forKey: Payment.currentKey)
            case .card(let card):
                UserDefaults.standard.set(card.id, forKey: Payment.currentKey)
            default:
                break
            }
            UserDefaults.standard.synchronize()
        }
        get {
            let id = UserDefaults.standard.integer(forKey: Payment.currentKey)
            switch self {
            case .applePay:
                return id == -1
            case .card(let card):
                return card.id == id
            default:
                return false
            }
        }
    }
}
