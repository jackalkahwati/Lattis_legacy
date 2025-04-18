//
//  AnnotationView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 26/06/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Mapbox
import Cartography
import LattisCore
import ClusterKit
import QuartzCore
import UICircularProgressRing

class Annotation: NSObject, MGLAnnotation, MKAnnotation {
    
    let value: MapPoint
    var coordinate: CLLocationCoordinate2D { return value.coordinate }
    var title: String? { return value.title }
    var subtitle: String? { return value.subtitle }
    
    init(_ value: MapPoint) {
        self.value = value
    }
    
    var bike: Bike? { value as? Bike }
}


class AnnotationView: MGLAnnotationView, ZoomableAnnotationView {
    
    fileprivate let backgroundImageView = UIImageView(image: UIImage(named: "icon_pin_background"))
    fileprivate let imageView = UIImageView(image: UIImage(named: "annotation_bike_regular"))
    fileprivate let batteryIcon = UIImageView(image: UIImage(named: "icon_electric_vehicle"))
    fileprivate let borderView = UIView()
    fileprivate var defaultSize = CGSize(width: 54, height: 60)
    fileprivate let zoomLevel: CGFloat = 1.5
    fileprivate let bageView = UIView()
    fileprivate let bageLabel = UILabel()
    fileprivate var batteryView: BatteryLevelView?
    fileprivate var progressView = UICircularProgressRing()
    fileprivate var imageTop: NSLayoutConstraint!
    fileprivate var imageLeading: NSLayoutConstraint!
    fileprivate var imageTrailing: NSLayoutConstraint!
    
    public var currentAnnotation: Annotation!
    public var customOffset =  CGVector(dx: 50, dy: 50)

    init(annotation: Annotation) {
        super.init(reuseIdentifier: annotation.value.identifier)
        self.currentAnnotation = annotation
        self.layer.zPosition = 1
        if let image = UIImage(named: annotation.value.identifier) {
            imageView.image = image
        }
        centerOffset = .init(dx: 0, dy: -defaultSize.height/2)
        frame = .init(origin: .zero, size: defaultSize)
        
//        borderView.layer.cornerRadius = defaultSize.width/2 - 2
//        borderView.layer.borderColor = UIColor(white: 1, alpha: 0.5).cgColor
//        borderView.layer.borderWidth = 2
        let level = annotation.value.batteryLevel ?? 0
        progressView.style = .ontop
        progressView.innerRingWidth = 2
        progressView.outerRingWidth = 2//level == 0 ? 2 : 4
        progressView.startAngle = -90
        progressView.shouldShowValueText = false
        progressView.outerRingColor = UIColor(white: 1, alpha: 0.5)
        progressView.innerRingColor = .white//UIColor(batteryLevel: level) ?? .clear
        progressView.value = CGFloat(level)
        
        addSubview(backgroundImageView)
//        addSubview(borderView)
        addSubview(progressView)
        addSubview(batteryIcon)
        addSubview(imageView)
        
        batteryIcon.isHidden = annotation.value.batteryLevel == nil

        // Adding battery level for electric scooters
//        if annotation.value.identifier == "annotation_bike_kick_scooter",
//           let level = annotation.bike?.batteryLevel {
//            batteryView = BatteryLevelView(level, batteryViewBorderColor: .white,  showTitleLabel: false)
//            if let view = batteryView {
//                addSubview(view)
//            }
//        }

        imageView.contentMode = .scaleAspectFit
        
        backgroundImageView.tintColor = annotation.value.color
        
        constrain(backgroundImageView, progressView, imageView, self) { background, border, image, view in
            border.top == view.top + 4
            border.left == view.left + 4
            border.right == view.right - 4
            border.height == border.width
            
            self.imageTop = image.top == view.top + 14
            self.imageLeading = image.leading == view.leading + 14
            self.imageTrailing = image.trailing == view.trailing - 14
            image.height == image.width
            
            background.edges == view.edges
        }
        
        constrain(batteryIcon, self) { battery, view in
            battery.top == view.top
            battery.centerX == view.centerX
        }

        if let batteryView = batteryView {
            constrain(batteryView, imageView){ batteryView, imageView in
                batteryView.top == imageView.top
                batteryView.left == imageView.left
                batteryView.right == imageView.right - 32
            }
        }

        addShadow(offcet: .init(width: 0, height: 3), opacity: 0)
        
        addSubview(bageView)
        bageView.addSubview(bageLabel)
        
        bageView.layer.borderWidth = 1
        bageView.layer.borderColor = UIColor.accent.cgColor
        bageView.backgroundColor = .white
        bageView.layer.cornerRadius = 12
        
        bageLabel.font = .theme(weight: .bold, size: .small)
        bageLabel.textColor = .accent
        
        constrain(bageView, bageLabel, self) { container, label, view in
            container.height == 24
            container.right == view.right + 4
            container.top == view.top - 4
            
            label.edges == container.edges.inseted(horizontally: 10, vertically: 0)
        }
        
        if let bage = annotation.value.bage {
            bageLabel.text = "\(bage)"
            bageView.isHidden = false
        } else {
            bageView.isHidden = true
        }
    }
    
    func zoomIn(animated: Bool = true) {
        superview?.bringSubviewToFront(self)
        guard frame.width == defaultSize.width else { return }
        var frame = self.frame
        frame.origin.y -= frame.height*(zoomLevel - 1)
        frame.origin.x -= frame.width*(zoomLevel - 1)/2
        frame.size.width *= zoomLevel
        frame.size.height *= zoomLevel
        centerOffset = .init(dx: 0, dy: -frame.height/2)
        imageTop.constant = 18
        imageTrailing.constant = -18
        imageLeading.constant = 18
        func perform() {
            self.frame = frame
            borderView.layer.borderColor = UIColor.white.cgColor
            borderView.layer.cornerRadius = frame.width/2 - 2
            self.superview?.layoutIfNeeded()
        }
        if animated {
            UIView.animate(withDuration: 0.3) {
                perform()
            }
        } else {
            perform()
        }
        layer.shadowOpacity = 0.5
    }
    
    func zoomOut() {
        guard frame.width > defaultSize.width else { return }
        var frame = self.frame
        frame.origin.y += defaultSize.height*(zoomLevel - 1)
        frame.origin.x += defaultSize.width*(zoomLevel - 1)/2
        frame.size = defaultSize
        centerOffset = .init(dx: 0, dy: -frame.height/2)
        imageTop.constant = 14
        imageTrailing.constant = -14
        imageLeading.constant = 14
        UIView.animate(withDuration: 0.3) {
            self.frame = frame
            self.borderView.layer.borderColor = UIColor(white: 1, alpha: 0.5).cgColor
            self.borderView.layer.cornerRadius = frame.width/2 - 2
            self.superview?.layoutIfNeeded()
        }
        layer.shadowOpacity = 0
    }
    
    override var annotation: MGLAnnotation? {
        didSet {
            guard let ann = annotation as? Annotation else { return }
            let value = ann.value.batteryLevel ?? 0
            progressView.value = CGFloat(value)
            if let image = UIImage(named: ann.value.identifier) {
                imageView.image = image
            }
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

