//
//  TicketCell.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 12/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class TicketCell: UITableViewCell {
    @IBOutlet weak var readenView: UIView!
    @IBOutlet weak var subtitleLabel: UILabel!
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var iconView: UIImageView!

    var ticket: Ticket? {
        didSet {
            titleLabel.text = ticket?.displayTitle
            subtitleLabel.text = ticket?.category?.displayTitle
            iconView.image = ticket?.icon
            readenView.isHidden = (ticket?.isNew ?? false) == false
        }
    }
}

extension Ticket {    
    var icon: UIImage {
        switch category {
        case .parking_outside_geofence?:
            return #imageLiteral(resourceName: "icon_ticket_out_of_zone")
        default:
            return #imageLiteral(resourceName: "icon_ticket_service_due")
        }
    }
}
