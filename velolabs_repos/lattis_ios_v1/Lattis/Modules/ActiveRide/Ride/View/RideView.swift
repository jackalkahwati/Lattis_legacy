//
//  RideRideView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 22/02/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit
import Mapbox
import SDWebImage

class RideView: MapContainer {
    @IBOutlet weak var priceTitleLabel: UILabel!
    @IBOutlet weak var priceLabel: UILabel!
    @IBOutlet weak var stateLabel: UILabel!
    @IBOutlet weak var shadowView: UIView!
    @IBOutlet weak var navigationInfoLabel: UILabel!
    @IBOutlet weak var navigationView: UIView!
    @IBOutlet weak var navigationLabel: UILabel!
    @IBOutlet weak var navigationTopLayout: NSLayoutConstraint!
    @IBOutlet weak var calloutImageView: UIImageView!
    @IBOutlet weak var calloutDescription: UILabel!
    @IBOutlet weak var calloutTitle: UILabel!
    @IBOutlet weak var hintTextLabel: UILabel!
    @IBOutlet weak var hintTitleLabel: UILabel!
    @IBOutlet weak var hintView: UIView!
    @IBOutlet weak var hintBottomLayout: NSLayoutConstraint!
    @IBOutlet weak var endRideButtomLayout: NSLayoutConstraint!
    @IBOutlet weak var infoViewBottomLayout: NSLayoutConstraint!
    @IBOutlet weak var lockButton: LockButton!
    @IBOutlet weak var timeLabel: UILabel!
    @IBOutlet weak var endRideButton: UIButton!
    @IBOutlet weak var calloutBottomLayout: NSLayoutConstraint!
    @IBOutlet weak var calloutView: UIView!
    @IBOutlet weak var infoView: UIView!
    @IBOutlet weak var parkingButton: LoadingButton!
    @IBOutlet weak var rideLabel: Label!
    @IBOutlet weak var rideTextView: UITextView!
    @IBOutlet weak var currentPositionButton: UIButton!
    
    var isParkingsSown = false {
        didSet {
            if isParkingsSown {
                parkingButton.startLoading()
            } else {
                hideParkings()
            }
        }
    }
    
    private(set) var isRouteMode: Bool = false
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        shadowView.layer.shadowOpacity = 0.15
        shadowView.layer.shadowColor = UIColor.black.cgColor
        shadowView.layer.shadowOffset = CGSize(width: 0, height: 1)
        shadowView.layer.shadowRadius = 2
    }
    
    var isPositionButtonShown: Bool = true {
        didSet {
            guard isPositionButtonShown != oldValue else { return }
            if isPositionButtonShown {
                self.currentPositionButton.alpha = 0
                self.currentPositionButton.isHidden = false
                UIView.animate(withDuration: .defaultAnimation, delay: 0, options: .curveEaseIn, animations: {
                    self.currentPositionButton.alpha = 1
                }, completion: { _ in
                    
                })
            } else {
                UIView.animate(withDuration: .defaultAnimation, delay: 0, options: .curveEaseIn, animations: {
                    self.currentPositionButton.alpha = 0
                }, completion: { _ in
                    self.currentPositionButton.isHidden = true
                })
            }
        }
    }
    
    func toggleParkings() -> Bool {
        isParkingsSown = !isParkingsSown
        return isParkingsSown
    }
    
    func showParkings(zones: Bool) {
        showWarning(title: "active_ride_parkings_hint_title".localized(), text: zones ? "active_ride_parkings_hint_text".localized() : "active_ride_parkings_hint_text_no_zones".localized())
    }
    
    private func hideParkings() {
        parkingButton.backgroundColor = .white
        parkingButton.titleColor = .lsSteel
        
        infoView.isHidden = false
        infoView.alpha = 0
        UIView.animate(withDuration: .defaultAnimation, delay: 0, options: .curveEaseIn, animations: {
            self.infoView.alpha = 1
            self.hintView.alpha = 0
            self.hintBottomLayout.constant = -self.hintView.frame.height
            self.infoViewBottomLayout.constant = 0
            self.layoutIfNeeded()
        }, completion: { _ in
            self.hintView.isHidden = true
        })
    }
    
    func showCallout(with parking: Parking) {
        calloutTitle.text = parking.name
        calloutDescription.text = parking.description
        if let url = parking.pic {
            calloutImageView.sd_setImage(with: url, placeholderImage: nil)
        } else {
            calloutImageView.image = nil
        }        
        calloutBottomLayout.constant = -calloutView.frame.height
        calloutView.alpha = 0
        calloutView.isHidden = false
        layoutIfNeeded()
        UIView.animate(withDuration: .defaultAnimation, delay: 0, options: .curveEaseIn, animations: {
            self.calloutView.alpha = 1
            self.parkingButton.alpha = 0
            self.infoView.alpha = 0
            self.endRideButton.alpha = 0
            self.calloutBottomLayout.constant = 0
            self.hintView.alpha = 0
            self.hintBottomLayout.constant = -self.hintView.frame.height
            self.endRideButton.alpha = 0
            self.endRideButtomLayout.constant = -self.endRideButton.frame.height
            self.layoutIfNeeded()
        }, completion: { _ in
            self.hintView.isHidden = true
            self.endRideButton.isHidden = true
        })
    }
    
    func hideCallout() {
        guard calloutView.isHidden == false else { return }
        hintView.alpha = 0
        hintView.isHidden = false
        endRideButton.alpha = 0
        endRideButton.isHidden = false
        UIView.animate(withDuration: .defaultAnimation, delay: 0, options: .curveEaseOut, animations: {
            self.calloutView.alpha = 0
            self.parkingButton.alpha = 1
            self.infoView.alpha = 1
            self.endRideButton.alpha = 1
            self.calloutBottomLayout.constant = -self.calloutView.frame.height
            self.hintView.alpha = 1
            self.hintBottomLayout.constant = 0
            self.endRideButton.alpha = 1
            self.endRideButtomLayout.constant = 0
            self.layoutIfNeeded()
        }, completion: { _ in
            self.calloutView.isHidden = true
        })
    }
    
    func showWarning(title: String, text: String, completion: @escaping () -> () = {}) {
        hintTitleLabel.text = title
        hintTextLabel.text = text
        parkingButton.backgroundColor = .brightTurquoise
        parkingButton.titleColor = .white
        
        hintView.isHidden = false
        hintView.alpha = 0
        UIView.animate(withDuration: .defaultAnimation, delay: 0, options: .curveEaseIn, animations: {
            self.infoView.alpha = 0
            self.hintView.alpha = 1
            self.hintBottomLayout.constant = 0
            self.infoViewBottomLayout.constant = -self.infoView.frame.height
            self.layoutIfNeeded()
        }, completion: { _ in
            self.infoView.isHidden = true
            completion()
        })
    }
    
    func hideRouteMode() {
        isRouteMode = false
        UIView.animate(withDuration: .defaultAnimation, animations: {
            self.navigationView.alpha = 0
            self.navigationTopLayout.constant = -self.navigationView.frame.height
            self.layoutIfNeeded()
        }, completion: { _ in
            self.navigationView.isHidden = true
        })
    }
}
