//
//  SLSharigTableViewCell.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 13/01/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import UIKit
import Localize_Swift
import Contacts

protocol SLSharigTableViewCellDelegate: class {
    func share(lock: SLLock)
    func unshare(lock: SLLock)
    func contact(forUser userId: String) -> CNContact?
}

class SLSharigTableViewCell: UITableViewCell {
    
    weak var delegate: SLSharigTableViewCellDelegate?
    var lock: SLLock? { didSet { refreshUI() } }
    internal let inset: CGFloat = 22
    
    internal let subtitleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14)
        label.textColor = .slCoolGrey
        label.text = "This Ellipse isn’t shared to anyone".localized()
        return label
    }()
    
    internal let shareButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setTitle("SHARE NOW".localized(), for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 15)
        button.setTitleColor(.white, for: .normal)
        button.backgroundColor = .slRobinsEgg
        button.layer.cornerRadius = 3
        return button
    }()
    
    private let titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 17)
        label.textColor = .slWarmGreyTwo
        return label
    }()
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        backgroundColor = .slWhite
        contentView.backgroundColor = .white
        selectionStyle = .none
        contentView.layer.borderColor = UIColor.slPinkishGrey.cgColor
        contentView.layer.borderWidth = 1.0/UIScreen.main.scale
        
        contentView.addSubview(titleLabel)
        contentView.addSubview(subtitleLabel)
        contentView.addSubview(shareButton)
        
        shareButton.addTarget(self, action: #selector(share), for: .touchUpInside)
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        
        contentView.frame = {
            var frame = bounds
            frame.origin.y = 26
            frame.size.height -= frame.minY
            return frame
        }()
        
        titleLabel.frame = {
            var frame = contentView.bounds.insetBy(dx: inset, dy: inset)
            frame.size.height = titleLabel.font.lineHeight
            return frame
        }()
        
        shareButton.frame = {
            var frame = titleLabel.frame
            frame.size.height = 50
            frame.origin.y = contentView.frame.height - frame.height - inset
            return frame
        }()
        
        subtitleLabel.frame = {
            var frame = titleLabel.frame
            frame.size.height = subtitleLabel.font.lineHeight
            frame.origin.y = titleLabel.frame.maxY + 15
            return frame
        }()
    }
    
    internal func refreshUI() {
        titleLabel.text = lock?.displayName
    }
    
    @objc private func share() {
        guard let lock = lock else { return print("SLSharigTableViewCell: cant find lock") }
        delegate?.share(lock: lock)
    }
}
