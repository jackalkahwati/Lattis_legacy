//
//  RideHistoryCostCell.swift
//  Lattis
//
//  Created by Ravil Khusainov on 8/22/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

class RideHistoryCell: UITableViewCell {
    var trip: Trip?
}

class RideHistoryCostCell: RideHistoryCell {
    @IBOutlet weak var refundLabel: UILabel!
    @IBOutlet weak var durationLabel: UILabel!
    @IBOutlet weak var priceLabel: UILabel!
    @IBOutlet weak var feesLabel: UILabel!
    @IBOutlet weak var excessFeeLabel: UILabel!
    @IBOutlet weak var depositLabel: UILabel!

    override var trip: Trip? {
        didSet {
            durationLabel.text = defaultValue(trip?.duration.time)
            priceLabel.text = defaultValue(trip?.price?.priceValue(trip!.currency))
            feesLabel.text = defaultValue(trip?.penaltyFees?.priceValue(trip!.currency))
            depositLabel.text = defaultValue(trip?.deposit?.priceValue(trip!.currency))
            excessFeeLabel.text = defaultValue(trip?.excessUsageFees?.priceValue(trip!.currency))
            if let duration = trip?.refundCriteria, let unit = trip?.refundCriteriaUnit {
                self.refundLabel.text = String(format: "ride_history_refund".localized(), "\(duration)", unit)
            } else {
                self.refundLabel.text = nil
            }
        }
    }
}

class RideHistoryFreeCell: RideHistoryCell {
    @IBOutlet weak var durationLabel: UILabel!
    
    override var trip: Trip? {
        didSet {
            durationLabel.text = defaultValue(trip?.duration.time)
        }
    }
}


class RideHistoryMapCell: RideHistoryCell {
    @IBOutlet weak var snapshotView: UIImageView!
}

class RideHistoryDetailsHeader: UIView {
    let titleLabel: UILabel = {
        let label = UILabel()
        label.font = UIFont.boldSystemFont(ofSize: 18)
        label.textColor = .lsWarmGrey
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = .white
        addSubview(titleLabel)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.frame = {
            var frame = bounds
            frame.size.width -= 32
            frame.origin.x = 16
            return frame
        }()
    }
}

class RideHistoryDetailsFreeHeader: RideHistoryDetailsHeader {
    fileprivate let subtitleLabel: UILabel = {
        let label = UILabel()
        label.textAlignment = .right
        label.font = UIFont.systemFont(ofSize: 16)
        label.textColor = .lsTurquoiseBlue
        label.text = "payment_cost_free".localized()
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        addSubview(subtitleLabel)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        subtitleLabel.frame = titleLabel.frame
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

class RideHistoryDetailsCostHeader: RideHistoryDetailsHeader {
    fileprivate let line: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(white: 226.0 / 255.0, alpha: 1.0)
        return view
    }()
    fileprivate let subtitleLabel: UILabel = {
        let label = UILabel()
        label.textAlignment = .right
        label.font = UIFont.systemFont(ofSize: 13)
        label.textColor = .lsCoolGrey
        return label
    }()
    fileprivate let cardImageView: UIImageView = {
        let view = UIImageView()
        view.contentMode = .right
        return view
    }()
    
    init(card: CreditCard?) {
        super.init(frame: .zero)
        
        addSubview(line)
        addSubview(subtitleLabel)
        addSubview(cardImageView)
        subtitleLabel.text = card?.smallMaskNumber
        cardImageView.image = card?.cardType?.icon
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        line.frame = {
            var frame = bounds
            frame.size.height = 1
            frame.origin.x = 16
            frame.size.width -= frame.origin.x*2
            frame.origin.y = bounds.height - frame.height
            return frame
        }()
        subtitleLabel.frame = titleLabel.frame
        cardImageView.frame = {
            var frame = bounds
            frame.origin.x = -60
            return frame
        }()
    }
}

class RideHistoryFooter: UIView {
    fileprivate let shadowView = UIView()
    fileprivate let titleLabel: UILabel = {
        let label = UILabel()
        label.text = "total".localized()
        label.font = UIFont.systemFont(ofSize: 13)
        label.textColor = .lsWarmGrey
        return label
    }()
    fileprivate let totalLabel: UILabel = {
        let label = UILabel()
        label.font = UIFont.systemFont(ofSize: 13)
        label.textColor = .lsWarmGrey
        label.textAlignment = .right
        return label
    }()
    
    init(trip: Trip, shadow: Bool) {
        super.init(frame: .zero)
        clipsToBounds = true
        backgroundColor = shadow ? .lsWhite : .white
        
        if shadow {
            shadowView.layer.shadowColor = UIColor.black.cgColor
            shadowView.layer.shadowOpacity = 0.06
            shadowView.layer.shadowRadius = 3
            shadowView.layer.shadowOffset = CGSize(width: 0, height: 2)
        }
        
        shadowView.backgroundColor = .white
        addSubview(shadowView)
        shadowView.addSubview(titleLabel)
        shadowView.addSubview(totalLabel)
        
        totalLabel.text = defaultValue(trip.total?.priceValue(trip.currency))
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        shadowView.frame = {
            var frame = bounds
            frame.size.height = 41
            return frame
        }()
        titleLabel.frame = {
            var frame = shadowView.bounds
            frame.origin.x = 16
            frame.size.width -= frame.origin.x*2
            return frame
        }()
        totalLabel.frame = titleLabel.frame
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
