//
//  LockButton.swift
//  Lattis
//
//  Created by Ravil Khusainov on 7/14/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import JTMaterialSpinner

class LockButton: UIControl {
    private let spinner = JTMaterialSpinner()
    fileprivate let imageView: UIImageView = {
        let view = UIImageView(image: #imageLiteral(resourceName: "icon_lock_locked"))
        view.contentMode = .center
        return view
    }()
    
    override func awakeFromNib() {
        super.awakeFromNib()
        backgroundColor = .lsTurquoiseBlue
        
        addSubview(imageView)
        addSubview(spinner)
        
        layer.shadowColor = UIColor.black.cgColor
        layer.shadowOffset = CGSize(width: 0, height: 1)
        layer.shadowRadius = 2
        layer.shadowOpacity = 0.21
        
        spinner.isUserInteractionEnabled = false
        spinner.circleLayer.lineWidth = 3
        spinner.circleLayer.strokeColor = UIColor.white.cgColor
        spinner.animationDuration = 1.5
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        imageView.frame = bounds
        spinner.frame = bounds.insetBy(dx: 2, dy: 2)
    }
    
    var lockState: LockButton.LockState = .disconnected {
        didSet {
            switch lockState {
            case .disconnected:
                alpha = 1
                imageView.image = #imageLiteral(resourceName: "icon_connect")
                isEnabled = true
                spinner.endRefreshing()
            case .locked:
                alpha = 1
                imageView.image = #imageLiteral(resourceName: "icon_lock_locked")
                isEnabled = true
                spinner.endRefreshing()
            case .unlocked:
                alpha = 1
                imageView.image = #imageLiteral(resourceName: "icon_lock_unlocked")
                isEnabled = true
                spinner.endRefreshing()
            case .processing(_):
                alpha = 0.5
                isEnabled = false
                spinner.beginRefreshing()
            case .connecting:
                imageView.image = #imageLiteral(resourceName: "icon_connect")
                alpha = 0.5
                isEnabled = true
                spinner.beginRefreshing()
            }
        }
    }
}

extension LockButton {
    indirect enum LockState {
        case disconnected, locked, unlocked, processing(LockState), connecting
        
        var isLocked: Bool {
            switch self {
            case .locked:
                return true
            default:
                return false
            }
        }
        
        var text: String? {
            switch self {
            case .disconnected:
                return "active_ride_lock_disconnected".localized()
            case .connecting:
                return "active_ride_lock_connecting".localized()
            default:
                return nil
            }
        }
    }
}
