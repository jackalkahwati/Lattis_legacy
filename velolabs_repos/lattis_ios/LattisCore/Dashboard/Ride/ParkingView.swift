//
//  ParkingView.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 09.06.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Cartography
import Kingfisher
import Model


class ParkingView: UIView {
    
    let directionsButton = UIButton(type: .custom)
    let closeButton = UIButton(type: .custom)
    
    fileprivate var parkingText: String = "you_can_park_anywhere".localized()
    fileprivate let titleLabel = UILabel.label(text: "where_can_i_park".localized(), font: .theme(weight: .bold, size: .body), color: .white)
    fileprivate let infoLabel = UILabel.label(font: .theme(weight: .book, size: .text), color: .white, lines: 0)
    fileprivate let imageView = UIImageView()
    
    fileprivate var infoToBottom: NSLayoutConstraint!
    fileprivate var infoToRight: NSLayoutConstraint!
    fileprivate var imageToClose: NSLayoutConstraint!
    fileprivate var imageToBottom: NSLayoutConstraint!
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        backgroundColor = .accent
        layer.cornerRadius = .containerCornerRadius
        addSubview(titleLabel)
        addSubview(infoLabel)
        addSubview(closeButton)
        addSubview(directionsButton)
        
        closeButton.setImage(.named("icon_close_small"), for: .normal)
        closeButton.backgroundColor = UIColor(white: 0, alpha: 0.2)
        closeButton.layer.cornerRadius = 12
        closeButton.alpha = 0
        
        directionsButton.setTitle("parking_get_direction".localized(), for: .normal)
        directionsButton.titleLabel?.font = .theme(weight: .medium, size: .body)
        directionsButton.backgroundColor = closeButton.backgroundColor
        directionsButton.layer.cornerRadius = 24
        directionsButton.alpha = 0
        
        let imageContainer = UIView()
        imageContainer.backgroundColor = UIColor(white: 0, alpha: 0.2)
        imageContainer.layer.cornerRadius = 10
        imageContainer.addSubview(imageView)
        imageContainer.alpha = 0
        imageView.layer.cornerRadius = 6
        imageView.clipsToBounds = true
        imageView.contentMode = .scaleAspectFill
        addSubview(imageContainer)
        
        infoLabel.setContentHuggingPriority(.defaultHigh, for: .vertical)
        titleLabel.setContentHuggingPriority(.defaultHigh, for: .vertical)
        
        constrain(imageView, imageContainer) { image, view in
            image.edges == view.edges.inseted(by: 4)
        }
        
        constrain(titleLabel, infoLabel, closeButton, directionsButton, imageContainer, self) { title, info, close, directions, image, view in
            close.right == view.right - .margin/2
            close.top == view.top + .margin/2
            close.height == 24
            close.width == 24
            
            title.top == view.top + .margin
            title.left == view.left + .margin
            title.right == close.left - .margin/2
            
            info.top == title.bottom + .margin/2
            info.left == title.left
            self.infoToRight = info.right == view.right - .margin
            self.infoToBottom = info.bottom == view.safeAreaLayoutGuide.bottom - .margin
            info.bottom >= view.bottom - .margin
            
            image.right == view.right - .margin
            image.centerY == info.centerY ~ .defaultLow
            image.height == 88
            image.width == image.height
            self.imageToClose = image.top >= close.bottom + .margin/2 ~ .defaultLow
            self.imageToBottom = image.bottom <= directions.top - .margin ~ .defaultLow
                        
            directions.bottom == view.safeAreaLayoutGuide.bottom
            directions.left == view.left + .margin
            directions.right == view.right - .margin
            directions.height == 48
        }
    }
    
    var hasPakings: Bool = false {
        didSet {
            parkingText = hasPakings ? "you_can_park_anywhere".localized() : "no_parking_restrictions".localized()
            infoLabel.text = parkingText
        }
    }
    
    var parking: Parking.Spot? = nil {
        didSet {
            if let spot = parking {
                titleLabel.text = spot.name
                infoLabel.text = spot.details
                backgroundColor = .azureRadiance
                imageView.kf.setImage(with: spot.pic)
                closeButton.alpha = 1
                directionsButton.alpha = 1
                imageView.superview?.alpha = 1
                infoToRight.constant = -.margin*1.5 - 88
                infoToBottom.constant = -.margin - 48
                imageToBottom.priority = .defaultHigh
                imageToClose.priority = .defaultHigh
            } else {
                titleLabel.text = "where_can_i_park".localized()
                infoLabel.text = parkingText
                backgroundColor = .accent
                imageView.image = nil
                closeButton.alpha = 0
                directionsButton.alpha = 0
                imageView.superview?.alpha = 0
                infoToRight.constant = -.margin
                infoToBottom.constant = -.margin
                imageToBottom.priority = .defaultLow
                imageToClose.priority = .defaultLow
            }
        }
    }
    
    var hub: ParkingHub? {
        didSet {
            titleLabel.text = hub?.hubName
            infoLabel.text = hub?.description
            backgroundColor = .azureRadiance
            imageView.kf.setImage(with: hub?.image)
            closeButton.alpha = 1
            directionsButton.alpha = 1
            imageView.superview?.alpha = 1
            infoToRight.constant = -.margin*1.5 - 88
            infoToBottom.constant = -.margin - 48
            imageToBottom.priority = .defaultHigh
            imageToClose.priority = .defaultHigh
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
