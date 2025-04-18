//
//  SearchBikeCard.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 16.04.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Kingfisher
import Cartography

class SearchBikeCard: UIControl {

    let bike: Bike
    
    let priceButton = UIButton(type: .custom)
    let priceLabel = UILabel.label(font: .theme(weight: .medium, size: .small), allignment: .center)
    fileprivate let logoView = UIImageView()
    fileprivate let infoView = UIImageView(image: .named("icon_info"))
    fileprivate let bikeControl: BikeControl
    fileprivate let priceView = UIView()
    
    init(bike: Bike, pricingTitle: String?) {
        self.bike = bike
        bikeControl = .init(bike: bike)
        super.init(frame: .zero)
        
        let logoContainer = UIView()
        logoContainer.backgroundColor = .white
        logoContainer.layer.cornerRadius = .containerCornerRadius
        
        logoContainer.addShadow()
        logoContainer.addSubview(logoView)
        logoView.layer.cornerRadius = logoContainer.layer.cornerRadius
        logoView.clipsToBounds = true
        logoView.kf.setImage(with: bike.fleetLogo)
        
        addSubview(logoContainer)
        addSubview(bikeControl)
        addSubview(infoView)
        
        subviews.forEach{ $0.isUserInteractionEnabled = false }
        addSubview(priceView)
        
        priceView.backgroundColor = .secondaryBackground
        priceView.layer.cornerRadius = 17
        
        priceView.addSubview(priceLabel)
        if pricingTitle != nil {
            priceLabel.text = pricingTitle
        } else {
            priceLabel.text = bike.pricingOptions != nil ? "select_pricing".localized() : bike.fullPrice
        }
        
        infoView.tintColor = .black
        
        constrain(logoContainer, logoView, infoView, bikeControl, priceView, priceLabel, self) { lc, logo, info, control, price, pLabel, view in
            lc.height == 64
            lc.width == lc.height
            lc.left == view.left + .margin
            lc.bottom == info.bottom
            
            info.right == view.right - .margin
            info.top == view.top
            
            logo.edges == lc.edges.inseted(by: 5)
            
            control.top == lc.bottom + .margin/2
            control.left == view.left + .margin
            control.right == view.right - .margin
            control.bottom == price.top - .margin
            
            price.height == 34
            price.bottom == view.bottom
            price.left == view.left + .margin
            price.right == view.right - .margin
            
            pLabel.edges == price.edges.inseted(horizontally: .margin/2)
        }
        
        priceView.addSubview(priceButton)
        
        constrain(priceButton, priceView) { button, view in
            button.edges == view.edges
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
