//
//  UIImage+OM.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 9/17/18.
//  Copyright © 2018 Lattis. All rights reserved.
//

import UIKit

extension CGSize {
    static func side(_ side: CGFloat) -> CGSize {
        return CGSize(width: side, height: side)
    }
}

extension UIImage{
    func resizeImageWith(newSize: CGSize) -> UIImage {
        let horizontalRatio = newSize.width / size.width
        let verticalRatio = newSize.height / size.height
        
        let ratio = max(horizontalRatio, verticalRatio)
        let newSize = CGSize(width: size.width * ratio, height: size.height * ratio)
        UIGraphicsBeginImageContextWithOptions(newSize, true, 0)
        draw(in: CGRect(origin: CGPoint(x: 0, y: 0), size: newSize))
        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return newImage!
    }
}
