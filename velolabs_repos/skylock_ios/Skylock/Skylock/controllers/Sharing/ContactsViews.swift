//
//  SharingContactViews.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 14/01/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import UIKit
import Contacts

class ContactsSectionView: UITableViewHeaderFooterView {
    let titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 16)
        label.textColor = .slCoolGreyTwo
        return label
    }()
    
    override init(reuseIdentifier: String?) {
        super.init(reuseIdentifier: reuseIdentifier)
        
        contentView.backgroundColor = .color(242, green: 242, blue: 242)
        contentView.addSubview(titleLabel)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.frame = contentView.bounds.insetBy(dx: 15, dy: 0)
    }
}

class EllipseContactCell: UITableViewCell {
    static let rowHeight: CGFloat = 65
    var contact: EllipseContact? {
        didSet {
            nameLabel.text = contact?.name
            phoneLabel.text = contact?.phoneNumber
        }
    }
    
    let nameLabel: UILabel = {
        let label = UILabel()
        label.textColor = .slCoolGreyTwo
        label.font = .systemFont(ofSize: 14)
        return label
    }()
    
    let phoneLabel: UILabel = {
        let label = UILabel()
        label.textColor = .slWhiteTwo
        label.font = .systemFont(ofSize: 14)
        return label
    }()
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        selectionStyle = .none
        contentView.backgroundColor = .white
        contentView.addSubview(nameLabel)
        contentView.addSubview(phoneLabel)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        var nameFrame = contentView.bounds.insetBy(dx: 15, dy: 0)
        nameFrame.size.height = nameLabel.font.lineHeight + phoneLabel.font.lineHeight
        nameFrame.origin.y = contentView.bounds.midY - nameFrame.height*0.5
        nameFrame.size.height = nameLabel.font.lineHeight
        nameLabel.frame = nameFrame
        
        nameFrame.origin.y = nameFrame.maxY
        nameFrame.size.height = phoneLabel.font.lineHeight
        phoneLabel.frame = nameFrame
    }
    
    override func setSelected(_ selected:Bool, animated:Bool){
        super.setSelected(selected, animated: animated)
        
        if (selected){
            contentView.backgroundColor = UIColor.color(87, green: 216, blue: 255)
            phoneLabel.textColor = .white
            nameLabel.textColor = .white
        } else {
            contentView.backgroundColor = UIColor.white
            phoneLabel.textColor = .slWhiteTwo
            nameLabel.textColor = .slCoolGreyTwo
        }
    }
}

class ChooseEllipseContactCell: EllipseContactCell {
    private let chooseImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage(named: "contacts_unselected_circle"), highlightedImage: UIImage(named: "contacts_selected_circle"))
        return imageView
    }()
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        contentView.addSubview(chooseImageView)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        chooseImageView.frame = {
            var frame = chooseImageView.frame
            frame.size = chooseImageView.image?.size ?? .zero
            frame.origin.x = 15
            frame.origin.y = (contentView.frame.height - frame.height)*0.5
            return frame
        }()
        
        nameLabel.frame = {
            var frame = nameLabel.frame
            frame.origin.x = chooseImageView.frame.maxX + 10
            frame.size.width = contentView.bounds.width - frame.minX - 15
            return frame
        }()
        
        phoneLabel.frame = {
            var frame = phoneLabel.frame
            frame.origin.x = nameLabel.frame.minX
            frame.size.width = nameLabel.frame.width
            return frame
        }()
    }
    
    var choosen: Bool = false {
        didSet {
            chooseImageView.isHighlighted = choosen
        }
    }
}
