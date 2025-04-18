//
//  FindRideView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 29/12/2016.
//  Copyright © 2016 Velo Labs. All rights reserved.
//

import UIKit
import Mapbox
import iCarousel

protocol FindRideViewDelegate: class {
    func ride(view: FindRideView, didHide bike: Bike)
}

class FindRideView: MapContainer {
    
    @IBOutlet weak var gradientView: UIView!
    @IBOutlet weak var whiteView: UIView!
    @IBOutlet weak var carouselView: iCarousel!
    @IBOutlet weak var navigationView: FindRideNavigationView!
    @IBOutlet weak var calloutContainer: UIView!
    @IBOutlet weak var bottomLayout: NSLayoutConstraint!
    @IBOutlet weak var reserveButton: UIButton!
    @IBOutlet weak var warningContainer: UIView!
    @IBOutlet weak var warningLabel: UILabel!
    @IBOutlet weak var warningBottomLayout: NSLayoutConstraint!
    @IBOutlet weak var topLayout: NSLayoutConstraint!
    
    
    weak var delegate: FindRideViewDelegate?
    private(set) var isCalloutShown = false
    private(set) var isWarningShown = false

    override func awakeFromNib() {
        super.awakeFromNib()
        
        carouselView.isPagingEnabled = true
        navigationView.whiteView = whiteView
        let gradient = CAGradientLayer()
        gradient.frame = gradientView.bounds
        gradient.locations = [0.0, 0.27, 1.0]
        gradient.colors = [UIColor(white: 1, alpha: 0.0).cgColor, UIColor(white: 1, alpha: 0.6).cgColor, UIColor.white.cgColor]
        gradientView.layer.insertSublayer(gradient, at: 0)
        if #available(iOS 11.0, *) {}
        else {
            topLayout.constant = 0
        }
    }
    
    func showCallout(with bike: Bike) {
        isCalloutShown = true
        UIView.animate(withDuration: .defaultAnimation) {
            self.calloutContainer.alpha = 1
            self.bottomLayout.constant = 0
            self.layoutIfNeeded()
        }
    }
    
    func hideCallout() {
        isCalloutShown = false
        UIView.animate(withDuration: .defaultAnimation) {
            self.calloutContainer.alpha = 0
            self.bottomLayout.constant = -200
            self.layoutIfNeeded()
        }
    }
    
    func showWarning(with text: String) {
        isWarningShown = true
        warningLabel.text = text
        UIView.animate(withDuration: .defaultAnimation) {
            self.warningContainer.alpha = 1
            self.warningBottomLayout.constant = 0
            self.layoutIfNeeded()
        }
    }
    
    func hideWarning() {
        isWarningShown = false
        UIView.animate(withDuration: .defaultAnimation) {
            self.warningContainer.alpha = 0
            self.warningBottomLayout.constant = -128
            self.layoutIfNeeded()
        }
    }
}
