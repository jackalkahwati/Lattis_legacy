//
//  OnboardingEllipseCell.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/16/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography

protocol OnboardingEllipseCellDelegate: class {
    func blink(device: Ellipse.Device)
    func connect(device: Ellipse.Device)
}

class OnboardingEllipseCell: UITableViewCell {
    
    weak var delegate: OnboardingEllipseCellDelegate?
    var device: Ellipse.Device? {
        didSet {
            titleLabel.text = device?.peripheral.name
        }
    }
    
    fileprivate let containerView = UIView()
    fileprivate let titleLabel = UILabel()
    fileprivate let ledButton = UIButton(type: .custom)
    fileprivate let connectButton = UIButton(type: .custom)
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        selectionStyle = .none
        
        contentView.addSubview(containerView)
        containerView.addSubview(titleLabel)
        containerView.addSubview(ledButton)
        containerView.addSubview(connectButton)
        
        titleLabel.font = .elTitle
        titleLabel.textColor = .elSteel
        smallNegativeStyle(ledButton)
        smallPositiveStyle(connectButton)
        ledButton.setTitle("blink_this_ellipse".localized(), for: .normal)
        connectButton.setTitle("connect".localized().lowercased().capitalized, for: .normal)
        
        constrain(containerView, titleLabel, ledButton, connectButton, contentView) { (container, title, led, connect, view) in
            container.centerY == view.centerY
            container.left == view.left + .margin
            container.right == view.right - .margin
            
            title.left == container.left
            title.right == container.right
            title.top == container.top
            
            led.left == container.left
            connect.right == container.right
            led.top == title.bottom + .margin/2
            connect.top == led.top
            connect.width == led.width
            connect.left == led.right + .margin
            connect.bottom == container.bottom
        }
        
        ledButton.addTarget(self, action: #selector(blink(_:)), for: .touchUpInside)
        connectButton.addTarget(self, action: #selector(connect(_:)), for: .touchUpInside)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc fileprivate func connect(_ sender: Any) {
        guard let device = device else { return }
        delegate?.connect(device: device)
    }
    
    @objc fileprivate func blink(_ sender: Any) {
        guard let device = device else { return }
        delegate?.blink(device: device)
    }
}
