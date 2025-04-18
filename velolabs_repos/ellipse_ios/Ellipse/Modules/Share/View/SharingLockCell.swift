//
//  SharingLockCell.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/8/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography

protocol SharingLockCellDelegate: class {
    func share(ellipse: Ellipse)
    func blink(ellipse: Ellipse)
    func unshare(ellipse: Ellipse)
}

class SharingLockCell: UITableViewCell {
    fileprivate let nameLabel = UILabel()
    fileprivate let titleLabel = UILabel()
    fileprivate let blinkButton = UIButton(type: .custom)
    fileprivate let shareButton = UIButton(type: .custom)
    weak var delegate: SharingLockCellDelegate?
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        selectionStyle = .none
        
        let containerView = UIView()
        contentView.addSubview(containerView)
        containerView.addSubview(nameLabel)
        containerView.addSubview(titleLabel)
        containerView.addSubview(blinkButton)
        containerView.addSubview(shareButton)
        
        titleLabel.font = .elTitle
        titleLabel.textColor = .elSteel
        nameLabel.font = .elRegular
        nameLabel.textColor = .elPinkishGrey
        nameLabel.numberOfLines = 2
        smallNegativeStyle(blinkButton)
        smallPositiveStyle(shareButton)
        blinkButton.setTitle("blink_this_ellipse".localized().lowercased().capitalized, for: .normal)
        shareButton.setTitle("share_now".localized().lowercased().capitalized, for: .normal)
        
        constrain(nameLabel, titleLabel, blinkButton, shareButton, contentView, containerView) { name, title, blink, share, view, container in
            container.centerY == view.centerY
            container.left == view.left + .margin
            container.right == view.right - .margin
            
            title.left == container.left
            title.right == container.right
            title.top == container.top
            
            blink.left == container.left
            share.right == container.right
            blink.top == title.bottom + .margin/2
            share.top == blink.top
            share.width == blink.width
            share.left == blink.right + .margin
            share.bottom == container.bottom
            
            name.centerY == blink.centerY
            name.left == blink.left
            name.right == blink.right
        }
        
        shareButton.addTarget(self, action: #selector(shareAction(_:)), for: .touchUpInside)
        blinkButton.addTarget(self, action: #selector(blink(_:)), for: .touchUpInside)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var ellipse: Ellipse.Shared! {
        didSet {
            titleLabel.text = ellipse.ellipse.name
            nameLabel.text = ellipse.ellipse.borrower?.fullNameWithPhone
            guard let ellipse = ellipse?.ellipse else { return }
            blinkButton.isHidden = ellipse.borrower != nil
            nameLabel.isHidden = !blinkButton.isHidden
            shareButton.setTitle(nameLabel.isHidden ? "share_now".localized().lowercased().capitalized : "share_stop".localized().lowercased().capitalized, for: .normal)
            shareButton.backgroundColor = nameLabel.isHidden ? .elDarkSkyBlue : .elSlate
        }
    }
    
    @objc fileprivate func shareAction(_ sender: Any) {
        guard let ellipse = ellipse?.ellipse else { return }
        if ellipse.borrower != nil {
            delegate?.unshare(ellipse: ellipse)
        } else {
            delegate?.share(ellipse: ellipse)
        }
    }
    
    @objc fileprivate func blink(_ sender: Any) {
        guard let ellipse = ellipse?.ellipse else { return }
        delegate?.blink(ellipse: ellipse)
    }
}
