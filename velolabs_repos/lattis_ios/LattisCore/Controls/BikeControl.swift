//
//  BikeControl.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 26.09.2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Kingfisher
import Model

class BikeControl: UIControl {
    fileprivate let bike: Bike
    
    fileprivate let bikeTypeLabel = UILabel()
    fileprivate let bikeNameLabel = UILabel()
    fileprivate let fleetNameLabel = UILabel()
    fileprivate let imageView = UIImageView()
    fileprivate let fleetLogoView = UIImageView()
    fileprivate let infoView = UIImageView(image: .named("icon_info"))
    fileprivate let batteryView: BatteryLevelView
        
    init(bike: Bike) {
        self.bike = bike
        batteryView = BatteryLevelView(bike.bikeBatteryLevel)
        super.init(frame: .zero)
        
        let verticalStack = UIStackView(arrangedSubviews: [bikeTypeLabel, bikeNameLabel, fleetNameLabel, batteryView])
        verticalStack.axis = .vertical
        verticalStack.spacing = .margin/2
//        verticalStack.setCustomSpacing(.margin/2, after: bikeNameLabel)
        
        addSubview(verticalStack)
        addSubview(imageView)
        imageView.contentMode = .scaleAspectFit
        
        setContentCompressionResistancePriority(.defaultHigh, for: .vertical)
        verticalStack.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        imageView.setContentCompressionResistancePriority(.defaultLow, for: .vertical)
        imageView.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        
        constrain(verticalStack, imageView, self) { stack, image, view in
            stack.top == view.top
            stack.left == view.left
            stack.bottom == view.bottom
            
            stack.right == image.left - .margin/4
            image.right == view.right
            image.bottom == view.bottom
            image.top == view.top
            
            image.width == 100
        }
        
        bikeTypeLabel.font = .theme(weight: .bold, size: .small)
        bikeTypeLabel.textColor = .black
        
        fleetNameLabel.font = .theme(weight: .book, size: .small)
        fleetNameLabel.textColor = .lightGray
        
        bikeNameLabel.font = .theme(weight: .bold, size: .title)
        bikeNameLabel.textColor = .black
        
        bikeTypeLabel.text = bike.localizedKindTitle
        bikeNameLabel.text = bike.name
        fleetNameLabel.text = bike.fleetName
        imageView.kf.setImage(with: bike.picture)
        fleetLogoView.kf.setImage(with: bike.fleetLogo)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func update(batteryLevel: Int) {
        batteryView.level = batteryLevel
    }
}

final class BikeItem: UIView {
    var bike: Model.Bike { didSet { updateUI() } }
    
    fileprivate let bikeTypeLabel = UILabel()
    fileprivate let bikeNameLabel = UILabel()
    fileprivate let fleetNameLabel = UILabel()
    fileprivate let imageView = UIImageView()
    fileprivate let fleetLogoView = UIImageView()
    fileprivate let infoView = UIImageView(image: .named("icon_info"))
        
    init(bike: Model.Bike) {
        self.bike = bike
        super.init(frame: .zero)
        
        let batteryView = UIView()
        
        let verticalStack = UIStackView(arrangedSubviews: [bikeTypeLabel, bikeNameLabel, fleetNameLabel, batteryView])
        verticalStack.axis = .vertical
        verticalStack.setCustomSpacing(.margin/2, after: bikeNameLabel)
        
        addSubview(verticalStack)
        addSubview(imageView)
        imageView.contentMode = .scaleAspectFit
        
        setContentCompressionResistancePriority(.defaultHigh, for: .vertical)
        verticalStack.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        imageView.setContentCompressionResistancePriority(.defaultLow, for: .vertical)
        imageView.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        
        constrain(verticalStack, batteryView, imageView, self) { stack, battery, image, view in
            stack.top == view.top
            stack.left == view.left
            stack.bottom == view.bottom
            
            stack.right == image.left - .margin/4
            image.right == view.right
            image.bottom == view.bottom
            image.top == view.top
            
            image.width == 100
            battery.height == 15
        }
        
        bikeTypeLabel.font = .theme(weight: .bold, size: .small)
        bikeTypeLabel.textColor = .black
        
        fleetNameLabel.font = .theme(weight: .book, size: .small)
        fleetNameLabel.textColor = .lightGray
        
        bikeNameLabel.font = .theme(weight: .bold, size: .title)
        bikeNameLabel.textColor = .black
        
        updateUI()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    fileprivate func updateUI() {
        bikeTypeLabel.text = bike.bikeGroup.type.localizedTitle
        bikeNameLabel.text = bike.bikeName
        fleetNameLabel.text = bike.fleet.name
        imageView.kf.setImage(with: bike.bikeGroup.pic)
        fleetLogoView.kf.setImage(with: bike.fleet.logo)
    }
}
