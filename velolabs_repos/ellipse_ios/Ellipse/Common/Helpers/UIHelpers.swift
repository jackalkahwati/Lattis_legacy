//
//  UIHelpers.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 19/12/2018.
//  Copyright © 2018 Lattis. All rights reserved.
//

import UIKit
import Cartography

let bigRoundCorners: (UIButton) -> () = { button in
    button.titleLabel?.font = .elButtonBig
    button.contentEdgeInsets = .init(top: 0, left: 30, bottom: 0, right: 30)
    constrain(button) {$0.height == 44}
    button.layer.cornerRadius = 22
}

let bigPositive: (UIButton) -> () = { button in
    bigRoundCorners(button)
    button.backgroundColor = .elDarkSkyBlue
    button.setTitleColor(.white, for: .normal)
}

let facebookStyle: (UIButton) -> () = { button in
    bigRoundCorners(button)
    button.backgroundColor = .elFadedBlue
    button.setTitle("facebook_log_in".localized().lowercased().capitalized, for: .normal)
    button.setImage(UIImage(named: "facebook_icon"), for: .normal)
    button.contentEdgeInsets = .init(top: 0, left: 50, bottom: 0, right: 30)
    button.imageEdgeInsets = .init(top: 0, left: -40, bottom: 0, right: 0)
}

let smallRoundedCornersStyle: (UIButton) -> () = { button in
    button.titleLabel?.font = .elButtonSmall
    button.contentEdgeInsets = .init(top: 0, left: 20, bottom: 0, right: 20)
    constrain(button) {$0.height == 38}
    button.layer.cornerRadius = 19
}

let hideShowStyle: (UIButton) -> () = { button in
    button.titleLabel?.font = .elButtonSmall
    button.setTitle("show".localized().lowercased().capitalized, for: .normal)
    button.setTitle("hide".localized().lowercased().capitalized, for: .selected)
    button.setTitleColor(.elDarkSkyBlue, for: .normal)
    button.setTitleColor(.elDarkSkyBlue, for: .selected)
}

let smallNegativeStyle: (UIButton) -> () = { button in
    smallRoundedCornersStyle(button)
    let color = UIColor.elBrownGrey
    button.layer.borderWidth = 1
    button.layer.borderColor = color.cgColor
    button.setTitleColor(color, for: .normal)
}

let smallPositiveStyle: (UIButton) -> () = { button in
    smallRoundedCornersStyle(button)
    button.backgroundColor = .elDarkSkyBlue
    button.setTitleColor(.white, for: .normal)
}

let titleLigtStyle: (UILabel) -> () = { label in
    label.textColor = .black
    label.textAlignment = .center
    label.numberOfLines = 0
    label.font = .elTitleLight
}

let largeTitleWhiteStyle: (UINavigationBar) -> () = { bar in
    bar.isTranslucent = false
    bar.shadowImage = UIImage()
    bar.tintColor = .elSteel
    bar.titleTextAttributes = [.font: UIFont.elScreenTitle, .foregroundColor: UIColor.black]
    bar.largeTitleTextAttributes = [.font: UIFont.elScreenTitleLarge, .foregroundColor: UIColor.black]
    bar.prefersLargeTitles = UINavigationBar.useLargeTitles
    
    if #available(iOS 13.0, *) {
        let appearance = UINavigationBarAppearance()
        appearance.backgroundColor = .white
        appearance.titleTextAttributes = bar.titleTextAttributes!
        appearance.largeTitleTextAttributes = bar.largeTitleTextAttributes!
        
        bar.standardAppearance = appearance
        bar.scrollEdgeAppearance = appearance
    }
}

//let largeTitleBlueStyle: (UINavigationBar) -> () = { bar in
//    bar.isTranslucent = false
//    bar.shadowImage = UIImage()
//    bar.backgroundColor = .elDarkSkyBlueTwo
//    bar.tintColor = .white
//    bar.titleTextAttributes = [.font: UIFont.elHeader, .foregroundColor: UIColor.black]
//}
