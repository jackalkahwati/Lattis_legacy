//
//  SLSettingsViewController.swift
//  Skylock
//
//  Created by Andre Green on 7/25/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

import UIKit

class SLSettingsViewController: UIViewController {
    
    private let xPadding:CGFloat = 25.0
    private let titleFont = UIFont(name:"HelveticaNeue", size:14)
    private let infoFont = UIFont(name:"HelveticaNeue", size:12)
    private let titleColor = UIColor.color(97, green: 100, blue: 100)
    private let infoColor = UIColor.color(128, green: 128, blue: 128)
    private let dividerColor = UIColor.color(191, green: 191, blue: 191)
    private let settingGreenColor = UIColor.color(30, green: 221, blue: 128)
    var lock: SLLock?
    private var pinLabel: UILabel?
    var touchCounter: Int = 0
    var navBarTgr:UITapGestureRecognizer?
    
    lazy var controlView: UIView = {
        let y0: CGFloat = self.navigationController == nil ? 20.0 :
            self.navigationController!.navigationBar.bounds.size.height +
                UIApplication.shared.statusBarFrame.size.height + 20.0
        let frame = CGRect(
            x: 0,
            y: y0,
            width: self.view.bounds.size.width,
            height: 150.0
        )
        var view: UIView = UIView(frame: frame)
        
        let labelDividerHeight: CGFloat = 45.0
        let labelWidth = 0.5*view.bounds.size.width
        let utility = SLUtilities()
        let labelSize:CGSize = utility.sizeForLabel(
            font: self.titleFont!,
            text: "Looking for height",
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 1
        )
        
        let labelStart: CGFloat = 0.5*(view.bounds.size.height  - 2.0*labelSize.height - labelDividerHeight)
        let touchPadLabelFrame = CGRect(
            x: self.xPadding,
            y: labelStart,
            width: labelWidth,
            height: labelSize.height
        )
        let touchPadLabel: UILabel = UILabel(frame: touchPadLabelFrame)
        touchPadLabel.text = NSLocalizedString("Capactive Touch Pad", comment: "")
        touchPadLabel.font = self.titleFont
        touchPadLabel.textColor = self.infoColor
        touchPadLabel.sizeToFit()
        view.addSubview(touchPadLabel)
        
        let autoLockLabelFrame = CGRect(
            x: self.xPadding,
            y: touchPadLabel.frame.maxY + labelDividerHeight,
            width: labelWidth,
            height: labelSize.height
        )
        let autoLockLabel: UILabel = UILabel(frame: autoLockLabelFrame)
        autoLockLabel.text = NSLocalizedString("Proximity Lock/Unlock", comment: "")
        autoLockLabel.font = self.titleFont
        autoLockLabel.textColor = self.infoColor
        view.addSubview(autoLockLabel)
        
        let touchPadSwitch: UISwitch = UISwitch()
        touchPadSwitch.frame = CGRect(
                x: view.bounds.size.width - touchPadSwitch.bounds.size.width - self.xPadding,
                y: touchPadLabel.frame.midY - 0.5*touchPadSwitch.bounds.size.height,
                width: touchPadSwitch.bounds.size.width,
                height: touchPadSwitch.bounds.size.height
        )
        touchPadSwitch.addTarget(
            self,
            action: #selector(touchPadSwitchFlipped(sender:)),
            for: UIControlEvents.valueChanged
        )
        view.addSubview(touchPadSwitch)
        
        let autoLockSwitch: UISwitch = UISwitch()
        autoLockSwitch.frame = CGRect(
            x: view.bounds.size.width - autoLockSwitch.bounds.size.width - self.xPadding,
            y: autoLockLabel.frame.midY - 0.5*autoLockSwitch.bounds.size.height,
            width: autoLockSwitch.bounds.size.width,
            height: autoLockSwitch.bounds.size.height
        )
        autoLockSwitch.addTarget(
            self, action:
            #selector(autoLockSwitchFlipped(sender:)),
            for: UIControlEvents.valueChanged
        )
        view.addSubview(autoLockSwitch)
        
        return view
    }()
    
