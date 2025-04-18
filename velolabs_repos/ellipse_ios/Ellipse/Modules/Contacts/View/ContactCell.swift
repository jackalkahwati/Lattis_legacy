//
//  ContactCell.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/8/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class ContactCell: UITableViewCell {
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var phoneLabel: UILabel!
    @IBOutlet weak var selectView: UIView?
    
    var contact: Contact? {
        didSet {
            nameLabel.text = contact?.fullName
            phoneLabel.text = contact?.phoneNumbers.joined(separator: ", ")
        }
    }
    
    var isChecked: Bool = false {
        didSet {
            selectView?.backgroundColor = isChecked ? .elDarkSkyBlue : .white
        }
    }
}
