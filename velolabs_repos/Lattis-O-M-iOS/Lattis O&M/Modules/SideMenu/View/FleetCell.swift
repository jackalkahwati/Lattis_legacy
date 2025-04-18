//
//  FleetCell.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 05/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class FleetCell: UITableViewCell {
    
    var fleet: Fleet? {
        didSet {
            textLabel?.text = fleet?.name?.capitalized
            if let isCurrent = fleet?.isCurrent {
                textLabel?.textColor = isCurrent ? .lsTurquoiseBlue : .lsCoolGrey
                textLabel?.font = isCurrent ? UIFont.systemFont(ofSize: 14, weight: UIFont.Weight.bold) : UIFont.systemFont(ofSize: 14)
            }
        }
    }
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        textLabel?.font = UIFont.systemFont(ofSize: 14)
        selectionStyle = .none
        backgroundColor = .clear
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        
        textLabel?.frame = {
            var frame = contentView.bounds
            frame.origin.x = separatorInset.left
            frame.size.width -= separatorInset.left + separatorInset.right
            return frame
        }()
    }
}

class FleetSectionHeader: UIView {
    fileprivate let titleLabel: UILabel = {
        let label = UILabel()
        label.font = UIFont.systemFont(ofSize: 15, weight: UIFont.Weight.bold)
        label.textColor = .white
        label.text = "FLEETS"
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        addSubview(titleLabel)
        backgroundColor = .lsTurquoiseBlue2
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        titleLabel.frame = {
            var frame = bounds
            frame.origin.x = 20
            frame.size.width -= 36
            return frame
        }()
    }
}
