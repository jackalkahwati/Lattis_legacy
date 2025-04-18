//
//  SharedExtensions.swift
//  Lattis
//
//  Created by Ravil Khusainov on 15.01.2020.
//  Copyright © 2020 Velo Labs. All rights reserved.
//

import UIKit

extension CGFloat {
    static let margin: CGFloat = 16
}

public extension UIColor {
    static var neonBlue: UIColor {
        return .init(red: 0, green: 170/255, blue: 209/255, alpha: 1)
    }
}

extension UIView {
    func addShadow(color: UIColor = .black, offcet: CGSize = .init(width: 0, height: 4), radius: CGFloat = 5, opacity: Float = 0.11) {
        layer.shadowColor = color.cgColor
        layer.shadowOffset = offcet
        layer.shadowRadius = radius
        layer.shadowOpacity = opacity
    }
}

public extension UIFont {
    static var tiny: UIFont {
        return .systemFont(ofSize: 10)
    }
    
    static var tinyBold: UIFont {
        return .boldSystemFont(ofSize: 10)
    }
    
    static var small: UIFont {
        return .systemFont(ofSize: 12)
    }
    
    static var smallBold: UIFont {
        return .boldSystemFont(ofSize: 12)
    }
    
    static func regular(_ style: Style = .normal) -> UIFont {
        switch style {
        case .bold:
            return .boldSystemFont(ofSize: 16)
        case .italyc:
            return .italicSystemFont(ofSize: 16)
        default:
            return .systemFont(ofSize: 16)
        }
        
    }
    
    static var title: UIFont {
        return .boldSystemFont(ofSize: 18)
    }
    
    static var giant: UIFont {
        return .boldSystemFont(ofSize: 28)
    }
    
    enum Style {
        case normal, bold, italyc
    }
}
