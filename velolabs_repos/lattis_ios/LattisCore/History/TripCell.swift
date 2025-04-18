//
//  TripCell.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 08/08/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Cartography

class TripCell: UITableViewCell {
    
    fileprivate let fleetLabel = UILabel.label(font: .theme(weight: .book, size: .text))
    fileprivate let timeLabel = UILabel.label(font: .theme(weight: .medium, size: .small))
    fileprivate let priceLabel = UILabel.label(font: .theme(weight: .book, size: .giant))
    fileprivate let durationLabel = UILabel.label(font: .theme(weight: .book, size: .body))
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        contentView.addSubview(fleetLabel)
        contentView.addSubview(timeLabel)
        contentView.addSubview(priceLabel)
        contentView.addSubview(durationLabel)
                
        constrain(fleetLabel, timeLabel, priceLabel, durationLabel, contentView) { fleet, time, price, duration, content in
            
            fleet.right == content.right - .margin
            fleet.top == content.top + .margin/2
            
            time.top == fleet.top
            time.left == content.left + .margin
            
            duration.top >= time.top + .margin/2
            duration.left == content.left + .margin
            duration.bottom == content.bottom - .margin/2
            
            price.right == content.right - .margin
            price.bottom == duration.bottom
            price.top == fleet.bottom + .margin/4
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    fileprivate var dateFormatter: DateFormatter!
    fileprivate var durationFormatter: DateComponentsFormatter!
    fileprivate var trip: Trip! {
        didSet {
            fleetLabel.text = trip.fleetName
            timeLabel.text = dateFormatter.string(from: trip.startedAt)
            priceLabel.text = trip.price(for: .total)
            durationLabel.text = durationFormatter.string(from: trip.duration)
        }
    }
    
    func update(trip: Trip, dateFormatter: DateFormatter, durationFormatter: DateComponentsFormatter) {
        self.dateFormatter = dateFormatter
        self.durationFormatter = durationFormatter
        self.trip = trip
    }
}
