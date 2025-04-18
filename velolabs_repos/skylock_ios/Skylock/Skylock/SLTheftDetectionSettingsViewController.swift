//
//  SLTheftDetectionSettingsViewController.swift
//  Skylock
//
//  Created by Andre Green on 6/10/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

class SLTheftDetectionSettingsViewController: SLBaseViewController {
    let lock:SLLock
    
    let xPadding:CGFloat = 25.0
    
    let user = SLDatabaseManager.shared().getCurrentUser()!
    
    init(lock:SLLock) {
        self.lock = lock
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    lazy var sensitivityView:UIView = {
        let height:CGFloat = 66.0
        let frame = CGRect(
            x: 0.0,
            y: self.view.frame.midY - 0.5*height,
            width: self.view.bounds.size.width,
            height: height
        )
        
        let view:UIView = UIView(frame: frame)
        view.backgroundColor = UIColor(red: 247, green: 247, blue: 248)
        
        let labelHeight:CGFloat = 17.0
        let labelFrame = CGRect(
            x: 0.0,
            y: 0.5*(view.bounds.size.height - labelHeight),
            width: view.bounds.size.width,
            height: labelHeight
        )
        
        let label:UILabel = UILabel(frame: labelFrame)
        label.text = NSLocalizedString("SECURITY SENSITIVITY", comment: "")
        label.textAlignment = NSTextAlignment.center
        label.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 14.0)
        label.textColor = UIColor(red: 140, green: 140, blue: 140)
        
        view.addSubview(label)
        
        return view
    }()
    
    lazy var infoView:UIView = {
        
        let yPadding:CGFloat = 24.0
        
        
        let y0 = self.navigationController!.navigationBar.bounds.size.height
            + UIApplication.shared.statusBarFrame.size.height
        let viewFrame = CGRect(
            x: 0.0,
            y: y0,
            width: self.view.bounds.size.width,
            height: self.sensitivityView.frame.minY - y0
        )
        
        let view:UIView = UIView(frame: viewFrame)
        view.backgroundColor = UIColor.white
        
        let maxWidth = self.view.bounds.size.width - 2*self.xPadding
        let font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 14.0)!
        let text = NSLocalizedString(
            "Either theft or crash detection may be activated, but not both at the same time. " +
            "Both require that you are connected to your Ellipse with bluetooth.",
            comment: ""
        )
        let utility = SLUtilities()
        let labelSize:CGSize = utility.sizeForLabel(
            font: font,
            text: text,
            maxWidth: maxWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        
        let frame = CGRect(
            x: self.xPadding,
            y: 0.5*(view.bounds.size.height - labelSize.height),
            width: labelSize.width,
            height: labelSize.height
        )
        
        let label:UILabel = UILabel(frame:frame)
        label.text = text
        label.font = font
        label.textColor = UIColor(white: 155.0/255.0, alpha: 1.0)
        label.textAlignment = .left
        label.numberOfLines = 0
        
        view.addSubview(label)

        return view
    }()
    
    lazy var slider:SLTouchSliderViewController = {
        let tsvc:SLTouchSliderViewController = SLTouchSliderViewController()
        return tsvc
    }()
    
    lazy var saveChangesButton:UIButton = {
        let height:CGFloat = 44.0
        let frame = CGRect(
            x: self.xPadding,
            y: self.view.bounds.size.height - 75.0 - height,
            width: self.view.bounds.size.width - 2*self.xPadding,
            height: height
        )
        
        let button:UIButton = UIButton(type: .system)
        button.frame = frame
        button.backgroundColor = UIColor(red: 87, green: 216, blue: 255)
        button.setTitle(NSLocalizedString("SAVE CHANGES", comment: ""), for: .normal)
        button.setTitleColor(UIColor.white, for: .normal)
        button.addTarget(self, action: #selector(saveChangesButtonPressed), for: .touchDown)
        
        return button
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = .white
        addBackButton()
        title = NSLocalizedString("THEFT DETECTION SETTINGS", comment: "")
        
        self.view.addSubview(self.sensitivityView)
        self.view.addSubview(self.infoView)
        self.view.addSubview(self.saveChangesButton)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        if !self.view.subviews.contains(self.slider.view) {
            let frame = CGRect(
                x: 0.0,
                y: self.sensitivityView.frame.maxY + 22.0,
                width: self.view.bounds.size.width,
                height: 36.0
            )
            self.slider.sliderValue = user.theftSensitivity
            self.slider.view.frame = frame
            self.addChildViewController(self.slider)
            self.view.addSubview(self.slider.view)
            self.view.bringSubview(toFront: self.slider.view)
            self.slider.didMove(toParentViewController: self)
        }
    }
    
    func saveChangesButtonPressed() {
        user.theftSensitivity = self.slider.getSliderValue()
        SLDatabaseManager.shared().save(user, withCompletion: nil)
        SLLockManager.sharedManager.updateTheftSensitivity()
        if let navController = self.navigationController {
            navController.popViewController(animated: true)
        }
    }
}
