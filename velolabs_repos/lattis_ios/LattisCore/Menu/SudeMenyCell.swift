//
//  SudeMenyCell.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 08/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

struct MenuItem {
    let title: String
    let icon: UIImage?
    let module: () -> UIViewController
    let navigation: Bool
    let hideMenu: Bool
    let identifier: String
    
    init(title: String, icon: UIImage?, module: @escaping () -> UIViewController, navigation: Bool, hideMenu: Bool, identifier: String = "cell") {
        self.title = title
        self.icon = icon
        self.module = module
        self.navigation = navigation
        self.hideMenu = hideMenu
        self.identifier = identifier
    }
    
    class Section {
        let title: String?
        var items: [MenuItem]
        
        init(title: String? = nil, items: [MenuItem] = []) {
            self.title = title
            self.items = items
        }
    }
}

class SideMenuCell: UITableViewCell {
    
    fileprivate let iconImageView = UIImageView()
    fileprivate let titleLabel = UILabel()
    let bageView = BageView()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        contentView.addSubview(iconImageView)
        contentView.addSubview(titleLabel)
        contentView.addSubview(bageView)
        iconImageView.tintColor = .black
        iconImageView.contentMode = .scaleAspectFit
        
        titleLabel.font = .theme(weight: .book, size: .text)
        titleLabel.textColor = .black
        
        constrain(iconImageView, titleLabel, bageView, contentView) { icon, title, bage, content in
            icon.width == 22
            icon.left == content.left + .margin
            icon.centerY == content.centerY
            
            title.centerY == content.centerY
            title.left == icon.right + .margin/2
            title.right == bage.left - .margin/2
            
            bage.centerY == content.centerY
            bage.right == content.right - .margin/2
        }
    }
    
    var item: MenuItem! {
        didSet {
            titleLabel.text = item.title
            iconImageView.image = item.icon
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}



class MenuButton: UIButton {
    
    let item: MenuItem
    init(item: MenuItem) {
        self.item = item
        super.init(frame: .zero)
        
        setTitle(item.title, for: .normal)
        titleLabel?.font = .theme(weight: .book, size: .text)
        setTitleColor(.darkGray, for: .normal)
        contentHorizontalAlignment = .left
        setImage(item.icon, for: .normal)
        contentEdgeInsets = .init(top: 0, left: .margin, bottom: 0, right: .margin)
        titleEdgeInsets  = .init(top: 0, left: .margin, bottom: 0, right: 0)
        
        constrain(self) {$0.height == 54}
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
