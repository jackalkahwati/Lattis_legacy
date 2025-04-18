//
//  AlertController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 19/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

public final class AlertController: UIViewController {
    
    var actions: [ActionButton.Action] = []
    
    fileprivate let contentView = UIView()
    fileprivate let titleLabel = UILabel()
    fileprivate let messageLabel = UILabel()
    fileprivate let parser = SimpleHTMLParser()
    
    init(title: String?, message: Text?) {
        super.init(nibName: nil, bundle: nil)
        titleLabel.text = title
         
        messageLabel.font = .theme(weight: .medium, size: .body)
        if let message = message {
            switch message {
            case .plain(let t):
                messageLabel.text = t
            case .html(let t):
                messageLabel.attributedText = parser.parse(t)
            }
        }
        modalTransitionStyle = .crossDissolve
        modalPresentationStyle = .overCurrentContext
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = UIColor(white: 0.3, alpha: 0.8)
        view.addSubview(contentView)
        contentView.addSubview(titleLabel)
        contentView.addSubview(messageLabel)
        
        titleLabel.font = .theme(weight: .bold, size: .body)
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
            button.removeTarget(nil, action: nil, for: .allEvents)
            button.addTarget(self, action: #selector(self.handleAction(_:)), for: .touchUpInside)
        }
        constrain(buttons) { (btns) in
            for b in btns {
                b.left == c.left + .margin
                b.right == c.right - .margin
                b.top == m.bottom + .margin
                m = b
            }
            m.bottom == c.bottom - .margin
        }
    }
    
    func update(title: String? = nil, message: Text? = nil) {
        if let t = title {
            titleLabel.text = t
        }
        if let message = message {
            switch message {
            case .plain(let t):
                messageLabel.text = t
            case .html(let t):
                messageLabel.attributedText = parser.parse(t)
            }
        }
    }
    
    @objc fileprivate func handleAction(_ sender: ActionButton) {
        dismiss(animated: true, completion: sender.action.handler)
    }
}

extension AlertController {
    enum Text {
        case plain(String)
        case html(String)
    }
}

extension AlertController {
    static var bluetooth: AlertController {
        let alert = AlertController(title: "bluetooth_access_alert_title".localized(), message: .plain("bluetooth_access_alert_message".localized()))
        alert.actions = [.ok]
        return alert
    }
    
    static var location: AlertController {
        let alert = AlertController(title: nil, message: .plain("location_access_hint".localized()))
        alert.actions = [.ok]
        return alert
    }
    
    static func cardRequired(completion: @escaping () -> ()) -> AlertController {
        let alert = AlertController(title: "general_error_title".localized(), message: .plain("credit_card_required".localized()))
        alert.actions = [
            .plain(title: "update_payment_details".localized(), handler: completion),
            .cancel
        ]
        return alert
    }
}

public extension AlertController {
    convenience init(title: String?, body: String?, handler: @escaping () -> Void = {}) {
        self.init(title: title, message: .plain(body ?? ""))
        actions = [.ok(handler: handler)]
    }
}


