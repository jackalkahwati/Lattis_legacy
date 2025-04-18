//
//  SLConcentricCirclesViewController.swift
//  Skylock
//
//  Created by Andre Green on 5/30/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

enum SLConcentricCirclesViewControllerAction {
    case dismiss
    case showSuccessVC
}

class SLConcentricCirclesViewController: SLBaseViewController {
    let interval:Double = 2.3
    
    var circleIndex:Int = 0
    
    let startDiameter:CGFloat = 10.0
    
    var finalDiamter:CGFloat = 0.0
    
    var initialCircleFrame:CGRect = CGRect.zero
    
    let numberOfCircles:Int = 5
    
    let xPadding:CGFloat = 35.0
    
    var viewHasAppeard:Bool {
        return isViewLoaded && view.window != nil
    }
    
    var lockConnectionErrorClosure:(() -> ())?
    
    let action:SLConcentricCirclesViewControllerAction
    
    lazy var connectingEllipseLabel:UILabel = {
        let frame = CGRect(
            x: 0,
            y: UIApplication.shared.statusBarFrame.size.height +
                (self.navigationController?.navigationBar.bounds.size.height)! + 5.0,
            width: self.view.bounds.size.width,
            height: 20
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.text = NSLocalizedString("Connecting...", comment: "")
        label.textAlignment = NSTextAlignment.center
        label.textColor = UIColor(red: 76, green: 79, blue: 97)
        label.font = UIFont.systemFont(ofSize: 16)
        label.numberOfLines = 0
        
        return label
    }()
    
    lazy var getHelpButton:UIButton = {
        let width:CGFloat = self.view.bounds.size.width - 2*self.xPadding
        let frame = CGRect(
            x: 0.5*(self.view.bounds.size.width - width),
            y: self.view.bounds.size.height - 50.0,
            width: width,
            height: 16
        )
        
        let button:UIButton = UIButton(frame: frame)
        button.addTarget(self, action: #selector(getHelpButtonPressed), for: .touchDown)
        button.setTitle(NSLocalizedString("Get help", comment: ""), for: .normal)
        button.setTitleColor(UIColor(red: 87, green: 216, blue: 255), for: .normal)
        button.titleLabel?.font = UIFont(name: SLFont.MonserratBold.rawValue, size: 14.0)
        return button
    }()
    
    lazy var makeSureLabel:UILabel = {
        let labelWidth = self.view.bounds.size.width - 2*self.xPadding
        let utility = SLUtilities()
        let font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 14.0)!
        let text = NSLocalizedString(
            "Make sure your Ellipse is switched on and within range of your phone.",
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
            y: self.getHelpButton.frame.minY - labelSize.height - 50.0,
            width: labelWidth,
            height: labelSize.height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = UIColor(red: 130, green: 156, blue: 178)
        label.textAlignment = NSTextAlignment.center
        label.font = font
        label.numberOfLines = 0
        label.text = text
        
        return label
    }()
    
    init(action: SLConcentricCirclesViewControllerAction) {
        self.action = action
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        addBackButton()
        self.view.backgroundColor = UIColor.white
        title = NSLocalizedString("CONNECTING ELLIPSE...", comment: "")
        
        self.initialCircleFrame = CGRect(
            x: 0.5*(self.view.bounds.size.width - self.startDiameter),
            y: 0.5*(self.view.bounds.size.height - self.startDiameter),
            width: self.startDiameter,
            height: self.startDiameter
        )
        
        self.finalDiamter = pow(pow(self.view.bounds.size.width, 2) + pow(self.view.bounds.size.height, 2), 0.5)
        
        self.view.addSubview(self.getHelpButton)
        self.view.addSubview(self.makeSureLabel)
        
        self.run()
        
        NotificationCenter.default.addObserver(self, selector: #selector(connectedLock(notificaiton:)), name: NSNotification.Name(rawValue: kSLNotificationLockPaired), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(lockConnectionError(notification:)), name: NSNotification.Name(rawValue: kSLNotificationLockManagerErrorConnectingLock), object: nil)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
        if let connectionClosure = self.lockConnectionErrorClosure {
            connectionClosure()
        }
    }
    
    func run() {
        Timer.scheduledTimer(
            timeInterval: self.interval/Double(self.numberOfCircles),
            target: self,
            selector: #selector(timerFired),
            userInfo: nil,
            repeats: true
        )
    }
    
    func timerFired() {
        let circleView = self.createCircleView()
        self.view.addSubview(circleView)
        self.bringTopViewsToFront()
        self.makeAnimation(circleView: circleView)
    }
    
    func makeAnimation(circleView: UIView) {
        let scale:CGFloat = self.finalDiamter/circleView.frame.size.width
        UIView.animate(
            withDuration: self.interval,
            delay: 0.0,
            options: UIViewAnimationOptions.curveLinear,
            animations: {
                circleView.transform = circleView.transform.scaledBy(x: scale, y: scale)
                circleView.alpha = 0.0
        }) { (finished) in
            circleView.removeFromSuperview()
        }
    }
    
    func createCircleView() -> UIView {
        let circle:UIView = UIView(frame: self.initialCircleFrame)
        circle.backgroundColor = UIColor(red: 160, green: 200, blue: 224).withAlphaComponent(0.5)
        circle.layer.cornerRadius = 0.5*self.initialCircleFrame.size.width
        
        return circle
    }
    
    func getHelpButtonPressed() {
        let webView = SLWebViewController(baseUrl: .Help)
        self.navigationController == nil ? self.present(webView, animated: true, completion: nil)
            : self.navigationController!.pushViewController(webView, animated: true)
        
    }
    
    func bringTopViewsToFront() {
        self.view.bringSubview(toFront: self.connectingEllipseLabel)
        self.view.bringSubview(toFront: self.getHelpButton)
        self.view.bringSubview(toFront: self.makeSureLabel)
        if self.warningBackgroundView != nil {
            self.view.bringSubview(toFront: self.warningBackgroundView!)
        }
        if self.warningViewController != nil {
            self.view.bringSubview(toFront: self.warningViewController!.view)
        }
    }
    
    func connectedLock(notificaiton: Notification) {
        guard let lock = notificaiton.object as? SLLock else { return }
        switch self.action {
        case .dismiss:
            self.dismiss(animated: true, completion: nil)
        case .showSuccessVC:
            let psvc = SLPairingSuccessViewController(lockId: lock.lockId)
            self.navigationController == nil ? self.present(psvc, animated: true, completion: nil) :
                self.navigationController?.pushViewController(psvc, animated: true)
        }
    }
    
    func lockConnectionError(notification: Notification) {
        guard let notificationObject = notification.object as? [String: Any?] else {
            print("no connection error in notification for method: lockConnectionError")
            return
        }
        
        guard let info = notificationObject["message"] as? String else {
            print("no connection error messsage in notification for method: lockConnectionError")
            return
        }
        let header = (notificationObject["header"] as? String) ?? "FAILED TO CONNECT".localized()
        let texts:[SLWarningViewControllerTextProperty:String?] = [
            .Header: header,
            .Info: info,
            .CancelButton: NSLocalizedString("OK", comment: ""),
            .ActionButton: nil
        ]
        
        if self.viewHasAppeard {
            self.presentWarningViewControllerWithTexts(texts: texts, cancelClosure: {
                if let navController = self.navigationController {
                    navController.popViewController(animated: true)
                } else {
                    self.dismiss(animated: true, completion: nil)
                }
            })
        } else {
            self.lockConnectionErrorClosure = { [weak self] in
                if let weakSelf = self {
                    weakSelf.presentWarningViewControllerWithTexts(texts: texts, cancelClosure: {
                        if let navController = weakSelf.navigationController {
                            navController.popViewController(animated: true)
                        } else {
                            weakSelf.dismiss(animated: true, completion: nil)
                        }
                    })
                }
            }
        }
    }
}
