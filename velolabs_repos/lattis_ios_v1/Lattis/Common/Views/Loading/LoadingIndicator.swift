//
//  LoadingIndicator.swift
//  Lattis
//
//  Created by Ravil Khusainov on 5/28/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

class LoadingIndicator: UIView {
    fileprivate let activWidth: CGFloat = 60
    fileprivate let activityView: UIView = {
        let view = UIView()
        view.backgroundColor = .lsTurquoiseBlue
        return view
    }()
    
    deinit {
        endRefreshing()
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        addSubview(activityView)
        clipsToBounds = true
        activityView.clipsToBounds = true
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        layer.cornerRadius = bounds.height/2
        activityView.layer.cornerRadius = layer.cornerRadius
    }
    
    func beginRefreshing() {
        let startFrame: CGRect = {
            var frame = bounds
            frame.size.width = activWidth
            frame.origin.x = -activWidth
            return frame
        }()
        self.activityView.frame = startFrame
        UIView.animate(withDuration: 1.5, delay: 0, options: [.repeat], animations: {
            self.activityView.frame = {
                var frame = self.activityView.frame
                frame.origin.x = self.bounds.width
                return frame
            }()
        }, completion: { _ in
            self.activityView.frame = startFrame
        })
    }
    
    func endRefreshing() {
        
    }
}
