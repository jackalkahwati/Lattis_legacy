//
//  RideHistoryListCell.swift
//  Lattis
//
//  Created by Ravil Khusainov on 8/18/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

class RideHistoryListCell: UITableViewCell {
    @IBOutlet weak var priceLabel: UILabel!
    @IBOutlet weak var fleetLabel: UILabel!
    @IBOutlet weak var durationLabel: UILabel!
    @IBOutlet weak var dateLabel: UILabel!
    
    fileprivate let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter
    }()
    
    var trip: Trip? {
        didSet {
            priceLabel.text = trip?.fleetType == .publicPay || trip?.fleetType == .privatePay ? defaultValue(trip?.total?.priceValue(trip!.currency), 0.0.priceValue(trip!.currency)) : "payment_cost_free".localized()
            fleetLabel.text = defaultValue(trip?.fleetName)
            durationLabel.text = defaultValue(trip?.duration.time)
            let dateText = trip?.finishedAt != nil ? dateFormatter.string(from: trip!.finishedAt!) : nil
            dateLabel.text = defaultValue(dateText)
        }
    }
}

public func defaultValue(_ string: String?, _ defaultValue: String? = nil) -> String {
    if let value = string, value.isEmpty == false {
        return value
    }
    if let def = defaultValue {
        return def
    }
    return "general_no_info".localized()
}
