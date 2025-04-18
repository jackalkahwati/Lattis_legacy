//
//  ReservationCell.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 14.08.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Model

class ReservationCell: UITableViewCell {
    
    fileprivate var bikeItem: BikeItem!
    fileprivate let dateLabel = UILabel.label(font: .theme(weight: .medium, size: .small))
    fileprivate let timeLabel = UILabel.label(font: .theme(weight: .medium, size: .small), allignment: .right)
    
    func update(reservation: Reservation, with dateFormatter: DateFormatter, and timeFormatter: DateFormatter) {
        if let item = bikeItem {
            item.bike = reservation.bike
        } else {
            addItem(with: reservation.bike)
        }
        
        if reservation.inAsingleDay {
            dateLabel.text = dateFormatter.string(from: reservation.reservationStart)
            timeLabel.text = timeFormatter.string(from: reservation.reservationStart) + " - " + timeFormatter.string(from: reservation.reservationEnd)
        } else {
            dateLabel.text = dateFormatter.string(from: reservation.reservationStart) + " " + timeFormatter.string(from: reservation.reservationStart)
            timeLabel.text = dateFormatter.string(from: reservation.reservationEnd) + " " + timeFormatter.string(from: reservation.reservationEnd)
        }
    }
    
    fileprivate func addItem(with bike: Model.Bike) {
        let item = BikeItem(bike: bike)
        bikeItem = item
        
        let stackView = UIStackView.tuple(dateLabel, timeLabel)
        
        contentView.addSubview(stackView)
        contentView.addSubview(item)
        
        constrain(item, stackView, contentView) { item, stack, content in
            item.top == content.top + .margin
            item.left == content.left + .margin
            item.right == content.right - .margin
            
            item.bottom == stack.top - .margin/2
            stack.left == content.left + .margin
            stack.right == content.right - .margin
            stack.bottom == content.bottom - .margin
        }
    }
}
