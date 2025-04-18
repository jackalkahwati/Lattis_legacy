//
//  EditPaymentCardDateCell.swift
//  LattisCore
//
//  Created by Roger Molas on 8/31/22.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Model

protocol EditPaymentCardDateCellDelete {
    func didStartEditing(cell: EditPaymentCardDateCell, text: String)
    func didEndEditing(cell: EditPaymentCardDateCell, text: String)
}

class EditPaymentCardDateCell: UITableViewCell {
    fileprivate let titleLabel = UILabel.label(font: .theme(weight: .medium, size: .body))
    fileprivate let textField = UITextField()
    fileprivate var isEditMode: Bool = false
    
    var delegate: EditPaymentCardDateCellDelete? = nil
    var payment: Payment! {
        didSet {
            if case let .card(card) = payment! {
                setExp(date: card.date)
                return
            }
            setExp(date: nil)
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        textField.font = .theme(weight: .book, size: .body)
        textField.returnKeyType = .done
        textField.delegate = self
        contentView.addSubview(titleLabel)
        contentView.addSubview(textField)

        constrain(titleLabel, contentView) {title, content in
            title.left == content.left + .margin
            title.centerY == content.centerY
            title.right == content.safeAreaLayoutGuide.right - .margin/2
        }
        constrain(textField, contentView) { textField, content in
            textField.left == content.left + .margin
            textField.centerY == content.centerY
            textField.right == content.safeAreaLayoutGuide.right - .margin/2
        }
        
        if isEditMode {
            titleLabel.isHidden = true
            textField.isHidden = false
        } else {
            titleLabel.isHidden = false
            textField.isHidden = true
        }
    }
    
    func setEditMode( _ isEditMode: Bool) {
        self.isEditMode = isEditMode
        if isEditMode {
            titleLabel.isHidden = true
            textField.isHidden = false
            textField.becomeFirstResponder()
        } else {
            titleLabel.isHidden = false
            textField.isHidden = true
            textField.resignFirstResponder()
        }
    }
    
    fileprivate func setExp(date: String?) {
        textField.text = date
        titleLabel.text = date
        if isEditMode {
            textField.becomeFirstResponder()
        } else {
            textField.resignFirstResponder()
        }
    }
    
    @objc
    fileprivate func selectExpDate() {
        let picker = CardExpDatePicker(title: "pickup".localized(), date: Date()) { [unowned self] (date) in
            let formatter = DateFormatter()
            formatter.dateFormat = "MM/YYYY"
            let expDate = formatter.string(from: date)
            textField.text = expDate
            delegate?.didStartEditing(cell: self, text: String(expDate))
        }
        (delegate as? UIViewController)?.present(picker, animated: true)
    }
}

//MARK: - UITextFieldDelegate
extension EditPaymentCardDateCell: UITextFieldDelegate {
    
    func textFieldShouldBeginEditing(_ textField: UITextField) -> Bool {
        self.selectExpDate()
        return true
    }
}

//EXT
extension String {
    var isValidCardDate: Bool {
        guard self != "" else { return true }
        let hexSet = CharacterSet(charactersIn: "0123456789/")
        let newSet = CharacterSet(charactersIn: self)
        return hexSet.isSuperset(of: newSet)
    }
}
