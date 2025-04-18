//
//  UIImage+Trip.swift
//  Lattis
//
//  Created by Ravil Khusainov on 8/18/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

extension UIImage {
    class func image(for trip: Trip, with size: CGSize, completion: @escaping (UIImage?) -> ()) {
    }
    
    func resize(to size: CGSize) -> UIImage {
        UIGraphicsBeginImageContext(size)
        self.draw(in: CGRect(x: 0, y: 0, width: size.width, height: size.height))
        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return newImage!
    }
}

extension UIImageView {
    func set(trip: Trip, size: CGSize? = nil) {
        self.image = nil
        UIImage.image(for: trip, with: size ?? frame.size) { [weak self] (image) in
            self?.image = image
        }
    }
}
