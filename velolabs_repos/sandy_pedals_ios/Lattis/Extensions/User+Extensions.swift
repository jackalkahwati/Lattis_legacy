//
//  User+Extensions.swift
//  Lattis
//
//  Created by Ravil Khusainov on 21/04/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import KeychainSwift

extension User {
    var password: String? {
        set {
            guard let pass = newValue else { return }
            KeychainSwift(keyPrefix: email).set(pass, forKey: .password)
        }
        get {
            return KeychainSwift(keyPrefix: email).get(.password)
        }
    }
    
    var image: UIImage? {
        set {
            guard let image = newValue else { return }
            let data = image.jpegData(compressionQuality: 1)
            let url = URL(fileURLWithPath: imagePath)
            _ = try? data?.write(to: url)
        }
        
        get {
            return UIImage(contentsOfFile: imagePath)
        }
    }
    
    var path: String {
        return FileManager.default.userDirectory(for: userId)
    }
    
    fileprivate var imagePath: String {
        return path.appending("/profileImage.jpeg")
    }
    
    static var current: User? {
        return CoreDataStack.shared.user(with: nil)
    }
}
