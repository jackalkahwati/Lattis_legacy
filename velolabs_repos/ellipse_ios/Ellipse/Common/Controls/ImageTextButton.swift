//
//  SLImageTextButton.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 5/30/17.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import UIKit

class ImageTextButton: UIControl {
    let imageView: UIImageView = {
        let view = UIImageView()
        view.clipsToBounds = true
        view.contentMode = .center
        return view
    }()
    let titleLabel: UILabel = {
        let label = UILabel()
        label.font = .elImageButtonTitle
        label.textAlignment = .center
        label.textColor = .elLightGreyBlue
        label.numberOfLines = 2
        label.lineBreakMode = .byWordWrapping
        return label
    }()
    
    fileprivate var selectedTitle: String?
    fileprivate let text: String
    
    init(image: UIImage, text: String) {
        self.text = text.lowercased().capitalized
        super.init(frame: .zero)
        addSubview(imageView)
        addSubview(titleLabel)
        tintColor = .elLightGreyBlue
        titleLabel.textColor = tintColor
        imageView.image = image
        titleLabel.text = self.text
    }
    
    func setSelected(image: UIImage, text: String) {
        imageView.highlightedImage = image
        selectedTitle = text.lowercased().capitalized
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override var isSelected: Bool {
        didSet {
            imageView.isHighlighted = isSelected
            titleLabel.textColor = isSelected ? .elNiceBlue : tintColor
            titleLabel.text = isSelected ? selectedTitle : text
        }
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
    
    
}
