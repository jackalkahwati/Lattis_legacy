//
//  AlertController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 19/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

extension CGFloat {
    static let margin = CGFloat(16)
}

final class AlertController: UIViewController {
    
    var actions: [ActionButton.Action] = []
    
    fileprivate let contentView = UIView()
    fileprivate let titleLabel = UILabel()
    fileprivate let messageLabel = UILabel()
    
    init(title: String?, message: String?) {
        super.init(nibName: nil, bundle: nil)
        titleLabel.text = title
        messageLabel.text = message
        messageLabel.font = .systemFont(ofSize: 16)

        modalTransitionStyle = .crossDissolve
        modalPresentationStyle = .overCurrentContext
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = UIColor(white: 0.3, alpha: 0.8)
        view.addSubview(contentView)
        contentView.addSubview(titleLabel)
        contentView.addSubview(messageLabel)
        
        titleLabel.font = .systemFont(ofSize: 16, weight: .bold)
        titleLabel.textColor = .darkGray
        titleLabel.numberOfLines = 2
        titleLabel.textAlignment = .center
        
        messageLabel.textColor = .gray
        messageLabel.numberOfLines = 0
        messageLabel.textAlignment = .center
        
        contentView.backgroundColor = .white
        contentView.layer.cornerRadius = 10
        
        var c: ViewProxy!
        var m: ViewProxy!
        constrain(contentView, titleLabel, messageLabel, view) { content, title, message, view in
            c = content
            m = message
            content.left == view.left + .margin
            content.right == view.right - .margin
            content.centerY == view.centerY
            
            title.top == content.top + .margin
            title.left == content.left + .margin
            title.right == content.right - .margin
            
            message.top == title.bottom + .margin
            message.left == title.left
            message.right == title.right
        }
        
        let buttons = actions.map(ActionButton.init)
        buttons.forEach{ button in
            contentView.addSubview(button)
            button.addTarget(self, action: #selector(self.handleAction(_:)), for: .touchUpInside)
            button.layer.cornerRadius = 5
        }
        constrain(buttons) { (btns) in
            for b in btns {
                b.height == 44
                b.left == c.left + .margin
                b.right == c.right - .margin
                b.top == m.bottom + .margin
                m = b
            }
            m.bottom == c.bottom - .margin
        }
        
        view.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(hide)))
    }
    
    func update(title: String? = nil, message: String? = nil) {
        if let t = title {
            titleLabel.text = t
        }
        if let message = message {
            messageLabel.text = message
        }
    }
    
    @objc fileprivate func handleAction(_ sender: ActionButton) {
        dismiss(animated: true, completion: sender.action.handler)
    }
    
    @objc func hide() {
        dismiss(animated: true, completion: nil)
    }
}


