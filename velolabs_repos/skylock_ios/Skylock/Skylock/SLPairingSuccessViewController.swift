//
//  SLParingSuccessViewController.swift
//  Skylock
//
//  Created by Andre Green on 6/1/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit
import Localize_Swift
import Crashlytics

class SLPairingSuccessViewController: SLBaseViewController, UITextFieldDelegate {
    let xPadding:CGFloat = 30.0
    
    let lightBlueColor = UIColor(red: 102, green: 177, blue: 227)
    
    let buttonSeperation:CGFloat = 20.0
    
    weak var ellipleBarController: BottomBarPresenting? {
        didSet {
            ellipleBarController?.canShowBottomBar = false
            ellipleBarController?.hideBottomBar()
        }
    }
    
    private let locksService = LocksService()
    
    lazy var dismissKeyboardButton:UIButton = {
        let image:UIImage = UIImage(named: "button_close_window_extra_large_Onboarding")!
        let frame:CGRect = CGRect(
            x: self.view.bounds.size.width - image.size.width - 10.0,
            y: self.successLabel.frame.minY - image.size.height - 5.0,
            width: image.size.width,
            height: image.size.height
        )
        let button:UIButton = UIButton(frame: frame)
        button.setImage(image, for: UIControlState.normal)
        button.addTarget(
            self,
            action: #selector(dismissKeyboardButtonPressed),
            for: .touchDown
        )
        button.isHidden = true
        
        return button
    }()
    
