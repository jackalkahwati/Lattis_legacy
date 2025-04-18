//
//  Device+Ellipse.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 07/02/2019.
//  Copyright © 2019 Lattis. All rights reserved.
//

import Device

extension CGFloat {
    static var regularFontSize: CGFloat {
        switch Device.size() {
        case .screen4Inch:
            return 14
        default:
            return 17
        }
    }
    
    static var titleLargeFontSize: CGFloat {
        switch Device.size() {
        case .screen4Inch:
            return 18
        default:
            return 24
        }
    }
    
    static let rowHeight: CGFloat = 44
    
    static var rowHeightMedium: CGFloat {
        if Device.size() > .screen4_7Inch {
            return 55
        }
        return .rowHeight
    }
    
    static var rowHeightBig: CGFloat {
        switch Device.size() {
        case .screen4Inch:
            return 70
        default:
            return 90
        }
    }
}

extension UINavigationBar {
    static let useLargeTitles = Device.size() > .screen4Inch
}
