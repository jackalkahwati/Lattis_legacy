//
//  UINavigationController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 13/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

extension UINavigationController {
    struct Style {
        var backgroundColor: UIColor
        var tintColor: UIColor
        var attributes: [NSAttributedString.Key: Any]
    }
    
    convenience init(rootViewController: UIViewController, style: Style) {
        self.init(rootViewController: rootViewController)
        isNavigationBarHidden = false
        navigationBar.barStyle = .black
        navigationBar.isTranslucent = false
        navigationBar.tintColor = style.tintColor
        navigationBar.barTintColor = style.backgroundColor
        navigationBar.titleTextAttributes = style.attributes
        navigationBar.isShadowHidden = false
    }
}

extension UINavigationController.Style {
    static let blue = UINavigationController.Style(backgroundColor: .lsTurquoiseBlue, tintColor: .white, attributes: [.foregroundColor: UIColor.white, .font: UIFont.lsTitleStyleFont()!])
    
    static let grey = UINavigationController.Style(backgroundColor: .lsWhite, tintColor: .lsSteel, attributes: [.foregroundColor: UIColor.lsSteel, .font: UIFont.lsTitleStyleFont()!])
    
    static let white = UINavigationController.Style(backgroundColor: .white, tintColor: .lsSteel, attributes: [.foregroundColor: UIColor.lsSteel, .font: UIFont.lsTitleStyleFont()!])
}

extension UINavigationBar {
    var isShadowHidden: Bool {
        set {
            layer.shadowOpacity = newValue ? 0 : 0.15
            layer.shadowColor = UIColor.black.cgColor
            layer.shadowOffset = CGSize(width: 0, height: 1)
            layer.shadowRadius = 2
        }
        get {
            return layer.shadowOpacity == 0
        }
    }
    
    func set(style: UINavigationController.Style) {
        tintColor = style.tintColor
        barTintColor = style.backgroundColor
        titleTextAttributes = style.attributes
        isShadowHidden = false
    }
}

extension UIBarButtonItem {
    class func close(target: Any?, action: Selector?) -> UIBarButtonItem {
        return UIBarButtonItem(image: #imageLiteral(resourceName: "Icon_close"), style: .plain, target: target, action: action)
    }
    
    class func back(target: Any?, action: Selector?) -> UIBarButtonItem {
        return UIBarButtonItem(image: #imageLiteral(resourceName: "icon_back_gray"), style: .plain, target: target, action: action)
    }
    
    class func menu(target: Any?, action: Selector?) -> UIBarButtonItem {
        return UIBarButtonItem(image: #imageLiteral(resourceName: "menu_icon_gray"), style: .plain, target: target, action: action)
    }
    
    static var empty: UIBarButtonItem {
        return UIBarButtonItem(title: "", style: .plain, target: nil, action: nil)
    }
}

