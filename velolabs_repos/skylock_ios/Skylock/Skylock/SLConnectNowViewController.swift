//
//  SLConnectNowViewController.swift
//  Ellipse
//
//  Created by Ranjitha on 12/29/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

class SLConnectNowViewController: SLBaseViewController {
    private let xPadding: CGFloat = 15.0
    
    lazy var getStartedLabel: UILabel = {
        var frame = CGRect(x: self.xPadding*2, y: 46.0, width: self.view.bounds.size.width - self.xPadding*4, height: 22.0)
        let label = UILabel(frame: frame)
        label.text = NSLocalizedString("We've found the following Ellipse.", comment: "")
        label.textColor = UIColor.slBluegrey
        label.font = .systemFont(ofSize: 18)
        label.textAlignment = .center
        label.numberOfLines = 0
        frame.size.height = label.sizeThatFits(CGSize(width: frame.width, height: CGFloat.greatestFiniteMagnitude)).height
        label.frame = frame
        return label
    }()
    
    lazy var connectEllipseLabel: UILabel = {
        let labelWidth = self.view.bounds.size.width - 2*self.xPadding
        let utility = SLUtilities()
        let font = UIFont.systemFont(ofSize: 16)

        let frame = CGRect(x: self.xPadding,y: self.getStartedLabel.frame.maxY + 76.0,
                           width: self.view.bounds.size.width - self.xPadding*2,
                           height: font.lineHeight)
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = UIColor.slWarmGrey
        label.textAlignment = .center
        label.font = font
        label.numberOfLines = 1
        
        return label
    }()
    
    lazy var setUpEllipseButton: UIButton = {
        
        let frame = CGRect(
            x: self.xPadding*2,
            y: self.connectEllipseLabel.frame.maxY + 20,
            width: self.view.bounds.size.width - 4*self.xPadding,
            height: 68
        )
        
        let button:UIButton = UIButton(type: .custom)
        button.frame = frame
        button.setTitle(NSLocalizedString("CONNECT NOW", comment: ""), for: .normal)
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 15)
        button.backgroundColor = UIColor.slLightBlueGrey
        button.addTarget(self, action: #selector(connectNowPressed), for: .touchUpInside)
        let image = UIImage(named: "connect_to_lock_icon")
        button.setImage(image, for: .normal)
        button.imageToRight()
        button.layer.cornerRadius = 3
        return button
    }()
    
    private var lock: SLLock!
    
    convenience init(lock: SLLock) {
        self.init()
        self.lock = lock
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationItem.title = NSLocalizedString("WELCOME ON BOARD :)", comment: "")
        
        self.view.backgroundColor = UIColor.white
        self.view.addSubview(getStartedLabel)
        self.view.addSubview(self.connectEllipseLabel)
        self.view.addSubview(self.setUpEllipseButton)
        
        connectEllipseLabel.text = lock?.displayName
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController?.isNavigationBarHidden = true
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        navigationController?.isNavigationBarHidden = false
    }
    
    func connectNowPressed() {
        SLLockManager.sharedManager.connectToLockWithMacAddress(macAddress: lock.macId!)
        NotificationCenter.default.post(name: hideMenuNotification, object: nil)
        dismiss(animated: true, completion: {
            NotificationCenter.default.post(name: NSNotification.Name(rawValue: kSLNotificationLockManagerStartedConnectingLock), object: nil)
        })
    }
}

extension UIButton {
    func imageToRight() {
        transform = CGAffineTransform(scaleX: -1.0, y: 1.0)
        titleLabel?.transform = CGAffineTransform(scaleX: -1.0, y: 1.0)
        imageView?.transform = CGAffineTransform(scaleX: -1.0, y: 1.0)
        titleEdgeInsets = UIEdgeInsets(top: 0, left: 20, bottom: 0, right: 0)
    }
}
