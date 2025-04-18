//
//  MessageOverlayView.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 29.06.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

struct LockSwitchInfo {
    let target: AnyHashable
    let selector: Selector
    let security: Device.Security
}

final class MessageOverlayView: UIView {
    let label = UILabel.label(font: .theme(weight: .medium, size: .body), allignment: .center, lines: 0)
    fileprivate let cardView = UIView()
    fileprivate let lockSwitch = SecuritySwitch()
    fileprivate let hintLabel = UILabel.label(text: "connecting_loader".localized(), font: .theme(weight: .book, size: .small), allignment: .center)
    
    init(_ message: String, info: LockSwitchInfo?) {
        super.init(frame: .zero)
        
        backgroundColor = UIColor(white: 0.3, alpha: 0.8)
        label.text = message
        addSubview(cardView)
        cardView.backgroundColor = .white
        
        let lockContainer = UIView()
        lockContainer.addSubview(lockSwitch)
        
        constrain(lockContainer, lockSwitch) { container, lock in
            lock.centerX == container.centerX
            lock.top == container.top
            lock.bottom == container.bottom
        }
        
        let stackView = UIStackView(arrangedSubviews: [label, lockContainer, hintLabel])
        stackView.axis = .vertical
        stackView.spacing = .margin
        stackView.distribution = .fill
        stackView.contentMode = .center
        cardView.addSubview(stackView)
        
        cardView.addShadow()
        cardView.layer.cornerRadius = .containerCornerRadius
        
        constrain(stackView, cardView, self) { stack, card, view in
            stack.edges == card.edges.inseted(by: .margin)
            card.centerY == view.centerY
            card.left == view.left + .margin
            card.right == view.right - .margin
        }
        update(info: info)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func show(parrent: UIView? = nil) {
        guard let parentView = parrent ?? UIApplication.shared.keyWindow else { return }
        alpha = 0
        parentView.addSubview(self)
        translatesAutoresizingMaskIntoConstraints = false
        constrain(self, parentView) { $0.edges == $1.edges }
        UIView.animate(withDuration: 0.3) {
            self.alpha = 1
        }
    }
    
    func hide(completion: (() -> ())? = nil) {
        UIView.animate(withDuration: 0.3, animations: {
            self.alpha = 0
        }, completion: { _ in
            completion?()
            self.removeFromSuperview()
        })
    }
    
    func update(info: LockSwitchInfo?) {
        if let i = info {
            if !lockSwitch.allTargets.contains(i.target) {
                lockSwitch.addTarget(i.target, action: i.selector, for: .touchUpInside)
            }
            lockSwitch.security = i.security
            lockSwitch.superview?.isHidden = false
            hintLabel.isHidden = i.security != .progress
        } else {
            lockSwitch.superview?.isHidden = true
            hintLabel.isHidden = true
        }
    }
}
