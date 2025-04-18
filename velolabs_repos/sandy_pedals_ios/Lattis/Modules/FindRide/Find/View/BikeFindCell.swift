//
//  BikeFindCell.swift
//  Lattis
//
//  Created by Ravil Khusainov on 26/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import SDWebImage


class BikeFindCell: UIView {
    @IBOutlet weak var batteryRightConstraint: NSLayoutConstraint!
    @IBOutlet weak var batteryChargeView: UIView!
    @IBOutlet weak var batteryView: UIView!
    @IBOutlet weak var batteryLabel: UILabel!
    @IBOutlet weak var textView: UITextView!
    @IBOutlet weak var iconImageView: UIImageView!
    @IBOutlet weak var companyName: UILabel!
    @IBOutlet weak var bikeName: UILabel!
    @IBOutlet weak var priceLabel: UILabel!
    @IBOutlet weak var cardIconView: UIImageView!
    @IBOutlet weak var cardLabel: UILabel!
    @IBOutlet weak var durationLabel: UILabel!
    @IBOutlet weak var depositLabel: UILabel!
    
    var openLink: (URL) -> () = {_ in}
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        layer.shadowColor = UIColor.black.cgColor
        layer.shadowOpacity = 0.1
        layer.shadowRadius = 3
        layer.shadowOffset = CGSize(width: 1, height: 1)
        clipsToBounds = false
        
        textView.delegate = self
    }
    
    var bike: Bike? {
        didSet {
            guard AppDelegate.fake == false else { return }
            bikeName.text = bike?.name
            companyName.text = bike?.fleetName
            if let logo = bike?.fleetLogo {
                iconImageView.sd_setImage(with: logo)
            } else {
                iconImageView.image = nil
            }
            if bike?.fleetType == .privatePay || bike?.fleetType == .publicPay, let price = bike?.priceForMembership {
                priceLabel.text = price.priceValue(bike!.currency)
            } else {
                priceLabel.text = "payment_cost_free".localized()
            }
            if bike?.fleetType == .privatePay || bike?.fleetType == .publicPay, let duration = bike?.priceDuration, let unit = bike?.priceUnit, duration > 0 {
                durationLabel.text = String(format: "payment_bike_card_per".localized(), "\(duration)", unit)
            } else {
                durationLabel.text = nil
            }
//            if bike != nil && bike!.bikeType == .eBike {
//                batteryView.isHidden = false
//            } else {
//                batteryView.isHidden = true
//            }
            batteryView.isHidden = true
            if let level = bike?.bikeBatteryLevel {
                let cf = CGFloat(1 - level)
                batteryRightConstraint.constant = cf*batteryChargeView.frame.width
                layoutIfNeeded()
                batteryLabel.text = bike?.bikeBatteryLevelString
            }
            if let terms = bike?.termsLink {
                let text = NSMutableAttributedString(string: "find_rite_terms_body".localized(), attributes: [.font: UIFont.systemFont(ofSize: 9), .foregroundColor: textView.textColor!])
                let range = (text.string.lowercased() as NSString).range(of: "find_rite_terms_title".localized().lowercased())
                text.addAttributes([.link: terms], range: range)
                textView.attributedText = text
                textView.linkTextAttributes = [.foregroundColor: textView.textColor!, .underlineStyle: NSUnderlineStyle.single.rawValue]
            }
        }
    }
    
    @IBAction func openTnC(_ sender: Any) {
        guard let link = bike?.termsLink else { return }
        openLink(link)
    }
    
    var card: CreditCard? {
        didSet {
            guard let card = card, let type = bike?.fleetType, type == .publicPay else {
                cardIconView.image = nil
                cardLabel.text = nil
                return
            }
            cardIconView.image = card.cardType?.icon
            cardLabel.text = card.shortMaskNumber
        }
    }
}

extension BikeFindCell: UITextViewDelegate {
    func textView(_ textView: UITextView, shouldInteractWith URL: URL, in characterRange: NSRange) -> Bool {
        openLink(URL)
        return false
    }
}
