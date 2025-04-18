//
//  MembershipsHeaderView.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 04.11.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

final class MembershipsHeaderView: UIView {
    fileprivate let titleLabel = UILabel.label(font: .theme(weight: .medium, size: .text))
    fileprivate let actionButton = UIButton(type: .custom)
    fileprivate let actionInfo: MembershipLogicController.ActionInfo
    
    init(_ actionInfo: MembershipLogicController.ActionInfo) {
        self.actionInfo = actionInfo
        titleLabel.text = actionInfo.title
        super.init(frame: .zero)
        
        backgroundColor = .white
        actionButton.titleLabel?.font = titleLabel.font
        actionButton.tintColor = .black
        actionButton.setTitleColor(.black, for: .normal)
        actionButton.setTitle(actionInfo.actionTitle, for: .normal)
        actionButton.setImage(actionInfo.actionIcon, for: .normal)
        actionButton.addTarget(self, action: #selector(performAction), for: .touchUpInside)
        
        addSubview(titleLabel)
        addSubview(actionButton)
        
        constrain(titleLabel, actionButton, self) { title, action, view in
            title.left == view.left + .margin
            title.centerY == view.centerY
            
            action.right == view.right - .margin
            action.centerY == view.centerY
        }
    }
    
    @objc
    fileprivate func performAction() {
        if let title = actionInfo.action?() {
            actionButton.setTitle(title, for: .normal)
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
