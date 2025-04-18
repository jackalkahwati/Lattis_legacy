//
//  SLLoadingView.swift
//  Ellipse
//
//  Created by Andre Green on 8/30/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

class SLLoadingView: UIView {
    private let xPadding:CGFloat = 10.0
    
    private let loadingLabel: UILabel = {
        let label:UILabel = UILabel()
        label.textColor = UIColor.white
        label.font = UIFont(name: SLFont.YosemiteRegular.rawValue, size: 15.0)
        label.textAlignment = .center
        
        return label
    }()
    
    private let activity = UIActivityIndicatorView(activityIndicatorStyle: .whiteLarge)
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        addSubview(loadingLabel)
        addSubview(activity)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        loadingLabel.frame = CGRect(x: xPadding, y: 0.0, width: bounds.size.width - 2.0*xPadding, height: 34.0)
        activity.frame = {
            var frame = activity.frame
            frame.origin.x = 0.5*(bounds.size.width - frame.width)
            frame.origin.y = loadingLabel.frame.maxY + 20
            return frame
        }()
    }
    
    func setMessage(message: String) {
        self.loadingLabel.text = message
    }
    
    func rotate() {
        activity.startAnimating()
    }
}
