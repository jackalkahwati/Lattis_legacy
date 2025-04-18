//
//  SLLockOnboardingTouchEllipseViewController.swift
//  Skylock
//
//  Created by Andre Green on 8/4/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

class SLLockOnboardingTouchEllipseViewController: SLBaseViewController {
    lazy var pressButtonLabel:UILabel = {
        let labelWidth = self.view.bounds.size.width
        let utility = SLUtilities()
        let font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 18.0)!
        let text = NSLocalizedString(
            "Press the middle button\non your Ellipse.",
            comment: ""
        )
        
        let labelSize:CGSize = utility.sizeForLabel(
            font: font,
            text: text,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        
        let frame = CGRect(
            x: 0.5*(self.view.bounds.size.width - labelSize.width),
            y: utility.statusBarAndNavControllerHeight(viewController: self) + 26.0,
            width: labelSize.width,
            height: labelSize.height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = UIColor(white: 140.0/255.0, alpha: 1.0)
        label.text = text
        label.textAlignment = NSTextAlignment.center
        label.font = font
        label.numberOfLines = 0
        
        return label
    }()
    
    lazy var touchPadLabel:UILabel = {
        let labelWidth = self.view.bounds.size.width
        let utility = SLUtilities()
        let font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 14.0)!
        let text = NSLocalizedString(
            "Tap the touchpad to wake up your Ellipse.",
            comment: ""
        )
        
        let labelSize:CGSize = utility.sizeForLabel(
            font: font,
            text: text,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        
        let frame = CGRect(
            x: 0.5*(self.view.bounds.size.width - labelSize.width),
            y: self.pressButtonLabel.frame.maxY + 25.0,
            width: labelSize.width,
            height: labelSize.height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = UIColor(red: 160, green: 200, blue: 224)
        label.text = text
        label.textAlignment = NSTextAlignment.center
        label.font = font
        label.numberOfLines = 0
        
        return label
    }()
    
    lazy var touchPadView:UIImageView = {
        let image:UIImage = UIImage(named: "lock_onboarding_touch_lock")!
        let frame = CGRect(
            x: 0.5*self.view.bounds.size.width - image.size.width + 50.0,
            y: self.touchPadLabel.frame.maxY + 15.0,
            width: image.size.width,
            height: image.size.height
        )
        
        let imageView:UIImageView = UIImageView(image: image)
        imageView.frame = frame
        
        return imageView
    }()
    
    lazy var touchPadOnButton:UIButton = {
        let height:CGFloat = 47.0
        let frame = CGRect(
            x: 0.0,
            y: self.view.bounds.size.height - height,
            width: self.view.bounds.size.width,
            height: height
        )
        
        let button:UIButton = UIButton(type: .system)
        button.frame = frame
        button.setTitle(NSLocalizedString("OK, TOUCH PAD IS ON", comment: ""), for: .normal)
        button.setTitleColor(UIColor.white, for: .normal)
        button.backgroundColor = UIColor(red: 87, green: 216, blue: 255)
        button.titleLabel?.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 13.0)
        button.addTarget(self, action: #selector(touchPadOnButtonPressed), for: .touchDown)
        
        return button
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = .white
        title = NSLocalizedString("WELCOME ON BOARD :)", comment: "")
        addBackButton()
        
        self.view.addSubview(self.pressButtonLabel)
        self.view.addSubview(self.touchPadLabel)
        self.view.addSubview(self.touchPadView)
        self.view.addSubview(self.touchPadOnButton)
    }
    
    func touchPadOnButtonPressed() {
        let alvc = SLAvailableLocksViewController()
        self.navigationController?.pushViewController(alvc, animated: true)
    }
}
