//
//  CreditCardCell.swift
//  Lattis
//
//  Created by Ravil Khusainov on 6/30/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

enum CreditCardCellType: TableCellPresentable {
    case add
    case empty
    case card(CreditCard)
    
    var identifire: String {
        switch self {
        case .add: return "add"
        case .empty: return "empty"
        case .card(_): return "card"
        }
    }
    
    var rowHeight: CGFloat { return 44 }
}

class EmptyCardCell: UITableViewCell {
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        textLabel?.font = UIFont.systemFont(ofSize: 14)
        textLabel?.textColor = .lsCoolGreyTwo
        selectionStyle = .none
        textLabel?.numberOfLines = 0
        textLabel?.text = "payment_empty_text".localized()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

class CreditCardCell: UITableViewCell {
    fileprivate let selectView: UIImageView = {
        let view = UIImageView(image: #imageLiteral(resourceName: "icon_select"))
        view.contentMode = .right
        return view
    }()
    
    fileprivate let subtitleLabel: UILabel = {
        let label = UILabel()
        label.font = UIFont.systemFont(ofSize: 14)
        label.textColor = .lsCoolGreyTwo
        label.textAlignment = .right
        return label
    }()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        textLabel?.font = UIFont.systemFont(ofSize: 14)
        textLabel?.textColor = .lsCoolGreyTwo
        selectionStyle = .none
        
        contentView.addSubview(selectView)
        contentView.addSubview(subtitleLabel)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        selectView.frame = {
            var frame = contentView.bounds
            frame.size.width = 20
            frame.origin.x = contentView.bounds.width - frame.width - 15
            return frame
        }()
        
        subtitleLabel.frame = {
            var frame = textLabel!.frame
            if accessory == .select {
                frame.size.width -= (selectView.frame.width + 20)
            }
            return frame
        }()
    }
    
    var card: CreditCard! {
        didSet {
            textLabel?.text = card.number == nil ? nil : String(repeating: "•", count: 4) + " " + card.number!.substring(fromReverse: 4)
            imageView?.image = card.cardType?.icon
            selectView.isHidden = accessory != .select || card.isCurrent == false
            subtitleLabel.text = card.expire
        }
    }
    
    var accessory: Accessory = .none {
        didSet {
            accessoryType = .none
//            if accessory == .disclosure {
//                accessoryType = .disclosureIndicator
//            } else {
//                accessoryType = .none
//            }
        }
    }
    
    enum Accessory {
        case none
        case disclosure
        case select
    }
}

class AddCardCell: UITableViewCell {
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        textLabel?.font = UIFont.systemFont(ofSize: 14)
        textLabel?.textColor = .lsTurquoiseBlue
        textLabel?.text = "add_credit_card".localized()
        
        selectionStyle = .none
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
