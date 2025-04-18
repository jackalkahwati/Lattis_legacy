//
//  WelcomViewController.swift
//  Ellipse
//  Created by Ranjitha on 12/29/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit
import Crashlytics

class WelcomViewController: SLBaseViewController {
    weak var ellipleBarController: BottomBarPresenting? {
        didSet {
            ellipleBarController?.hideBottomBar()
        }
    }
    let xPadding: CGFloat = 15.0
    
    let labelTextColor:UIColor = UIColor(red: 160, green: 200, blue: 224)
    
    lazy var getStartedLabel:UILabel = {
        let frame = CGRect(x: 0.0, y: 80.0, width: self.view.bounds.size.width, height: 22.0)
        let label:UILabel = UILabel(frame: frame)
        label.text = NSLocalizedString("Let's get you started.", comment: "")
        label.textColor = UIColor.color(140, green: 140, blue: 140)
        label.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 18.0)
        label.textAlignment = .center
        
        return label
    }()
    
    lazy var connectEllipseLabel:UILabel = {
        let labelWidth = self.view.bounds.size.width - 2*self.xPadding
        let utility = SLUtilities()
        let font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 14.0)!
        let text = NSLocalizedString(
            "To get the most out of this app you'll\nneed to set up at least one Ellipse.",
            comment: ""
        )
        
        let labelSize:CGSize = utility.sizeForLabel(
            font: font,
            text: text,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        
        let frame = CGRect(x: 0.5*(self.view.bounds.size.width - labelSize.width), y: self.getStartedLabel.frame.maxY + 76.0,
            width: labelSize.width, height: labelSize.height)
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = UIColor.color(188, green: 187, blue: 187)
        label.text = text
        label.textAlignment = .center
        label.font = font
        label.numberOfLines = 0
        
        return label
    }()
    
    lazy var setUpEllipseButton:UIButton = {
        
        let frame = CGRect(
            x: self.xPadding,
            y: self.connectEllipseLabel.frame.maxY + 20,
            width: self.view.bounds.size.width - 2.0*self.xPadding,
            height: 44.0
        )
        
        let button:UIButton = UIButton(type: .system)
        button.frame = frame
        button.setTitle(NSLocalizedString("SET UP MY OWN ELLISPE", comment: ""), for: .normal)
        button.setTitleColor(UIColor.white, for: .normal)
        button.titleLabel?.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 12.0)
        button.backgroundColor = UIColor(red: 87, green: 216, blue: 255)
        button.addTarget(self, action: #selector(yesButtonPressed), for: .touchDown)
        
        return button
    }()
    
    lazy var sharingInfoLabel:UILabel = {
        let labelWidth = self.view.bounds.size.width - 2*self.xPadding
        let utility = SLUtilities()
        let font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 14.0)!
        let text = NSLocalizedString(
            "I have received an invitation code to\nborrow a friend’s Ellipse",
            comment: ""
        )
        
        let labelSize:CGSize = utility.sizeForLabel(
            font: font,
            text: text,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        
        let frame = CGRect(x: 0.5*(self.view.bounds.size.width - labelSize.width), y: self.setUpEllipseButton.frame.maxY + 76.0, width: labelSize.width, height: labelSize.height)
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = UIColor.color(188, green: 187, blue: 187)
        label.text = text
        label.textAlignment = NSTextAlignment.center
        label.font = font
        label.numberOfLines = 0
        
        return label
    }()
    
    lazy var invitationButton:UIButton = {
        let color = UIColor(red: 87, green: 216, blue: 255)
        let frame = CGRect(
            x: self.xPadding,
            y: self.sharingInfoLabel.frame.maxY + 26.0,
            width: self.setUpEllipseButton.bounds.size.width,
            height: self.setUpEllipseButton.bounds.size.height
        )
        
        let button:UIButton = UIButton(type: .system)
        button.frame = frame
        button.setTitle(NSLocalizedString("ADD A FRIEND'S ELLIPSE", comment: ""), for: .normal)
        button.setTitleColor(color, for: .normal)
        button.titleLabel?.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 12.0)
        button.addTarget(self, action: #selector(invitationButtonPressed), for: .touchDown)
        button.layer.borderWidth = 1.0
        button.layer.borderColor = color.cgColor
        return button
    }()
    
    internal class func navigation() -> UINavigationController {
        let controller = WelcomViewController()
        let nc = UINavigationController(rootViewController: controller)
        nc.navigationBar.barStyle = .black
        nc.navigationBar.tintColor = .white
        nc.navigationBar.barTintColor = .slBluegrey
        nc.isNavigationBarHidden = false
        return nc
    }
    
    convenience init(title: String?) {
        self.init()
        self.title = title
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        if title == nil {
            title = NSLocalizedString("WELCOME ON BOARD :)", comment: "")
            addMenuButton()
        } else {
            addBackButton()
        }
        self.view.backgroundColor = .white

        self.view.addSubview(getStartedLabel)
        self.view.addSubview(self.connectEllipseLabel)
        self.view.addSubview(self.setUpEllipseButton)
        self.view.addSubview(self.sharingInfoLabel)
        self.view.addSubview(self.invitationButton)
        ellipleBarController?.canShowBottomBar = false
        
        Answers.logCustomEvent(withName: "Welcome screen shown", customAttributes: nil)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        ellipleBarController?.hideBottomBar()
    }
    
    override func backAction() {
        super.backAction()
        ellipleBarController?.canShowBottomBar = true
        ellipleBarController?.showBottomBar()
    }
    
    func yesButtonPressed() {
        let lotevc = SLLockOnboardingTouchEllipseViewController()        
        navigationController?.pushViewController(lotevc, animated: true)
    }
    
    func invitationButtonPressed() {
        let lvc = SLInvitationCodeViewController()
        navigationController?.pushViewController(lvc, animated: true)
    }
}

extension WelcomViewController: BottomBarPresentable {}

