//
//  PermissionsViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/23/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import CoreLocation
import Cartography

enum Permission {
    case location, notifications
    
    static func isGranted(_ permission: Permission) -> Bool {
        switch permission {
        case .location:
            return CLLocationManager.authorizationStatus() == .authorizedWhenInUse
        default:
            return true
        }
    }
}

protocol PermissionsDelegate: class {
    func permissionsFinished(for permission: Permission, dismiss: @escaping (@escaping () -> ()) -> ())
}

class PermissionsViewController: ViewController {
    
    weak var delegate: PermissionsDelegate?
    fileprivate let locationManager = CLLocationManager()
    fileprivate let titleLabel = UILabel()
    fileprivate let subtitleLabel = UILabel()
    fileprivate let okButton = UIButton(type: .custom)
    fileprivate let bgImageView = UIImageView(image: UIImage(named: "login_use_location_background"))
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = .white
        view.addSubview(bgImageView)
        view.addSubview(titleLabel)
        view.addSubview(subtitleLabel)
        view.addSubview(okButton)
        
        bgImageView.contentMode = .scaleAspectFill
        titleLabel.textColor = .black
        titleLabel.font = .elTitle
        titleLabel.numberOfLines = 0
        titleLabel.text = "location_using_screen_title".localized()
        titleLabel.textAlignment = .center
        
        subtitleLabel.textColor = .elSteel
        subtitleLabel.font = .elRegular
        subtitleLabel.numberOfLines = 0
        subtitleLabel.text = "location_using_screen_text".localized()
        subtitleLabel.textAlignment = .center
        
        bigPositive(okButton)
        okButton.setTitle("ok".localized().lowercased().capitalized, for: .normal)
        
        constrain(titleLabel, subtitleLabel, okButton, bgImageView, view) { title, subtitle, ok, bg, view in
            bg.top == view.top
            bg.left == view.left
            bg.right == view.right
            bg.bottom == view.bottom
            
            ok.left == view.left + .margin
            ok.right == view.right - .margin
            ok.bottom == view.safeAreaLayoutGuide.bottom - .margin
            
            subtitle.bottom == ok.top - .margin
            subtitle.left == ok.left
            subtitle.right == ok.right
            
            title.bottom == subtitle.top - .margin
            title.left == ok.left
            title.right == ok.right
        }
        
        okButton.addTarget(self, action: #selector(action(_:)), for: .touchUpInside)
    }
    
    override func close() {
        delegate?.permissionsFinished(for: .location, dismiss: { [unowned self] completion in
            self.dismiss(animated: true) {
                completion()
            }
        })
    }
    
    @objc fileprivate func action(_ sender: Any) {
        locationManager.delegate = self
        locationManager.requestWhenInUseAuthorization()
    }
}

extension PermissionsViewController: CLLocationManagerDelegate {
    func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        close()
    }
}
