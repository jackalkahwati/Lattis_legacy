//
//  AxaLockDetailsCell.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 18.03.2020.
//  Copyright © 2020 Lattis. All rights reserved.
//

import UIKit
import Cartography

class AxaLockDetailsCell: UITableViewCell {
    
    fileprivate let container = UIStackView()
    fileprivate let titleLabel = UILabel()
    fileprivate let valueLabel = UILabel()
    fileprivate let actionButton = ActionButton(.none)
    
    var info: AxaLockDetailsInfo! {
        didSet {
            update(text: info.title, for: titleLabel)
            update(text: info.value, for: valueLabel)
            update(action: info.action)
        }
    }
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        titleLabel.font = .systemFont(ofSize: 14)
        titleLabel.textColor = .lightGray
        valueLabel.font = .boldSystemFont(ofSize: 14)
        valueLabel.textColor = .gray
        contentView.addSubview(container)
        constrain(container, contentView) { con, view in
            con.edges == view.edges.inseted(by: .margin)
        }
        container.spacing = .margin/2
        actionButton.layer.cornerRadius = 5
        actionButton.contentEdgeInsets = .init(top: .margin/2, left: .margin, bottom: .margin/2, right: .margin)
        actionButton.addTarget(self, action: #selector(handleAction), for: .touchUpInside)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    fileprivate func update(text: String?, for label: UILabel) {
        if let title = text {
            label.text = title
            if !container.arrangedSubviews.contains(label) {
                container.addArrangedSubview(label)
            }
        } else if container.arrangedSubviews.contains(label) {
            container.removeArrangedSubview(label)
            label.removeFromSuperview()
        }
    }
    
    fileprivate func update(action: ActionButton.Action?) {
        if let a = action {
            actionButton.action = a
            if !container.arrangedSubviews.contains(actionButton) {
                container.addArrangedSubview(actionButton)
            }
        } else if container.arrangedSubviews.contains(actionButton) {
            container.removeArrangedSubview(actionButton)
            actionButton.removeFromSuperview()
        }
    }
    
    @objc
    fileprivate func handleAction() {
        actionButton.action.handler?()
    }
}
