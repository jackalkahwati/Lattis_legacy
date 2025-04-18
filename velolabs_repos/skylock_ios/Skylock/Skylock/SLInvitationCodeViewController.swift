//
//  SLInvitationCodeViewController.swift
// Ellipse
//
//  Created by Ranjitha on 12/29/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit
import TPKeyboardAvoiding
import Localize_Swift
import SwiftyTimer
import RestService
import SwiftyJSON
import Crashlytics

class SLInvitationCodeViewController: SLBaseViewController,UITextFieldDelegate{
    let xPadding: CGFloat = 15.0
    var backview:UIView!
    var alertView:UIView!
    var shareView: UIView!
    var closeButton:UIButton!
    var userImage:UIImageView!
    private var sharedLock: SLLock?
    fileprivate var timeoutTimer: Timer?
    private let locksService = LocksService()
    
    let labelTextColor = UIColor.slSteel
    
    lazy var getStartedLabel:UILabel = {
        let frame = CGRect(x: 0.0, y: 110.0, width: self.view.bounds.size.width, height: 22.0)
        let label:UILabel = UILabel(frame: frame)
        label.text = "Enter your invitation code".localized()
        label.textColor = UIColor(red: 87, green: 216, blue: 255)
        label.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 18.0)
        label.textAlignment = .center
        
