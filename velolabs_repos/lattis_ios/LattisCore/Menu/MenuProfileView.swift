//
//  MenuProfileView.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 08/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

fileprivate extension CGFloat {
    static var headerHeight: CGFloat {
        return 80
    }
    
    static var pictureHeight: CGFloat {
        return 42
    }
}

class MenuProfileView: UIControl {
    
    let photoButton = UIButton(type: .custom)
    
    fileprivate let nameLabel = UILabel.label(text: "My Profile", font: .theme(weight: .bold, size: .body))
    fileprivate let emailLabel = UILabel.label(text: "email@host.com", font: .theme(weight: .book, size: .small))
    fileprivate let profileImageView = UIImageView(image: .named("icon_person_circle"))
    
    init() {
        super.init(frame: .init(origin: .zero, size: .init(width: 0, height: .headerHeight)))
        
        addSubview(profileImageView)
        addSubview(photoButton)
        addSubview(nameLabel)
        addSubview(emailLabel)
        
        profileImageView.contentMode = .scaleAspectFill
        profileImageView.layer.cornerRadius = .pictureHeight/2
        profileImageView.clipsToBounds = true
        profileImageView.tintColor = .black
        
        constrain(profileImageView, photoButton, nameLabel, emailLabel, self) { image, pButton, name, email, view in
            image.height == .pictureHeight
            image.width == image.height
            image.centerY == view.centerY
            image.right == view.right - .margin
            
            pButton.edges == image.edges.inseted(by: -5)
            
            name.left == view.left + .margin
            name.right == image.left - .margin/2
            name.top == image.top
            
            email.bottom == image.bottom
            email.left == name.left
            email.right == name.right
        }
    }
    
    var user: User! {
        didSet {
            nameLabel.text = user.fullName
            if let path = user.photoPath {
                update(image: UIImage(contentsOfFile: path))
            }
            emailLabel.text = user.email
        }
    }
    
    var image: UIImage? {
        get {
            return profileImageView.image
        }
        set {
            user.save(photo: newValue)
            update(image: newValue)
        }
    }
    
    fileprivate func update(image: UIImage?) {
        profileImageView.image = image ?? .named("icon_person_circle")
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
