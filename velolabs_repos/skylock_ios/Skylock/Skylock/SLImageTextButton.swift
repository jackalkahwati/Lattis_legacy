//
//  SLImageTextButton.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 5/30/17.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import UIKit

class SLImageTextButton: UIControl {
    let imageView: UIImageView = {
        let view = UIImageView()
        view.backgroundColor = .white
        view.layer.cornerRadius = 16
        view.clipsToBounds = true
        view.contentMode = .center
        return view
    }()
    let titleLabel: UILabel = {
        let label = UILabel()
        label.font = UIFont.systemFont(ofSize: 9)
        label.textAlignment = .center
        label.textColor = .white
        label.numberOfLines = 0
        label.lineBreakMode = .byWordWrapping
        return label
    }()
    
    fileprivate let image: UIImage
    fileprivate let onText: String
    fileprivate let offText: String
    
    init(image: UIImage, onText: String, offText: String) {
        self.image = image
        self.onText = onText
        self.offText = offText
        super.init(frame: .zero)
        self.alpha = 0.4
        self.imageView.image = image
        self.titleLabel.text = offText
        addSubview(imageView)
        addSubview(titleLabel)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        imageView.frame = {
            var frame = bounds
            frame.size.height = 32
            frame.size.width = 32
            frame.origin.x = bounds.midX - frame.width*0.5
            return frame
        }()
        
        titleLabel.frame = {
            var frame = bounds
            frame.origin.y = imageView.frame.maxY
            frame.size.height -= frame.origin.y
            return frame
        }()
    }
    
    var isOn: Bool = false {
        didSet {
            alpha = isOn ? 1 : 0.4
            imageView.image = isOn ? image + .slRobinsEgg : image
            titleLabel.text = isOn ? onText : offText
        }
    }
}
