//
//  RideHistorySummaryCell.swift
//  Lattis
//
//  Created by Ravil Khusainov on 8/22/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import Mapbox

class RideHistorySummaryCell: RideHistoryCell {
    @IBOutlet weak var tripView: TripView!
    @IBOutlet weak var mapImageView: UIImageView!
    @IBOutlet weak var dropOffLabel: UILabel!
    @IBOutlet weak var pickUpLabel: UILabel!

    override var trip: Trip? {
        didSet {
            dropOffLabel.text = defaultValue(trip?.endAddress)
            pickUpLabel.text = defaultValue(trip?.startAddress)
            tripView?.trip = trip
        }
    }
}

