//
//  BatteryIndicator.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 22/01/2019.
//  Copyright © 2019 Lattis. All rights reserved.
//

import UIKit
import Cartography

class BatteryIndicator: UIView {
    var frameImage: UIImage? {
        set {
            frameImageView.image = newValue
        }
        get {
            return frameImageView.image
        }
    }
    var criticalColor: UIColor = .red
    var warningColor: UIColor = .orange
    var capacity: Float = 1 {
        didSet {
            calculate()
        }
    }
    fileprivate let frameImageView = UIImageView(image: UIImage(named: "battery_frame_black"))
    fileprivate let capacityView = UIView()
    fileprivate var rightEdge: NSLayoutConstraint?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        tintColor = .black
        capacityView.backgroundColor = tintColor
        addSubview(frameImageView)
        addSubview(capacityView)
        capacityView.cornerRadius = 1
        
        frameImageView.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        
        constrain(frameImageView, capacityView, self) { frame, capacity, view in
            frame.edges == view.edges
            let layouts = capacity.edges == inset(view.edges, 2, 2, 2, 4)
            self.rightEdge = layouts.filter({$0.secondAttribute == .trailing}).first
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    fileprivate func calculate() {
        if capacity < 0.15 {
            capacityView.backgroundColor = criticalColor
        } else if capacity < 0.3 {
            capacityView.backgroundColor = warningColor
        } else {
            capacityView.backgroundColor = tintColor
        }
        let width = frame.width - 6
        rightEdge?.constant = -4 - width*CGFloat(1 - capacity)
    }
}
