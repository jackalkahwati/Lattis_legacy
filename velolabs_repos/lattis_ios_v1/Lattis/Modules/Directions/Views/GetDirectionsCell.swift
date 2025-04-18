//
//  GetDirectionsCell.swift
//  Lattis
//
//  Created by Ravil Khusainov on 24/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

class GetDirectionsCell: UITableViewCell {
    @IBOutlet weak var addressLabel: UILabel!
    @IBOutlet weak var nameLabel: UILabel!
    
    
    var direction: Direction? {
        didSet {
            addressLabel.text = direction?.address
            nameLabel.text = direction?.name
        }
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        contentView.backgroundColor = selected ? UIColor.lsTurquoiseBlue.withAlphaComponent(0.19) : .white
    }
}

class GetDirectionsSectionView: UIView {
    private let titleLabel = UILabel()
    
    init(title: String) {
        super.init(frame: .zero)
        backgroundColor = .white
        titleLabel.text = title
        addSubview(titleLabel)
        
        titleLabel.font = UIFont(.circularBook, size: 16)
        titleLabel.textColor = .lsSteel
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.frame = bounds.insetBy(dx: 16, dy: 0)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