    lazy var successLabel:UILabel = {
        let labelWidth = self.view.bounds.size.width - 4*self.xPadding
        let utility = SLUtilities()
        let font = UIFont.systemFont(ofSize: 24)
        let text = "Success, your Ellipse has been paired.".localized()
        let labelSize:CGSize = utility.sizeForLabel(
            font: font,
            text: text,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 2
        )
        
        let frame = CGRect(
            x: self.xPadding*2,
            y: 100.0,
            width: labelWidth,
            height: labelSize.height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = .slBluegrey
        label.text = text
        label.textAlignment = NSTextAlignment.center
        label.font = font
        label.numberOfLines = 2
        
        return label
    }()
    
    lazy var chooseNameLabel:UILabel = {
        let labelWidth = self.view.bounds.size.width - 2*self.xPadding
        let utility = SLUtilities()
        let font = UIFont.systemFont(ofSize: 12)
        let text = NSLocalizedString(
            "Choose a name for your Ellipse\n(max 40 characters)",
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
            x: self.xPadding,
            y: self.successLabel.frame.maxY + 15,
            width: labelWidth,
            height: labelSize.height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = .slLightBlueGrey
        label.text = text
        label.textAlignment = NSTextAlignment.center
        label.font = font
        label.numberOfLines = 0
        
        return label
    }()
    
    lazy var nameField:UITextField = {
        let xSpacer:CGFloat = 10.0
        let frame = CGRect(
            x: xSpacer,
            y: self.chooseNameLabel.frame.maxY + 55.0,
            width: self.view.bounds.size.width - 2*xSpacer,
            height: 20
        )
        
        let field:UITextField = UITextField(frame: frame)
        field.font = UIFont.systemFont(ofSize: 18)
        field.placeholder = "Name your Ellipse.".localized()
        field.textColor = UIColor(white: 155.0/255.0, alpha: 1)
        field.textAlignment = .center
        field.delegate = self
        field.autocapitalizationType = .words
        field.returnKeyType = .done
        
        return field
    }()
    
    lazy var underlineView:UIView = {
        let frame = CGRect(
            x: self.nameField.frame.minX,
            y: self.nameField.frame.maxY + 1.0,
            width: self.nameField.bounds.size.width,
            height: 1.0
        )
        
        let view:UIView = UIView(frame: frame)
        view.backgroundColor = UIColor(white: 210.0/255.0, alpha: 1.0)
        
        return view
    }()
    
    lazy var nextButton: UIButton = {
        let frame = CGRect(
            x: 0.5*(self.view.bounds.size.width - 140),
            y: self.view.bounds.size.height - 45 - 20.0,
            width: 140,
            height: 45
        )
        
        let button = UIButton(type: .custom)
        button.backgroundColor = .slRobinsEgg
        button.frame = frame
        button.setTitle("NEXT".localized(), for: .normal)
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = UIFont.systemFont(ofSize: 12)
        button.addTarget(self, action: #selector(nextAction), for: .touchDown)
        button.layer.cornerRadius = 3
        return button
    }()
    
    lazy var setPinNowLabel:UILabel = {
        let labelWidth = self.view.bounds.size.width - 2*self.xPadding
        let utility = SLUtilities()
        let font = UIFont.systemFont(ofSize: 14)
        let text = "Let’s set up a Pin Code".localized()
        let labelSize:CGSize = utility.sizeForLabel(
            font: font,
            text: text,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        
        let frame = CGRect(
            x: self.xPadding,
            y: self.nextButton.frame.minY - 25,
            width: labelWidth,
            height: labelSize.height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = .slBluegrey
        label.text = text
        label.textAlignment = NSTextAlignment.center
        label.font = font
        label.numberOfLines = 0
        
        return label
    }()
    
    private let lockId: Int32
    
    init(lockId: Int32) {
        self.lockId = lockId
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private var autoName: String?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.navigationItem.title = NSLocalizedString("NAME YOUR ELLIPSE", comment: "")
        self.navigationItem.hidesBackButton = true
        
        self.view.backgroundColor = UIColor.white
        
        self.view.addSubview(self.successLabel)
        self.view.addSubview(self.chooseNameLabel)
        self.view.addSubview(self.nameField)
        self.view.addSubview(self.underlineView)
        self.view.addSubview(nextButton)
        self.view.addSubview(self.setPinNowLabel)
        self.view.addSubview(self.dismissKeyboardButton)
        
        navigationItem.leftBarButtonItem = nil
        
        generateName()
        
        Answers.logCustomEvent(withName: "Addlock", customAttributes: nil)
    }
    
    private func generateName() {
        if let lockName = SLDatabaseManager.shared().getLockWithLockId(lockId)?.givenName {
            autoName = lockName
            nameField.placeholder = autoName
            return
        }
        guard let user = SLDatabaseManager.shared().getCurrentUser(), let userName = user.firstName else { return }
        let lockCount = (user.locks?.count ?? 0) + 1
        autoName = String(format: "%@'s Ellipse %d", userName, lockCount)
        nameField.placeholder = autoName
    }
    
    func nextAction() {
        guard saveNewLockName() else { return }
        guard let macId = SLDatabaseManager.shared().getLockWithLockId(lockId)?.macId else { return }
        let tpvc = SLTouchPadViewController(macId: macId, isOnboarding: true)
        tpvc.onCanelExit = {
            self.dismiss(animated: true, completion: nil)
        }
        tpvc.onSaveExit = {[weak weakTpvc = tpvc] in
            weakTpvc?.dismiss(animated: true, completion: nil)
        }
        navigationController?.setViewControllers([tpvc], animated: true)
    }
    
    func noButtonPressed() {
        guard saveNewLockName() else { return }
        
        dismiss(animated: true, completion: nil)
    }
    
    func dismissKeyboardButtonPressed() {
        self.nameField.resignFirstResponder()
    }
    
    func saveNewLockName() -> Bool {
        if autoName == nil || autoName!.isEmpty {
            autoName = self.nameField.text
        }
        if let name = self.nameField.text, name.isEmpty == false {
            autoName = name
        }
        
        guard let lockName = autoName , !lockName.isEmpty else {
            let texts:[SLWarningViewControllerTextProperty:String?] = [
                .Header: "New name is not set.".localized(),
                .Info: "Name your Ellipse please.".localized(),
                .CancelButton: NSLocalizedString("OK", comment: ""),
                .ActionButton: nil
            ]
            
            self.presentWarningViewControllerWithTexts(texts: texts, cancelClosure: nil)
            return false
        }
        locksService.changeName(forLockWith: lockId, name: lockName.trimmingCharacters(in: .whitespaces))
        
        return true
    }
    
    func textFieldDidBeginEditing(_ textField: UITextField) {
        self.dismissKeyboardButton.isHidden = false
    }
    
    func textFieldDidEndEditing(_ textField: UITextField) {
        self.dismissKeyboardButton.isHidden = true
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return true
    }
}

extension SLPairingSuccessViewController: BottomBarPresentable {}
