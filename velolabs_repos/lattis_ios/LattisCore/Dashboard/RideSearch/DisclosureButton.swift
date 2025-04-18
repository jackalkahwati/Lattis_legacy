//
//  DisclosureButton.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 03.08.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

class DisclosureButton: UIControl {
    
    let textLabel = UILabel.label(font: .theme(weight: .bold, size: .text))

    init(title: String? = nil) {
        super.init(frame: .zero)
        
        textLabel.text = title
        let disclosureView = UIImageView(image: .named("icon_accessory_arrow"))
        disclosureView.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        textLabel.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        setContentHuggingPriority(.defaultHigh, for: .horizontal)
        
        addSubview(textLabel)
        addSubview(disclosureView)
        
        constrain(textLabel, disclosureView, self) { button, disclosure, view in
            view.height == 30
            button.left == view.left
            button.bottom == view.bottom
            button.top == view.top
            
            disclosure.right == view.right
            disclosure.centerY == view.centerY
            button.right == disclosure.left - .margin/2
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var title: String? {
        get { textLabel.text }
        set { textLabel.text = newValue }
    }
}
