//
//  AlertView.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/24/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography

class AlertView: UIView, TopPresentable {
    
    let titleLabel = UILabel()
    let contentView = UIView()
    fileprivate let blurView = UIVisualEffectView(effect: UIBlurEffect(style: .dark))
    fileprivate let bubbleView = UIView()
    
    class func alert(title: String? = nil, text: String? = nil, actions: [Action] = [.ok]) -> AlertView {
        let alert = AlertView(frame: .zero)
        alert.titleLabel.text = title
        alert.configure(body: text, actions: actions)
        return alert
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        bubbleView.backgroundColor = .white
        addSubview(blurView)
        addSubview(bubbleView)
        bubbleView.addSubview(titleLabel)
        bubbleView.addSubview(contentView)
        bubbleView.layer.cornerRadius = 12
        titleLabel.font = .elTitle
        titleLabel.textAlignment = .center
        titleLabel.numberOfLines = 0
        titleLabel.textColor = .elGunmetal
        
        constrain(bubbleView, titleLabel, contentView, self, blurView) { bubble, title, content, view, blur in
            bubble.left == view.left + .margin*1.5
            bubble.right == view.right - .margin*1.5
            bubble.centerY == view.centerY
            
            title.top == bubble.top + .margin/2
            title.left == bubble.left + .margin/2
            title.right == bubble.right - .margin/2
            
            content.top == title.bottom + .margin/2
            content.left == title.left
            content.right == title.right
            content.bottom == bubble.bottom - .margin
            
            blur.edges == view.edges
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func configure(view: UIView, actions: [Action] = []) {
        contentView.addSubview(view)
        var bottom: Edge?
        constrain(view, contentView) { v, c in
            v.left == c.left
            v.right == c.right
            v.top == c.top
            if actions.isEmpty {
                v.bottom == c.bottom
            } else {
                bottom = v.bottom
            }
        }
        if let b = bottom {
            configure(actions: actions, bottomEdge: b)
        }
    }
    
    func configure(body: String?, actions: [Action]) {
        let label = UILabel()
        label.text = body
        label.textAlignment = .center
        label.font = .elRegular
        label.textColor = .elSlateGreyTwo
        label.numberOfLines = 0
        
        contentView.addSubview(label)
        
        var bottom: Edge!
        constrain(label, contentView) { lbl, content in
            lbl.left == content.left
            lbl.right == content.right
            lbl.top == content.top
            bottom = lbl.bottom
        }
        
        configure(actions: actions, bottomEdge: bottom)
    }
    
    fileprivate func configure(actions: [Action], bottomEdge: Edge) {
        guard !actions.isEmpty else {
            constrain(contentView) { content in
                bottomEdge == content.bottom
            }
            return
        }
        let buttons = actions.map(AlertViewButton.init)
        buttons.forEach{ button in
            self.contentView.addSubview(button)
            button.addTarget(self, action: #selector(action(_:)), for: .touchUpInside)
        }
        
        var bottom = bottomEdge
        constrain(buttons) { (btns) in
            btns.forEach({ (button) in
                button.top == bottom + .margin
                button.left == button.superview!.left + .margin*2
                button.right == button.superview!.right - .margin*2
                bottom = button.bottom
            })
            bottom == btns.last!.superview!.bottom
        }
    }
    
    @objc fileprivate func action(_ sender: AlertViewButton) {
        hide {
            sender.action.handler?(sender.action)
        }
    }
}


extension AlertView {
    struct Action {
        let title: String
        let style: Style
        let handler: ((Action) -> ())?
        
        init(title: String, style: Style = .default, handler: ((Action) -> ())? = nil) {
            self.title = title
            self.style = style
            self.handler = handler
        }
        
        enum Style {
            case `default`, cancel
        }
        
        static var ok: Action {
            return Action(title: "ok".localized().lowercased().capitalized)
        }
        
        static func ok(handler: @escaping () -> () = {}) -> Action {
            return Action(title: "ok".localized().lowercased().capitalized) { _ in handler()}
        }
        
        static var cancel: Action {
            return Action(title: "cancel".localized().lowercased().capitalized, style: .cancel, handler: nil)
        }
        
        static func yes(handler: @escaping () -> () = {}) -> Action {
            return Action(title: "yes".localized().lowercased().capitalized) { _ in handler()}
        }
        
        static var no: Action {
            return Action(title: "no".localized().lowercased().capitalized, style: .cancel)
        }
    }
}

class AlertViewButton: UIButton {
    let action: AlertView.Action
    
    init(action: AlertView.Action) {
        self.action = action
        super.init(frame: .zero)
        switch action.style {
        case .cancel:
            smallNegativeStyle(self)
        default:
            smallPositiveStyle(self)
        }
        setTitle(action.title, for: .normal)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

extension AlertView {
    class func theft(completion: @escaping () -> ()) -> AlertView {
        let alert = AlertView()
        alert.titleLabel.text = "action_label_theft_description2".localized()
        alert.configure(body: "action_label_theft_description1".localized(), actions: [
            .init(title: "locate_my_bike".localized().lowercased().capitalized, handler: { (_) in
                completion()
            }),
            .init(title: "ok".localized().lowercased().capitalized, style: .cancel)
            ])
        return alert
    }
}
