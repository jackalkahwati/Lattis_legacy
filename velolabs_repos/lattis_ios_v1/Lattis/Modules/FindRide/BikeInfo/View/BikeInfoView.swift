//
//  BikeInfoView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 13/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

class BikeInfoView: UIView {
    @IBOutlet weak var reserveButton: UIButton!
    @IBOutlet weak var tableView: UITableView!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        tableView.register(BikeInfoSectionHeader.self, forHeaderFooterViewReuseIdentifier: BikeInfoSectionHeader.identifier)
        tableView.register(BikeInfoSectionFooter.self, forHeaderFooterViewReuseIdentifier: BikeInfoSectionFooter.identifier)
        
        tableView.register(CreditCardCell.self, forCellReuseIdentifier: "card")
        tableView.register(AddCardCell.self, forCellReuseIdentifier: "add")
        tableView.register(EmptyCardCell.self, forCellReuseIdentifier: "empty")
        
        tableView.tableFooterView = UIView(frame: CGRect(x: 0, y: 0, width: 0, height: 55))
        tableView.estimatedRowHeight = 44
        tableView.rowHeight = UITableView.automaticDimension
        tableView.contentInset = .zero
    }
}

class BikeInfoSectionHeader: UITableViewHeaderFooterView {
    static let identifier = "header"
    
    let titleLabel: UILabel = {
        let label = UILabel()
        label.font = UIFont(.circularBold, size: 18)
        label.textColor = .lsWarmGreyFour
        label.numberOfLines = 0
        return label
    }()
    
    let subtitleLabel: UILabel = {
        let label = UILabel()
        label.font = UIFont.systemFont(ofSize: 14)
        label.textColor = .lsWarmGrey
        label.textAlignment = .right
        return label
    }()
    
    private let line = UIView()
    
    override init(reuseIdentifier: String?) {
        super.init(reuseIdentifier: reuseIdentifier)
        backgroundView = UIView()
        
        contentView.backgroundColor = .white
        contentView.addSubview(titleLabel)
        
        contentView.addSubview(subtitleLabel)
        
        line.backgroundColor = UIColor(white: 226.0/255.0, alpha: 1)
        contentView.addSubview(line)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        titleLabel.frame = contentView.bounds.insetBy(dx: 16, dy: 0)
        subtitleLabel.frame = {
            var frame = titleLabel.frame
            frame.size.height = subtitleLabel.font.lineHeight
            frame.origin.y = contentView.bounds.height - frame.height - 16
            return frame
        }()
        line.frame = {
            var frame = contentView.bounds.insetBy(dx: 16, dy: 0)
            frame.size.height = 1.0/UIScreen.main.scale
            frame.origin.y = contentView.bounds.maxY - frame.height
            return frame
        }()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

class BikeInfoSectionFooter: UITableViewHeaderFooterView {
    static let identifier = "footer"
    private let shadowView = UIView()
    override init(reuseIdentifier: String?) {
        super.init(reuseIdentifier: reuseIdentifier)
        
        backgroundView = UIView()
        backgroundView?.backgroundColor = UIColor(white: 226.0/255.0, alpha: 1)
        
        contentView.addSubview(shadowView)
        contentView.backgroundColor = .clear
        shadowView.backgroundColor = .white
        shadowView.layer.shadowColor = UIColor.black.cgColor
        shadowView.layer.shadowOpacity = 0.2
        shadowView.layer.shadowRadius = 3
        shadowView.layer.shadowOffset = CGSize(width: 0, height: 3)
        
        backgroundView = UIView()
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        contentView.frame = bounds
        shadowView.frame = {
            var frame = bounds
            frame.size.height = 1
            return frame
        }()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
