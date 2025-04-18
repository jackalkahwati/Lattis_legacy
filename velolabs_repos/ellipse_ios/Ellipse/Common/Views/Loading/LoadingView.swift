//
//  LoadingView.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/24/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography
import JTMaterialSpinner

class LoadingView: UIView, TopPresentable {
    
    let textLabel = UILabel()
    fileprivate let containerView = UIView()
    fileprivate let spinner = JTMaterialSpinner()
    
    init() {
        super.init(frame: .zero)
        
        let blur = UIVisualEffectView(effect: UIBlurEffect(style: .dark))
        addSubview(blur)
        addSubview(containerView)
        containerView.addSubview(textLabel)
        containerView.addSubview(spinner)
        containerView.layer.cornerRadius = 12
        containerView.backgroundColor = .white
        textLabel.font = .elRegular
        textLabel.textColor = .elSlateGreyTwo
        textLabel.textAlignment = .center
        textLabel.numberOfLines = 0
        textLabel.adjustsFontSizeToFitWidth = true
        
        spinner.circleLayer.strokeColor = textLabel.textColor.cgColor
        spinner.circleLayer.lineWidth = 2
        spinner.animationDuration = 2.3
        spinner.beginRefreshing()
        
        constrain(blur, containerView, textLabel, spinner, self) { blur, container, text, spinner, view in
            blur.edges == view.edges
            container.center == view.center
            
            spinner.top == container.top + .margin
            spinner.left >= container.left + .margin ~ UILayoutPriority(rawValue: 700)
            spinner.right >= container.right - .margin ~ UILayoutPriority(rawValue: 700)
            spinner.height == .margin*2
            spinner.width == spinner.height
            spinner.centerX == container.centerX
            
            text.top == spinner.bottom + .margin/2
            text.left >= container.left + .margin/2
            text.right >= container.right - .margin/2
            text.centerX == container.centerX
            
            container.left >= view.left + .margin*3
            container.right >= view.right - .margin*3
            text.bottom == container.bottom - .margin/2
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    class func show(_ text: String? = nil, in view: UIView? = nil) -> LoadingView {
        let loading = LoadingView()
        loading.textLabel.text = text
        if let parent = view {
            loading.show(in: parent)
        } else {
            loading.show()
        }
        return loading
    }
}
