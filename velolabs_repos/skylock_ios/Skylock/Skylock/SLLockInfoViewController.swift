//
//  SLLockInfoViewController.swift
//  Skylock
//
//  Created by Andre Green on 6/29/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

@objc protocol SLLockInfoViewControllerDelegate:class {
    func directionsButtonPressed(livc: SLLockInfoViewController)
}

class SLLockInfoViewController: UIViewController {
    let xPadding:CGFloat = 20.0
    
    let lock:SLLock
    
    let titleColor:UIColor = UIColor(red: 76, green: 79, blue: 97)
    
    weak var delegate:SLLockInfoViewControllerDelegate?
    
//    lazy var showLessButton:UIButton = {
//        let image:UIImage = UIImage(named: "map_close_details_x_icon")!
//        let frame = CGRect(
//            x: self.view.bounds.size.width - image.size.width - self.xPadding,
//            y: 20.0,
//            width: image.size.width,
//            height: image.size.height
//        )
//        
//        let button:UIButton = UIButton(frame: frame)
//        button.setImage(image, forState: .Normal)
//        button.addTarget(self, action: #selector(showLessButtonPressed), forControlEvents: .TouchDown)
//        
//        return button
//    }()
    
    lazy var lockNameLabel:UILabel = {
        let labelWidth = self.view.bounds.size.width - 2.0*self.xPadding
        let utility = SLUtilities()
        let font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 15)!
        let text = self.lock.displayName
        
        let labelSize:CGSize = utility.sizeForLabel(
            font: font,
            text: self.lock.displayName,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 1
        )
        
        let frame = CGRect(
            x: self.xPadding,
            y: 20.0,
            width: labelWidth,
            height: labelSize.height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = UIColor(white: 140.0/255.0, alpha: 1.0)
        label.text = text
        label.textAlignment = .left
        label.font = font
        label.numberOfLines = 1
        
        return label
    }()
    
    lazy var lockInfoLabel:UILabel = {
        let labelWidth = self.lockNameLabel.bounds.size.width
        let utility = SLUtilities()
        let font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 10)!
        let df:DateFormatter = DateFormatter()
        df.dateFormat = "MMM d, H:mm a"
        
        let text = self.lock.displayName +  " " + NSLocalizedString("Last Locked", comment: "") + " at "
            + (self.lock.lastConnected ==  nil ? "" : df.string(from: self.lock.lastConnected! as Date))
        
        let labelSize:CGSize = utility.sizeForLabel(
            font: font,
            text: self.lock.displayName,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 1
        )
        
        let frame = CGRect(
            x: self.xPadding,
            y: self.lockNameLabel.frame.maxY + 15.0,
            width: labelWidth,
            height: labelSize.height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = UIColor(red: 187, green: 187, blue: 187)
        label.text = text
        label.textAlignment = .left
        label.font = font
        label.numberOfLines = 0
        
        return label
    }()
    
//    lazy var crashButton:SLLockScreenAlertButton = {
//        let button:SLLockScreenAlertButton = SLLockScreenAlertButton(
//            activeImageName: "map_crash_detection_on_button",
//            inactiveImageName: "map_crash_detection_off_button",
//            titleText: NSLocalizedString("Crash detection", comment: ""),
//            textColor: self.titleColor
//        )
//        button.frame = CGRect(
//            x: self.xPadding,
//            y: CGRectGetMaxY(self.lockNameLabel.frame) + 25.0,
//            width: button.bounds.size.width,
//            height: button.bounds.size.height
//        )
//        button.addTarget(self, action: #selector(crashButtonPressed), forControlEvents: .TouchDown)
//        
//        return button
//    }()
//    
//    lazy var theftButton:SLLockScreenAlertButton = {
//        let button:SLLockScreenAlertButton = SLLockScreenAlertButton(
//            activeImageName: "map_theft_detection_on_button",
//            inactiveImageName: "map_theft_detection_off_button",
//            titleText: NSLocalizedString("Theft detection", comment: ""),
//            textColor: self.titleColor
//        )
//        button.frame = CGRect(
//            x: CGRectGetMaxX(self.crashButton.frame) + 25.0,
//            y: CGRectGetMinY(self.crashButton.frame),
//            width: button.bounds.size.width,
//            height: button.bounds.size.height
//        )
//        button.addTarget(self, action: #selector(theftButtonPressed), forControlEvents: .TouchDown)
//        
//        return button
//    }()
//    
//    lazy var tempInfoView:UIImageView = {
//        let image:UIImage = UIImage(named: "map_temp_lock_info")!
//        let frame = CGRect(
//            x: self.view.bounds.size.width - image.size.width - self.xPadding,
//            y: CGRectGetMidY(self.crashButton.frame) - 0.5*image.size.height,
//            width: image.size.width,
//            height: image.size.height
//        )
//        
//        let view:UIImageView = UIImageView(image: image)
//        view.frame = frame
//        
//        return view
//    }()
    
    lazy var getDirectionsButton:UIButton = {
        let height:CGFloat = 42.0
        let frame = CGRect(
            x: self.lockNameLabel.frame.minX,
            y: self.view.bounds.size.height - height - 15.0,
            width: self.lockInfoLabel.bounds.size.width,
            height: height
        )
        
        let button:UIButton = UIButton(type: .system)
        button.frame = frame
        button.setTitle(NSLocalizedString("GET DIRECTIONS", comment: ""), for: .normal)
        button.setTitleColor(UIColor.white, for: .normal)
        button.titleLabel?.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 12.0)
        button.backgroundColor = UIColor(red: 60, green: 83, blue: 119)
        button.addTarget(self, action: #selector(getDirectionsButtonPressed), for: .touchDown)
        
        return button
    }()
    
    init(lock:SLLock) {
        self.lock = lock
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = UIColor.white
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
//        if !self.view.subviews.contains(self.showLessButton) {
//            self.view.addSubview(self.showLessButton)
//        }
        
        if !self.view.subviews.contains(self.lockNameLabel) {
            self.view.addSubview(self.lockNameLabel)
        }
        
        if !self.view.subviews.contains(self.lockInfoLabel) {
            self.view.addSubview(self.lockInfoLabel)
        }
        
//        if !self.view.subviews.contains(self.crashButton) {
//            self.view.addSubview(self.crashButton)
//        }
//        
//        if !self.view.subviews.contains(self.theftButton) {
//            self.view.addSubview(self.theftButton)
//        }
//        
//        if !self.view.subviews.contains(self.tempInfoView) {
//            self.view.addSubview(self.tempInfoView)
//        }
        
        if !self.view.subviews.contains(self.getDirectionsButton) {
            self.view.addSubview(self.getDirectionsButton)
        }
    }
    
//    func showLessButtonPressed() {
//        
//    }
    
//    func crashButtonPressed() {
//        self.crashButton.selected = !self.crashButton.selected
//    }
//    
//    func theftButtonPressed() {
//        self.theftButton.selected = !self.theftButton.selected
//    }
    
    func getDirectionsButtonPressed() {
        self.delegate?.directionsButtonPressed(livc: self)
    }
}
