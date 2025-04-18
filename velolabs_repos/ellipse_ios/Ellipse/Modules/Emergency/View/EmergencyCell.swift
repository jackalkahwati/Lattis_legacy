//
//  EmergencyCell.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/14/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography

protocol EmergencyCellDelegate: class {
    func remove(contact: Contact)
}

class EmergencyCell: UITableViewCell {
    
    weak var delegate: EmergencyCellDelegate?
    fileprivate let nameLabel = UILabel()
    fileprivate let phoneLabel = UILabel()
    fileprivate let removeButton = UIButton(type: .custom)
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        selectionStyle = .none
        
        let containerView = UIView()
        contentView.addSubview(containerView)
        containerView.addSubview(nameLabel)
        containerView.addSubview(phoneLabel)
        contentView.addSubview(removeButton)
//        smallNegativeStyle(removeButton)
//        removeButton.setTitle("remove".localized(), for: .normal)
        removeButton.setImage(UIImage(named: "trash_can_gray"), for: .normal)
        removeButton.contentEdgeInsets = .init(top: .margin, left: .margin, bottom: .margin, right: .margin)
        nameLabel.font = .elButtonSmall
        nameLabel.textColor = .black
        phoneLabel.font = .elRegularSmall
        phoneLabel.textColor = .darkGray
        
        constrain(containerView, nameLabel, phoneLabel, removeButton, contentView) { container, name, phone, remove, view in
            remove.centerY  == view.centerY
            remove.right == view.right
            
            container.centerY == view.centerY
            container.left == view.left + .margin
            container.right == remove.right - .margin/2
            
            name.top == container.top
            name.left == container.left
            name.right == container.right
            
            phone.top == name.bottom
            phone.left == name.left
            phone.right == name.right
            phone.bottom == container.bottom
        }
        
        removeButton.addTarget(self, action: #selector(remove(_:)), for: .touchUpInside)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var contact: Contact? {
        didSet {
            nameLabel.text = contact?.fullName
            phoneLabel.text = contact?.primaryNumber
        }
    }
    
    @objc fileprivate func remove(_ sender: Any) {
        guard let contact = contact else { return }
        delegate?.remove(contact: contact)
    }
}
