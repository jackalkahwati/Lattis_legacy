//
//  BatteryLevelView.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 03.11.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

public final class BatteryLevelView: UIView {
    var level: Int {
        didSet {
            updateLevelUI()
        }
    }
    
    fileprivate let frameSize = CGSize(width: 18, height: 10)
    fileprivate let titleLable = UILabel.label(font: .theme(weight: .bold, size: .tiny))
    fileprivate let batteryView = UIView()
    fileprivate let catView = UIView()
    fileprivate let levelView = UIView()
    fileprivate var showTitleLabel = true

    public init(_ level: Int?,
                batteryViewBorderColor: UIColor = .black,
                catViewBackgroundColor: UIColor = .black,
                showTitleLabel: Bool = true) {
        self.level = level ?? 0
        self.showTitleLabel = showTitleLabel
        super.init(frame: .zero)
        
        guard level != nil else {
            constrain(self) { $0.height == self.frameSize.height}
            return
        }
        
        addSubview(titleLable)
        addSubview(batteryView)
        addSubview(catView)
        batteryView.addSubview(levelView)
        levelView.translatesAutoresizingMaskIntoConstraints = false
        levelView.layer.cornerRadius = 1

        catView.backgroundColor = catViewBackgroundColor
        catView.layer.cornerRadius = 1
        catView.layer.maskedCorners = [.layerMaxXMinYCorner, .layerMaxXMaxYCorner]

        batteryView.layer.cornerRadius = 3
        batteryView.layer.borderWidth = 2
        batteryView.layer.borderColor = batteryViewBorderColor.cgColor

        constrain(titleLable, batteryView, catView, self) { title, battery, cathode, view in
            title.left == view.left
            title.centerY == view.centerY
            
            battery.left == title.right + 3
            battery.height == frameSize.height
            battery.width == frameSize.width
            battery.top == view.top
            battery.bottom == view.bottom
            
            cathode.centerY == view.centerY
            cathode.left == battery.right + 1
            cathode.width == 2
            cathode.height == 4
        }
        
        updateLevelUI()
    }

    fileprivate func updateLevelUI() {
        if showTitleLabel { titleLable.text = "\(level) %" }
        let width: CGFloat = (frameSize.width - 4)*CGFloat(level)/100.0
        levelView.frame = .init(x: 2, y: 2, width: width, height: frameSize.height - 4)
        levelView.backgroundColor = UIColor(batteryLevel: level)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

public extension UIColor {
    convenience init?(batteryLevel: Int) {
        switch batteryLevel {
        case 0..<10:
            self.init(hexString: "FF0000")
        default:
            self.init(hexString: "57B84E")
        }
    }
}