        return label
    }()
    
    lazy var connectEllipseLabel:UILabel = {
        let labelWidth = self.view.bounds.size.width - 2*self.xPadding
        let utility = SLUtilities()
        let font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 16.0)!
        let text = "If a friend has invited you to share\ntheir Ellipse, you should have been\nsent an SMS with an invitation code.\nEnter this code now".localized()

        let labelSize:CGSize = utility.sizeForLabel(
            font: font,
            text: text,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        let frame = CGRect(x: 0.5*(self.view.bounds.size.width - labelSize.width),y: self.getStartedLabel.frame.maxY + 26.0,
                           width: labelSize.width,
                           height: labelSize.height)
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = self.labelTextColor
        label.text = text
        label.textAlignment = .center
        label.font = font
        label.numberOfLines = 0
        
        return label
    }()
    
    lazy var codeEntryField:UITextField = {
        
        let xPadding:CGFloat = 40.0
        let labelWidth = self.view.bounds.size.width - 2*xPadding
        let utility = SLUtilities()
        let font = UIFont.systemFont(ofSize: 28)
        let height:CGFloat = 45.0
        let frame = CGRect(
            x: xPadding,
            y: self.connectEllipseLabel.frame.maxY + 20,
            width: self.view.bounds.size.width - 2.0*xPadding,
            height: height
        )
        let toolBarFrame = CGRect(x: 0, y: 0, width: self.view.bounds.size.width, height: 45.0)
        let numberToolbar:UIToolbar = UIToolbar(frame: toolBarFrame)
        numberToolbar.barStyle = UIBarStyle.default
        numberToolbar.items = [
            UIBarButtonItem(
                title: NSLocalizedString("Done", comment: ""),
                style: UIBarButtonItemStyle.plain,
                target: self,
                action: #selector(doneButtonPressed)
            )
        ]
        numberToolbar.sizeToFit()
        
        let field:UITextField = UITextField(frame: frame)
        field.delegate = self
        field.textColor = UIColor(red: 102, green: 177, blue: 227)
        field.layer.borderWidth = 1
        field.layer.borderColor = UIColor.slRobinsEgg.cgColor
        field.textAlignment = .center
        field.font = font
        field.inputAccessoryView = numberToolbar
        field.attributedPlaceholder = NSAttributedString(
            string: NSLocalizedString(" ", comment: ""),
            attributes: [NSForegroundColorAttributeName : UIColor(red: 102, green: 177, blue: 227)]
        )
        field.keyboardType = .numberPad
        
        return field
    }()
    
    lazy var setUpEllipseButton:UIButton = {
        let xPadding:CGFloat = 40.0
        let frame = CGRect(
            x: xPadding,
            y: self.codeEntryField.frame.maxY + 20,
            width: self.view.bounds.size.width - 2.0*xPadding,
            height: 44.0
        )
        
        let button:UIButton = UIButton(type: .system)
        button.layer.cornerRadius = 3
        button.frame = frame
        button.setTitle(NSLocalizedString("CONNECT NOW", comment: ""), for: .normal)
        button.setTitleColor(UIColor.white, for: .normal)
        button.titleLabel?.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 12.0)
        button.backgroundColor = UIColor(red: 87, green: 216, blue: 255)
        button.addTarget(self, action: #selector(yesButtonPressed), for: .touchUpInside)
        
        return button
    }()
    
    func doneButtonPressed() {
        self.codeEntryField.resignFirstResponder()
    }
    
    lazy var exitButton:UIButton = {
        let image:UIImage = UIImage(named: "close_icon")!
        let frame:CGRect = CGRect(
            x: self.view.bounds.size.width - image.size.width - 10.0,
            y: UIApplication.shared.statusBarFrame.size.height + 10.0,
            width: image.size.width,
            height: image.size.height
        )
        let button:UIButton = UIButton(frame: frame)
        button.setImage(image, for: UIControlState.normal)
        button.addTarget(
            self,
            action: #selector(exitButtonPressed),
            for: .touchDown
        )
        
        return button
    }()
    
    private let scrollView = TPKeyboardAvoidingScrollView()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.navigationItem.title = NSLocalizedString("WELCOME ON BOARD :)", comment: "")
        self.navigationItem.backBarButtonItem = UIBarButtonItem(title: "", style: .plain, target: nil, action: nil)
        
        self.view.addSubview(scrollView)
        scrollView.translatesAutoresizingMaskIntoConstraints = false
        scrollView.constrainEdges(to: view)
        
        self.scrollView.backgroundColor = UIColor.white
        self.scrollView.addSubview(getStartedLabel)
        self.scrollView.addSubview(self.connectEllipseLabel)
        self.scrollView.addSubview(self.codeEntryField)
        self.scrollView.addSubview(self.setUpEllipseButton)
        //Check and hide this
        self.scrollView.addSubview(self.setUpEllipseButton)
        self.scrollView.addSubview(self.exitButton)
        
        NotificationCenter.default.addObserver(self, selector: #selector(foundLock(notification:)), name: NSNotification.Name(rawValue: kSLNotificationLockManagerDiscoverdLock), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(lockConnectionError(notification:)), name: NSNotification.Name(rawValue: kSLNotificationLockManagerErrorConnectingLock), object: nil)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController?.isNavigationBarHidden = true
        codeEntryField.becomeFirstResponder()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        navigationController?.isNavigationBarHidden = false
    }

    func alertInvalideCodeView() {
        DispatchQueue.main.async() {
            self.backview = UIView()
            self.backview.frame = self.view.bounds
            self.backview?.backgroundColor = UIColor.slNavy.withAlphaComponent(0.77)
            
            self.alertView = UIView()
            self.alertView.layer.cornerRadius = 3
            self.alertView.frame = self.backview.bounds.insetBy(dx: 20, dy: 0)

            self.alertView?.backgroundColor = UIColor.white
            self.view.addSubview(self.backview!)
            self.backview.addSubview(self.alertView )
            self.alertView.addSubview(self.getTitleLabel)
            self.alertView.addSubview(self.messageLabel)
            self.alertView.addSubview(self.tryAgainButton)
            
            self.alertView.frame = {
                var frame = self.backview.bounds.insetBy(dx: 20, dy: 0)
                frame.size.height = self.tryAgainButton.frame.maxY + 35
                frame.origin.y = self.backview.frame.midY - frame.height*0.5
                return frame
            }()
        }
    }
    
    func exitButtonPressed() {
        if (self.codeEntryField.isFirstResponder) {
            self.codeEntryField.resignFirstResponder()
        } else {
            _ = navigationController?.popViewController(animated: true)
        }
    }
    
    func alertSuccessView(lockName: String?) {
        DispatchQueue.main.async() {
            self.backview = UIView()
            self.backview.frame = self.view.bounds
            self.backview?.backgroundColor = UIColor(white: 0.2, alpha: 0.75)
            
            self.alertView = UIView()
            self.alertView.frame = self.backview.bounds.insetBy(dx: 20, dy: 0)
            
            self.alertView?.backgroundColor = UIColor.white
            self.view.addSubview(self.backview!)
            
            self.backview.addSubview(self.alertView )
            self.alertView.addSubview(self.getSuccessTitleLabel)
            self.alertView.addSubview(self.getSuccessMessageLabel(lockName: lockName))
            self.alertView.addSubview(self.connectButton)
            self.alertView.addSubview(self.connectLaterButton)
            self.alertView.frame = {
                var frame = self.backview.bounds.insetBy(dx: 20, dy: 0)
                frame.size.height = self.connectLaterButton.frame.maxY + 35
                frame.origin.y = self.backview.frame.midY - frame.height*0.5
                return frame
            }()
        }
    }
    
    func yesButtonPressed() {
        view.endEditing(true)
        guard let code = codeEntryField.text, code.isEmpty == false else { return print("Submit code is empty") }
        presentLoadingViewWithMessage(message: "Submitting SMS code...".localized())
        locksService.acceptSharing(confirmationCode: code, success: { [weak self] (lock) in
            self?.process(lock: lock)
            Answers.logShare(withMethod: "Lock borrowed", contentName: nil, contentType: nil, contentId: nil, customAttributes: nil)
        }, fail: { [weak self] in
            self?.dismissLoadingViewWithCompletion(completion: {
                self?.alertInvalideCodeView()
            })
        })
    }
    
    private func process(lock: SLLock) {
        sharedLock = lock
        connectNowBtnAction()
    }
    
    func invitationButtonPressed() {
        let lvc = SLLockViewController()
        self.present(lvc, animated: true, completion: nil)
    }
    
    // MArk: alert
    lazy var getTitleLabel:UILabel = {
        let frame = CGRect(x: 0.0, y: 50.0, width: self.alertView.bounds.size.width, height: 22.0)
        let label:UILabel = UILabel(frame: frame)
        label.text = NSLocalizedString("Invalid code", comment: "")
        label.textColor = UIColor(white: 0.0/255.0, alpha: 1.0)
        label.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 18.0)
        label.textAlignment = .center
        
        return label
    }()
    
    lazy var messageLabel:UILabel = {
        let labelWidth = self.alertView.bounds.size.width - 2*self.xPadding
        let utility = SLUtilities()
        let font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 14.0)!
        let text = "Please check your invitation code and try again. If you have not received an invitation, ask the Ellipse owner to try again.".localized()
        let labelSize:CGSize = utility.sizeForLabel(
            font: font,
            text: text,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        let frame = CGRect(x: 0.5*(self.alertView.bounds.size.width - labelSize.width),
                           y: self.getTitleLabel.frame.maxY + 26.0,
                           width: labelSize.width,
                           height:labelSize.height)
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = self.labelTextColor
        label.text = text
        label.textAlignment = .center
        label.font = font
        label.numberOfLines = 0
        
        return label
    }()
    
    lazy var tryAgainButton:UIButton = {
        
        let frame = CGRect(
            x: self.xPadding,
            y: self.messageLabel.frame.maxY + 20,
            width: self.alertView.bounds.size.width - 2.0*self.xPadding,
            height: 44.0
        )
        
        let button:UIButton = UIButton(type: .system)
        button.layer.cornerRadius = 3
        button.frame = frame
        button.setTitle(NSLocalizedString("TRY AGAIN", comment: ""), for: .normal)
        button.setTitleColor(UIColor.white, for: .normal)
        button.titleLabel?.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 12.0)
        button.backgroundColor = UIColor(red: 87, green: 216, blue: 255)
        button.addTarget(self, action: #selector(tryAgainBtn), for: .touchUpInside)
        
        return button
    }()
    
    func tryAgainBtn() {
        self.backview.isHidden = true
        self.backview.removeFromSuperview()
        codeEntryField.becomeFirstResponder()
    }
    
    
    // MArk: alert Successs
    
    lazy var getSuccessTitleLabel:UILabel = {
        let frame = CGRect(x: 0.0, y: 50.0, width: self.alertView.bounds.size.width, height: 22.0)
        let label:UILabel = UILabel(frame: frame)
        label.text = NSLocalizedString("Success", comment: "")
        label.textColor = .slWarmGreyTwo
        label.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 18.0)
        label.textAlignment = .center
        
        return label
    }()
    
    private var successMessageLabel: UILabel!
    private func getSuccessMessageLabel(lockName: String? = nil) -> UILabel {
        let lock = lockName ?? NSLocalizedString("Undefined", comment: "")
        let labelWidth = self.alertView.bounds.size.width - 2*self.xPadding
        let utility = SLUtilities()
        let font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 14.0)!
        let text = NSLocalizedString(
            "You've accepted the invitation and\ncan beign sharing \(lock)\nEllipse as soon as you are within\nrange of it",
            comment: ""
        )
        
        let labelSize:CGSize = utility.sizeForLabel(
            font: font,
            text: text,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        let frame = CGRect(x: 0.5*(self.view.bounds.size.width - 40 - labelSize.width),
                           y: self.getSuccessTitleLabel.frame.maxY + 26.0,
                           width: labelSize.width,
                           height: labelSize.height)
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = self.labelTextColor
        label.text = text
        label.textAlignment = .center
        label.font = font
        label.numberOfLines = 0
        
        successMessageLabel = label
        
        return label
    }
    
    lazy var connectButton:UIButton = {
        let frame = CGRect(
            x: self.xPadding,
            y: self.successMessageLabel.frame.maxY + 20,
            width: self.alertView.bounds.size.width - 2.0*self.xPadding,
            height: 44.0
        )
        
        let button:UIButton = UIButton(type: .system)
        button.layer.cornerRadius = 3
        button.frame = frame
        button.setTitle(NSLocalizedString("CONNECT NOW", comment: ""), for: .normal)
        button.setTitleColor(UIColor.white, for: .normal)
        button.titleLabel?.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 12.0)
        button.backgroundColor = UIColor(red: 87, green: 216, blue: 255)
        button.addTarget(self, action: #selector(connectNowBtnAction), for: .touchDown)
        
        return button
    }()
    
    lazy var connectLaterButton:UIButton = {
        
        let frame = CGRect(
            x: self.xPadding,
            y: self.connectButton.frame.maxY + 20,
            width: self.alertView.bounds.size.width - 2.0*self.xPadding,
            height: 44.0
        )
        
        let button:UIButton = UIButton(type: .system)
        button.layer.cornerRadius = 3
        button.frame = frame
        button.setTitle(NSLocalizedString("CONNECT LATER", comment: ""), for: .normal)
        button.setTitleColor(.slWarmGreyThree, for: .normal)
        button.titleLabel?.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 12.0)
        button.backgroundColor = .slPaleGrey
        button.addTarget(self, action: #selector(connectLaterAction), for: .touchDown)
        
        return button
    }()
    
    func connectNowBtnAction() {
        loadingView?.setMessage(message: String(format: "Searching %@".localized(), sharedLock?.displayName ?? ""))
        SLLockManager.sharedManager.startActiveSearch()
        timeoutTimer = Timer.after(30.seconds) { [weak self] in
            SLLockManager.sharedManager.endActiveSearch()
            self?.dismissLoadingViewWithCompletion(completion: { 
                self?.presentWarningViewControllerWithTexts(texts: [.Header: "Lock not found".localized(), .Info: "Make sure you have lock arround you".localized(), .CancelButton: "OK".localized()], cancelClosure: {
                    self?.dismiss(animated: true, completion: nil)
                })
            })
        }
    }
    
    func connectLaterAction() {
        self.backview.isHidden = true
        self.backview.removeFromSuperview()
        dismiss(animated: true, completion: nil)
    }
    
    @objc fileprivate func foundLock(notification: Notification) {
        guard let lock = notification.object as? SLLock,
        let sharedLock = sharedLock  else {
            print("Error: found lock but it was not included in notification")
            return
        }
        
        if lock.macId == sharedLock.macId {
            timeoutTimer?.invalidate()
            SLLockManager.sharedManager.connectToLockWithMacAddress(macAddress: lock.macId!)
            loadingView?.setMessage(message: String(format: "Connecting to %@...".localized(), lock.displayName))
            NotificationCenter.default.post(name: hideMenuNotification, object: nil)
            dismiss(animated: true, completion: {
                NotificationCenter.default.post(name: NSNotification.Name(rawValue: kSLNotificationLockManagerStartedConnectingLock), object: nil)
            })
        }
    }
    
    func lockConnectionError(notification: Notification) {
        timeoutTimer?.invalidate()
        var info: String?
        if let notificationObject = notification.object as? [String: Any?],
            let msg = notificationObject["message"] as? String {
            info = msg
        }
        
        presentWarningViewControllerWithTexts(texts: [
            .Header: NSLocalizedString("FAILED TO CONNECT", comment: ""),
            .Info: info,
            .CancelButton: NSLocalizedString("OK", comment: "")
            ], cancelClosure: {
                self.dismiss(animated: true, completion: nil)
        })
    }
}
