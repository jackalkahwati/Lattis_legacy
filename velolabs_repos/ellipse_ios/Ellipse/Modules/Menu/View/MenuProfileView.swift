//
//  MenuProfileView.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 09/02/2019.
//  Copyright © 2019 Lattis. All rights reserved.
//

import UIKit
import Cartography
import Device

fileprivate extension Device {
    static var isBigScreen = Device.size() > .screen4_7Inch
}

fileprivate extension CGFloat {
    static var headerHeight: CGFloat {
        guard Device.isBigScreen else { return 120 }
        return 250
    }
    
    static var pictureHeight: CGFloat {
        guard Device.isBigScreen else { return 60 }
        return 126
    }
}

fileprivate extension UIImage {
    static let photoImage = UIImage(named: Device.isBigScreen ? "icon_camera_Myprofile" : "icon_camera_small")
}

class MenuProfileView: UIView {
    
    let photoButton = UIButton(type: .custom)
    let profileButton = UIButton(type: .custom)
    fileprivate let nameLabel = UILabel()
    fileprivate let backgroundPicture = UIImageView()
    fileprivate let profilePicture = UIImageView()
    fileprivate let editPicture = UIImageView(image: UIImage(named: "icon_pen"))
    
    init() {
        super.init(frame: .init(x: 0, y: 0, width: 300, height: .headerHeight))
        
        backgroundColor = .clear
        addSubview(backgroundPicture)
        let blurView = UIVisualEffectView(effect: UIBlurEffect(style: .extraLight))
        addSubview(blurView)
        addSubview(nameLabel)
        addSubview(editPicture)
        addSubview(profilePicture)
        addSubview(photoButton)
        addSubview(profileButton)
        
        photoButton.setImage(.photoImage, for: .normal)
        photoButton.backgroundColor = .elDarkSkyBlue
        photoButton.cornerRadius = .pictureHeight/2
        profilePicture.cornerRadius = photoButton.cornerRadius
        profilePicture.clipsToBounds = true
        profilePicture.contentMode = .scaleAspectFill
        backgroundPicture.clipsToBounds = true
        backgroundPicture.contentMode = .scaleAspectFill
        
        nameLabel.textAlignment =  Device.isBigScreen ? .center : .left
        nameLabel.textColor = .black
        nameLabel.font = .elTitle
        nameLabel.text = "myprofile".localized().lowercased().capitalized
        
        constrain(nameLabel, backgroundPicture, profilePicture, blurView, photoButton, self, editPicture, profileButton) { name, back, profile, blur, photo, view, edit, button in
            back.edges == view.edges
            blur.edges == view.edges
            
            profile.height == .pictureHeight
            profile.width == profile.height
            
            if Device.isBigScreen {
                name.bottom == view.bottom - .margin
                name.left == view.left - .margin ~ .defaultLow
                name.centerX == view.centerX

                profile.centerX == view.centerX
                profile.bottom == name.top - .margin
                
            } else {
                profile.bottom == view.bottom - .margin
                profile.left == view.left + .margin
                
                name.left == profile.right + .margin/2
                name.centerY == profile.centerY
            }
            
            edit.left == name.right + .margin/2
            edit.centerY == name.centerY
            edit.right >= view.right - .margin/2 ~ .defaultLow
            
            button.left == name.left
            button.right == edit.right
            button.bottom == name.bottom
            button.top == name.top
            
            photo.edges == profile.edges
        }
    }
    
    var user: User? {
        didSet {
            nameLabel.text = user?.fullName ?? "myprofile".localized().lowercased().capitalized
            user?.getPhoto { [weak self] (image) in
                self?.update(profileImage: image)
            }
            if let user = user, user.userType == .facebook {
                photoButton.isEnabled = false
            }
        }
    }
    
    var picture: UIImage? {
        get {
            return profilePicture.image
        }
        set {
            update(profileImage: newValue)
            user?.save(photo: newValue)
        }
    }
    
    fileprivate func update(profileImage: UIImage?) {
        let isPresent = profileImage != nil
        photoButton.backgroundColor = isPresent ? .clear : .elDarkSkyBlue
        photoButton.setImage(isPresent ? nil : .photoImage, for: .normal)
        profilePicture.image = profileImage
        backgroundPicture.image = profileImage
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
