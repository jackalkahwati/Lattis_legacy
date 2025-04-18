//
//  SLFirmwareUpdateProgressBarView.swift
//  Ellipse
//
//  Created by Andre Green on 8/8/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

class SLFirmwareUpdateProgressBarView: UIView {
    lazy var barView:UIView = {
        let frame = CGRect(x: 0.0, y: 0.0, width: 1.0, height: self.bounds.size.height)
        
        let view:UIView = UIView(frame: frame)
        view.backgroundColor = UIColor(red: 102, green: 177, blue: 227)

        return view
    }()
    
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.layer.borderColor = UIColor.white.cgColor
        self.layer.borderWidth = 1.0
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        if !self.subviews.contains(self.barView) {
            self.addSubview(self.barView)
        }
    }
    
    func updateBarWithRatio(ratio: Double) {
        self.barView.frame = CGRect(
            x: 0.0,
            y: 0.0,
            width: CGFloat(ratio)*self.bounds.size.width,
            height: self.barView.bounds.size.height
        )
    }
}
