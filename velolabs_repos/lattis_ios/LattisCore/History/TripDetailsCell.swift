//
//  TripDetailsCell.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 23/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Kingfisher

class TripDetailsCell: UITableViewCell {
    var trip: Trip!
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        selectionStyle = .none
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

class TripDetailsDateCell: TripDetailsCell {
    
    fileprivate let titleLabel = UILabel()
    fileprivate let dateFormatter = DateFormatter()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        dateFormatter.dateStyle = .full
        dateFormatter.timeStyle = .none
        
        contentView.addSubview(titleLabel)
        titleLabel.numberOfLines = 2
        titleLabel.font = .theme(weight: .bold, size: .title)
        titleLabel.textColor = .gray
        titleLabel.textAlignment = .center
        
        constrain(titleLabel, contentView) { title, content in
            title.edges == content.edges.inseted(by: .margin)
        }
    }
    
    override var trip: Trip! {
        didSet {
            titleLabel.text = dateFormatter.string(from: trip.startedAt)
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

class TripDetailsPriceCell: TripDetailsCell {
    
    fileprivate let titleLabel = UILabel()
    fileprivate let priceLabel = UILabel()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        contentView.addSubview(titleLabel)
        contentView.addSubview(priceLabel)
        
        titleLabel.font = .theme(weight: .medium, size: .body)
        titleLabel.textColor = .gray
        
        priceLabel.font = .theme(weight: .bold, size: .body)
        priceLabel.textColor = .darkGray
        
        constrain(titleLabel, priceLabel, contentView) { title, price, content in
            title.left == content.left + .margin
            title.top == content.top + .margin
            title.bottom == content.bottom - .margin
            
            price.centerY == title.centerY
            price.right == content.right - .margin
        }
        
    }
    
    override var trip: Trip! {
        didSet {
            guard let p = Trip.Price(rawValue: reuseIdentifier!) else { return }
            priceLabel.text = trip.price(for: p)
            titleLabel.text = p.rawValue
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

class TripDetailsAddressCell: TripDetailsCell {
    
    fileprivate let startTimeLabel = UILabel()
    fileprivate let startAddressLabel = UILabel()
    fileprivate let durationLabel = UILabel()
    fileprivate let endAddressLabel = UILabel()
    fileprivate let endTimeLabel = UILabel()
    
    fileprivate let timeFormatter = DateFormatter()
    fileprivate let durationFormatter = DateComponentsFormatter()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        timeFormatter.timeStyle = .short
        timeFormatter.dateStyle = .none
        
        durationFormatter.allowedUnits = [.day, .hour, .minute]
        durationFormatter.unitsStyle = .short
        
        contentView.addSubview(startTimeLabel)
        contentView.addSubview(startAddressLabel)
        contentView.addSubview(durationLabel)
        contentView.addSubview(endAddressLabel)
        contentView.addSubview(endTimeLabel)
        
        startTimeLabel.font = .theme(weight: .medium, size: .small)
        startTimeLabel.textColor = .lightGray
        
        startAddressLabel.font = .theme(weight: .medium, size: .body)
        startAddressLabel.textColor = .gray
        startAddressLabel.numberOfLines = 3
        
        durationLabel.font = .theme(weight: .medium, size: .body)
        durationLabel.textColor = .lightGray
        
        endTimeLabel.font = .theme(weight: .medium, size: .small)
        endTimeLabel.textColor = .lightGray
        
        endAddressLabel.font = .theme(weight: .medium, size: .body)
        endAddressLabel.textColor = .gray
        endAddressLabel.numberOfLines = 3
        
        let topLineView = UIView()
        contentView.addSubview(topLineView)
        topLineView.backgroundColor = .neonBlue
        topLineView.layer.cornerRadius = 2
        
        let bottomLineView = UIView()
        contentView.addSubview(bottomLineView)
        bottomLineView.backgroundColor = .neonBlue
        bottomLineView.layer.cornerRadius = 2
        
        let topDotView = UIView()
        topDotView.backgroundColor = .white
        topDotView.layer.cornerRadius = 6
        topDotView.layer.borderColor = UIColor.neonBlue.cgColor
        topDotView.layer.borderWidth = 3
        contentView.addSubview(topDotView)
        
        let bottomDotView = UIView()
        bottomDotView.backgroundColor = .neonBlue
        bottomDotView.layer.cornerRadius = 6
        bottomDotView.layer.borderColor = UIColor.neonBlue.cgColor
        bottomDotView.layer.borderWidth = 3
        contentView.addSubview(bottomDotView)
        
        constrain(startTimeLabel, startAddressLabel, durationLabel, endAddressLabel, endTimeLabel, topLineView, bottomLineView, topDotView, bottomDotView, contentView) { startTime, startAddress, duration, endAddress, endTime, topLine, bottomLine, topDot, bottomDot, content in
            
            startTime.top == content.top + .margin
            startTime.left == content.left + .margin
            startTime.right == content.right - .margin
            
            topDot.left == content.left + .margin
            topDot.centerY == startAddress.centerY
            topDot.width == 12
            topDot.height == topDot.width
            topLine.centerX == topDot.centerX
            topLine.width == 4
            topLine.top == startAddress.centerY
            
            startAddress.top == startTime.bottom
            startAddress.left == topDot.right + .margin/2
            startAddress.right == startTime.right
            
            duration.top == startAddress.bottom + .margin
            duration.left == content.left + .margin
            duration.right == startTime.right
            topLine.bottom == duration.top
            
            bottomLine.top == duration.bottom
            bottomLine.left == topLine.left
            bottomLine.width == topLine.width
            bottomLine.bottom == endAddress.centerY
            
            bottomDot.width == topDot.width
            bottomDot.height == bottomDot.width
            bottomDot.centerX == bottomLine.centerX
            bottomDot.centerY == bottomLine.bottom
            
            endAddress.top == duration.bottom + .margin
            endAddress.left == startAddress.left
            endAddress.right == startTime.right
            
            endTime.top == endAddress.bottom
            endTime.left == content.left + .margin
            endTime.right == startTime.right
            endTime.bottom == content.bottom - .margin
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override var trip: Trip! {
        didSet {
            startTimeLabel.text = timeFormatter.string(from: trip.startedAt)
            startAddressLabel.text = trip.startAddress
            if trip.duration < .minute {
                durationFormatter.allowedUnits = [.second]
            }
            durationLabel.text = durationFormatter.string(from: trip.duration)
            endAddressLabel.text = trip.endAddress
            if let d = trip.endedAt {
                endTimeLabel.text = timeFormatter.string(from: d)
            }
        }
    }
}

class TripDetailsBikeCell: TripDetailsCell {
    
    fileprivate let iconView = UIImageView()
    fileprivate let fleetLabel = UILabel()
    fileprivate let bikeLabel = UILabel()
    fileprivate let infoView = UIImageView(image: .named("icon_info"))
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        selectionStyle = .default
        
        contentView.addSubview(iconView)
        contentView.addSubview(fleetLabel)
        contentView.addSubview(bikeLabel)
        contentView.addSubview(infoView)
        
        iconView.contentMode = .scaleAspectFit
        
        fleetLabel.textColor = .lightGray
        fleetLabel.font = .theme(weight: .medium, size: .small)
        
        bikeLabel.textColor = .gray
        bikeLabel.font = .theme(weight: .medium, size: .body)
        bikeLabel.numberOfLines = 0
        
        infoView.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        
        constrain(iconView, fleetLabel, bikeLabel, infoView, contentView) { icon, fleet, bike, info, content in
            icon.left == content.left + .margin
            icon.top == content.top + .margin
            icon.height == 35
            icon.width == icon.height
            
            info.top == icon.top
            info.right == content.right - .margin
            
            fleet.left == icon.right + .margin/2
            fleet.top == icon.top
            fleet.right == info.left - .margin/2
            
            bike.left == fleet.left
            bike.top == fleet.bottom
            bike.right == content.right - .margin
            
            bike.bottom == content.bottom - .margin
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override var trip: Trip! {
        didSet {
            fleetLabel.text = trip.bike?.fleetName
            bikeLabel.text = trip.bike?.name
            iconView.kf.setImage(with: trip.bike?.fleetLogo)
        }
    }
}

class TripDetailsMapCell: TripDetailsCell {
    
    let mapImageView = UIImageView()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        contentView.addSubview(mapImageView)
        mapImageView.backgroundColor = .gray
        
        constrain(mapImageView, contentView) { map, content in
            map.edges == content.edges
            map.height == 200
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
