//
//  MenuCell.swift
//  Lattis
//
//  Created by Ravil Khusainov on 28/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

class MenuCell: UITableViewCell {
    
    @IBOutlet weak var iconView: UIImageView!
    @IBOutlet weak var titleLabel: UILabel!
    static let identifier = "MenuCell"

    var action: MenuViewController.Action? {
        didSet {
            titleLabel.text = action?.name
            iconView.image = action?.image
        }
    }
    
    @IBAction func tapButton(_ sender: Any) {
        action?.action()
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        titleLabel.textColor = selected ? .lsTurquoiseBlue : .lsWarmGrey
    }
}