    lazy var pinCodeView:UIView = {
        let frame = CGRect(
            x: self.xPadding,
            y: self.controlView.frame.maxY,
            width: self.view.bounds.size.width - 2*self.xPadding,
            height: 125.0
        )
        let view: UIView = UIView(frame: frame)
        let dividerHeight: CGFloat = 0.5
        
        let dividerFrame = CGRect(x: 0, y: 10, width: view.bounds.size.width, height: dividerHeight)
        let dividerViewTop: UIView = UIView(frame: dividerFrame)
        dividerViewTop.backgroundColor = self.dividerColor
        view.addSubview(dividerViewTop)
        
        let text: String = NSLocalizedString("Change Pin Code", comment: "")
        let utility: SLUtilities = SLUtilities()
        let size: CGSize = utility.sizeForLabel(
            font: self.titleFont!,
            text:text,
            maxWidth:self.view.bounds.size.width - 2*self.xPadding,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 1
        )
        
        let headerLabelFrame = CGRect(x: 0, y: 20, width: view.bounds.size.width, height: size.height)
        let headerLabel: UILabel = UILabel(frame: headerLabelFrame)
        headerLabel.text = text
        headerLabel.textColor = self.titleColor
        headerLabel.font = self.titleFont
        view.addSubview(headerLabel)
        
        let pinLabelFrame = CGRect(x: 0, y: 0, width: view.bounds.size.width, height: CGFloat.greatestFiniteMagnitude)
        self.pinLabel = UILabel(frame: pinLabelFrame)
        self.pinLabel!.text = NSLocalizedString(
            "A pin code can be set to unlock Skylock without a mobile device.\n" +
            "*You must be in range of Skylock to chage the pin code",
            comment: ""
        )
        self.pinLabel!.font = self.infoFont
        self.pinLabel!.textColor = self.infoColor
        self.pinLabel!.numberOfLines = 0
        self.pinLabel!.sizeToFit()
        self.pinLabel!.frame = CGRect(
            x: 0.0,
            y: headerLabel.frame.maxY + 13.0,
            width: self.pinLabel!.bounds.size.width,
            height: self.pinLabel!.bounds.size.height
        )
        
        view.addSubview(self.pinLabel!)
        
        let buttonImageNormal: UIImage = UIImage(named: "settings_change_pin_button")!
        let buttonImageDissabled: UIImage = UIImage(named: "settings_change_pin_button_dissabled")!
        let changePinButtonFrame = CGRect(
            x: 0.5*(view.bounds.size.width - buttonImageNormal.size.width),
            y: self.pinLabel!.frame.maxY + 15,
            width: buttonImageNormal.size.width,
            height: buttonImageNormal.size.height
        )
        let changePinButton:UIButton  = UIButton(frame: changePinButtonFrame)
        changePinButton.addTarget(
            self, action:
            #selector(changePinButtonPressed),
            for: UIControlEvents.touchDown
        )
        changePinButton.setBackgroundImage(buttonImageNormal, for: UIControlState.normal)
        changePinButton.setBackgroundImage(buttonImageDissabled, for: UIControlState.disabled)
        view.addSubview(changePinButton)
        
        return view
    }()
    
    
    lazy var backButton: UIBarButtonItem = {
        let button = UIBarButtonItem(
            image: UIImage(named: "icon_chevron_left"),
            style: UIBarButtonItemStyle.plain,
            target: self,
            action: #selector(backButtonPushed)
        )
        
        return button
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
            
        self.navigationController?.navigationBar.barTintColor = self.settingGreenColor
        self.navigationController?.navigationBar.tintColor = UIColor.white
        
        self.navigationItem.leftBarButtonItem = self.backButton;
        
        self.view.backgroundColor = UIColor.color(255, green: 255, blue: 255)
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        self.navBarTgr = UITapGestureRecognizer(
            target: self,
            action: #selector(navigationBarTapped)
        )
        
        self.navigationController?.navigationBar.addGestureRecognizer(self.navBarTgr!)
        
        self.touchCounter = 0
        
        if !self.view.subviews.contains(self.controlView) {
            self.view.addSubview(self.controlView)
        }
        
        if !self.view.subviews.contains(self.pinCodeView) {
            self.view.addSubview(self.pinCodeView)
        }
    }
    
    func backButtonPushed() {
        print("back button pushed")
        self.dismiss(animated: true, completion: nil);
    }
    
    func changePinButtonPressed() {
        guard let macId = lock?.macId else { return }
        let touchPadViewController = SLTouchPadViewController(macId: macId, isOnboarding: false)
        self.navigationController?.pushViewController(touchPadViewController, animated: true)
    }
    
    func touchPadSwitchFlipped(sender: UISwitch) {
        print("touch pad switch flipped")
    }
    
    func autoLockSwitchFlipped(sender: UISwitch) {
        print("auto lock switch flipped")
    }
    
    func navigationBarTapped() {
        self.touchCounter += 1

        if (self.touchCounter == 10) {
            let logViewController = SLLogViewController()
            self.navigationController?.pushViewController(logViewController, animated: true)
            self.navigationController?.navigationBar.removeGestureRecognizer(self.navBarTgr!)
        }
    }
}
