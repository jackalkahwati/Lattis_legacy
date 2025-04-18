//
//  ClusterView.swift
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

protocol ZoomableAnnotationView: AnyObject {
    func zoomIn(animated: Bool)
    func zoomOut()
}

class ClusterView: MGLAnnotationView, ZoomableAnnotationView {
    fileprivate let countLabel = UILabel()
    fileprivate let zoomLevel: CGFloat = 1.5
    fileprivate var defaultSize = CGSize(width: 48, height: 48)
    
    init(cluster: CKCluster) {
        super.init(reuseIdentifier: "cluster")
        self.layer.zPosition = 1
        frame = .init(origin: .zero, size: defaultSize)
        let borderView = UIView()
        addSubview(borderView)
        layer.cornerRadius = defaultSize.height/2
        borderView.layer.cornerRadius = layer.cornerRadius - 2
        borderView.layer.borderColor = UIColor(white: 1, alpha: 0.5).cgColor
        borderView.layer.borderWidth = 2
        if let annotation = (cluster.annotations as? [Annotation])?.first {
            backgroundColor = annotation.value.color
        } else {
            backgroundColor = .accent
        }
        addSubview(countLabel)
        countLabel.textColor = .white
        countLabel.font = .theme(weight: .bold, size: .body)
        countLabel.textAlignment = .center
        updateCount()
        
        constrain(countLabel, borderView, self) { count, border, view in
            border.edges == view.edges.inseted(by: 2)
            count.edges == border.edges
        }
    }
    
    override var annotation: MGLAnnotation? {
        didSet {
            updateCount()
        }
    }
    
    fileprivate func updateCount() {
        if let cluster = annotation as? CKCluster {
            countLabel.text = cluster.annotations.count > 10 ? "10+" : "\(cluster.annotations.count)"
        } else {
            countLabel.text = "0"
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func zoomIn(animated: Bool = true) {
        superview?.bringSubviewToFront(self)
        guard transform.a != zoomLevel else { return }
        func perform() {
            self.transform = CGAffineTransform(scaleX: zoomLevel, y: zoomLevel)
            self.layoutIfNeeded()
        }
        if animated {
            UIView.animate(withDuration: 0.3) {
                perform()
            }
        } else {
            perform()
        }
    }
    
    func zoomOut() {
        guard frame.width > defaultSize.width else { return }
        UIView.animate(withDuration: 0.3) {
            self.transform = .identity
            self.layoutIfNeeded()
        }
    }
}
