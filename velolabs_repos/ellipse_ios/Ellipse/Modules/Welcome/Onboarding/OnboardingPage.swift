//
//  OnboardingPage.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/9/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography

class OnboardingPage: UIViewController {
    struct Content {
        let image: UIImage
        let title: String
        let subtitle: String
    }
    
    fileprivate let imageView = UIImageView()
    fileprivate let contentView = UIView()
    fileprivate let titleLabel = UILabel()
    fileprivate let subtitleLabel = UILabel()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        configureImage()
        configureLabels()
    }
    
    fileprivate func configureImage() {
        view.backgroundColor = .elSlateGrey
        contentView.backgroundColor = .elWhiteTwo
        view.addSubview(imageView)
        view.addSubview(contentView)
        imageView.contentMode = .bottom
        imageView.clipsToBounds = true
        constrain(imageView, contentView, view) { image, content, superview in
            image.top == superview.top
            image.left == superview.left
            image.right == superview.right
            content.left == superview.left
            content.right == superview.right
            content.bottom == superview.bottom
            image.bottom == content.top
            image.height == content.height
        }
    }
    
    fileprivate func configureLabels() {
        contentView.addSubview(titleLabel)
        contentView.addSubview(subtitleLabel)
        titleLabel.textColor = .black
        subtitleLabel.textColor = .black
        titleLabel.font = .elHeader
        subtitleLabel.font = .elTitleLight
        titleLabel.textAlignment = .center
        subtitleLabel.textAlignment = .center
        titleLabel.numberOfLines = 0
        subtitleLabel.numberOfLines = 0
        let margin: CGFloat = 30
        constrain(titleLabel, subtitleLabel, contentView) { title, subtitle, superview in
            title.top == superview.top + margin
            title.left == superview.left + margin
            title.right == superview.right - margin
            subtitle.left == title.left
            subtitle.right == title.right
            subtitle.top == title.bottom + margin/2
        }
    }
    
    init(_ content: Content) {
        imageView.image = content.image
        titleLabel.text = content.title
        subtitleLabel.text = content.subtitle
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
