//
//  EditCardViewController.swift
//  LattisCore
//
//  Created by Roger Molas on 8/30/22.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Model

class EditCardViewController: UIViewController {
    fileprivate let tableView = UITableView()
    fileprivate let editButton = ActionButton()
    fileprivate let deleteButton = ActionButton()
    fileprivate var payment:Payment? = nil
    fileprivate var isEditingMode:Bool = false
    fileprivate var newDate: String = ""

    var editCallBack: ((Payment.Card.Update)-> Void)? = nil
    var deleteCallBack: ((Payment)-> Void)? = nil
    
    init(payment: Payment? = nil) {
        self.payment = payment
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

        title = "update_payment_details".localized()
        view.backgroundColor = .white
        view.addSubview(tableView)
        addCloseButton()
        tableView.register(EditPaymentCardCell.self, forCellReuseIdentifier: "card")
        tableView.register(EditPaymentCardDateCell.self, forCellReuseIdentifier: "card_date")
        tableView.separatorInset = .init(top: 0, left: .margin, bottom: 0, right: .margin)
        tableView.separatorStyle = .none
        tableView.rowHeight = 60
        tableView.estimatedSectionHeaderHeight = 100
        tableView.sectionHeaderHeight = UITableView.automaticDimension
        tableView.delegate = self
        tableView.dataSource = self
        tableView.tableFooterView = UIView()
        
        if case let .card(card) = payment! {
            newDate = card.date
        }
        
        editButton.action = .plain(title: "edit".localized(), handler: { [unowned self] in
            self.isEditingMode = !self.isEditingMode
            if isEditingMode {
                editButton.setTitle( "save".localized(), for: .normal)
            } else {
                if case let .card(card) = payment! {
                    let component = newDate.components(separatedBy: "/")
                    let month = NSString(string: component.first!).intValue
                    let year = NSString(string: component.last!).intValue
                    let newCard = Payment.Card.Update(
                        cardId: card.cardId, month: Int(month), year: Int(year))
                    self.editCallBack!(newCard!)
                }
                editButton.setTitle( "edit".localized(), for: .normal)
            }
            tableView.reloadData()
        })
        
        deleteButton.action = .plain(title: "delete".localized(), handler: { [unowned self] in
            if let payment = self.payment {
                deleteCallBack!(payment)
            }
        })
        
        view.addSubview(editButton)
        view.addSubview(deleteButton)
        
        constrain(tableView, editButton, deleteButton, view) { table, edit, delete, view in
            table.top == view.safeAreaLayoutGuide.top
            table.left == view.left
            table.right == view.right
                        
            edit.bottom == delete.top - .margin
            edit.left == view.left + .margin
            edit.right == view.right - .margin
            
            delete.bottom == view.safeAreaLayoutGuide.bottom - .margin
            delete.left == view.left + .margin
            delete.right == view.right - .margin
            
            table.bottom == edit.top - .margin
        }
    }
}

//MARK: - UITableViewDataSource
extension EditCardViewController: UITableViewDataSource, UITableViewDelegate {
    func numberOfSections(in tableView: UITableView) -> Int { 2 }
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int { 1 }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        if indexPath.section == 0 {
            let cell = tableView.dequeueReusableCell(withIdentifier: "card", for: indexPath) as! EditPaymentCardCell
            cell.payment = self.payment
            return cell
        }
        let cell = tableView.dequeueReusableCell(withIdentifier: "card_date", for: indexPath) as! EditPaymentCardDateCell
        cell.payment = self.payment
        cell.setEditMode(self.isEditingMode)
        cell.delegate = self
        return cell
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        return HeaderView(title: section == 0 ? "card_number".localized() : "card_exp_date".localized())
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
    }
}

//MARK: - EditPaymentCardDateCellDelete
extension EditCardViewController: EditPaymentCardDateCellDelete {
    func didStartEditing(cell: EditPaymentCardDateCell, text: String) {
        newDate = text
    }
    
    func didEndEditing(cell: EditPaymentCardDateCell, text: String) {
        newDate = text
    }
}

//MARK: -
fileprivate extension EditCardViewController {
    class HeaderView: UIView {
        init(title: String) {
            super.init(frame: .zero)
            backgroundColor = .white
            let titleLabel = UILabel.label(text: title, font: .theme(weight: .medium, size: .title))
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
