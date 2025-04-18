//
//  SLSharigTableViewSharedCell.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 16/01/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import UIKit

class SLSharigTableViewSharedCell: SLSharigTableViewCell {
    
    private let contactImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage(named: "contacts_icon"))
        imageView.contentMode = .scaleAspectFit
        imageView.layer.cornerRadius = 3
        imageView.clipsToBounds = true
        return imageView
    }()
    
    private let unshareButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setTitle("STOP SHARING".localized(), for: .normal)
        button.layer.cornerRadius = 3
        button.backgroundColor = .slDenim
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 12)
        return button
    }()
    
    private let contactNameLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 15)
        label.textColor = .slRobinsEgg
        return label
    }()
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        contentView.addSubview(contactImageView)
        contentView.addSubview(contactNameLabel)
        contentView.addSubview(unshareButton)
        unshareButton.addTarget(self, action: #selector(unshare), for: .touchUpInside)
        
        shareButton.setTitle("SHARE WITH ANOTHER FRIEND".localized(), for: .normal)
        shareButton.titleLabel?.font = .systemFont(ofSize: 13)
        
        subtitleLabel.text = "Shared with".localized()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        contactImageView.frame = {
            var frame = CGRect(x: 0, y: 0, width: 78, height: 78)
            frame.origin.y = 63
            frame.origin.x = inset
            return frame
        }()
        
        subtitleLabel.frame = {
            var frame = subtitleLabel.frame
            frame.origin.y = contactImageView.frame.minY
            frame.origin.x = contactImageView.frame.maxX + 18
            frame.size.width = contentView.frame.width - frame.minX - inset
            return frame
        }()
        
        contactNameLabel.frame = {
            var frame = subtitleLabel.frame
            frame.size.height = contactNameLabel.font.lineHeight
            frame.origin.y = subtitleLabel.frame.maxY + 3
            return frame
        }()
        
        unshareButton.frame = {
            var frame = subtitleLabel.frame
            frame.size.height = 34
            frame.size.width -= 60
            frame.origin.y = contactImageView.frame.maxY - frame.height
            return frame
        }()
    }
    
    override func refreshUI() {
        super.refreshUI()
        contactImageView.image = UIImage(named: "contacts_icon")
        contactNameLabel.text = lock?.borrower?.fullName ?? lock?.borrower?.usersId
        guard let usersId = lock?.borrower?.usersId,
            let contact = delegate?.contact(forUser: usersId) else { return }
        contactNameLabel.text = contact.fullName
        if let data = contact.thumbnailImageData {
            contactImageView.image = UIImage(data: data)
        }
    }
    
    @objc private func unshare() {
        guard let lock = lock else { return print("SLSharigTableViewSharedCell: cant find lock") }
        delegate?.unshare(lock: lock)
    }
    
}
