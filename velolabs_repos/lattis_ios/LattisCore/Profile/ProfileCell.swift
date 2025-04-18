//
//  ProfileCell.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 10/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

class ProfileCell: UITableViewCell {
    
    fileprivate let titleLabel = UILabel.label(font: .theme(weight: .medium, size: .small))
    fileprivate let valueButton = UIButton(type: .custom)
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        let containerView = UIStackView(arrangedSubviews: [titleLabel, valueButton])
        containerView.axis = .vertical
        containerView.spacing = .margin/2
        valueButton.isUserInteractionEnabled = false
        valueButton.contentHorizontalAlignment = .leading
        valueButton.titleEdgeInsets = .init(top: 0, left: .margin/2, bottom: 0, right: 0)
        valueButton.tintColor = .black
        valueButton.setTitleColor(.black, for: .normal)
        contentView.addSubview(containerView)
        
        constrain(containerView, contentView) { container, content in
            container.left == content.left + .margin
            container.right == content.right - .margin
            container.bottom == content.bottom - .margin/2
            container.top == content.top + .margin/2
        }
    }
    
    var model: Model! {
        didSet {
            titleLabel.isHidden = model.title == nil
            titleLabel.text = model.title
            valueButton.setImage(model.icon, for: .normal)
            valueButton.setTitle(model.value, for: .normal)
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

extension ProfileCell {
    struct Model {
        let title: String?
        let value: String
        let icon: UIImage?
        let handler: () -> ()
    }
}
