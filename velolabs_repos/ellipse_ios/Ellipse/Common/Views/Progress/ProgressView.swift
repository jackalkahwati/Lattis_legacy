//
//  ProgressView.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/15/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import UICircularProgressRing
import Cartography

class ProgressView: UIView {
    
    var progress: Float {
        set {
            ringView.value = CGFloat(newValue)*100
        }
        get {
            return Float(ringView.value)/100
        }
    }
    
    fileprivate let titleLabel = UILabel()
    fileprivate let ringView = UICircularProgressRing()
    
    init(text: String?) {
        super.init(frame: .zero)
        titleLabel.text = text
        
        addSubview(titleLabel)
        addSubview(ringView)
        
        titleLabel.text = text
        titleLabel.textAlignment = .center
        titleLabel.font = .elRegular
        titleLabel.textColor = .elSlateGreyTwo
        titleLabel.numberOfLines = 0
        
        ringView.startAngle = -90
        ringView.endAngle = 270
        ringView.style = .ontop
        ringView.innerRingColor = .elDarkSkyBlue
        ringView.outerRingColor = .elBrownGreyTwo
        ringView.innerRingWidth = .margin
        ringView.outerRingWidth = ringView.innerRingWidth
        ringView.font = .elTitleLarge
        ringView.fontColor = .elSlateGreyTwo
        
        constrain(titleLabel, ringView, self) { title, ring, view in
            title.top == view.top
            title.left == view.left
            title.right == view.right
            
            ring.top == title.bottom + .margin
            ring.left == view.left
            ring.right == view.right
            ring.bottom == view.bottom
            ring.height == ring.width
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

extension AlertView {
    class func update() -> (AlertView, ProgressView) {
        let alert = AlertView()
        alert.titleLabel.text = "firmware_update".localized()
        let progress = ProgressView(text: "fw_update_hint".localized())
        alert.configure(view: progress)
        return (alert, progress)
    }
}
