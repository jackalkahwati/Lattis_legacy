//
//  SLTouchPadViewController.swift
//  Skylock
//
//  Created by Andre Green on 8/30/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

import UIKit
import Crashlytics
import Localize_Swift
import RestService

protocol SLTouchPadViewControllerDelegate:class {
    func touchPadViewControllerWantsExit(touchPadViewController: SLTouchPadViewController)
}

class SLTouchPadViewController: SLBaseViewController, SLTouchPadViewDelegate {
    let xPadding:CGFloat = 25.0
    
    let minimumCodeNumber:Int = 4
    
    let maximunCodeNumber: Int = 8
    
    weak var delegate: SLTouchPadViewControllerDelegate?
    
    var letterIndex:Int = 0
    
    var pinCode: [Oval.Locks.Pin] = []
    
    var onSaveExit:(() -> Void)?
    
    var onCanelExit:(() -> Void)?
    
    var arrowInputViews:[UIView] = [UIView]()
    
    var arrowButtonSize:CGSize = CGSize.zero
    
    let arrowViewSpacer:CGFloat = 3.0
    
    // TODO: - remove this logic when we don't need initial force update anymore
    private var currentVersion: String?
    private var latestVersion: String?
    
    private let locksService = LocksService()
    
    private let ovalImage: UIImageView = {
        let image = UIImage(named: "pin_oval")!
        let imageView = UIImageView(image: image)
        imageView.frame = CGRect(x: 0, y: 0, width: image.size.width, height: image.size.height)
        return imageView
    }()
    
    lazy var xExitButton:UIButton = {
        let image:UIImage = UIImage(named: "button_close_window_large_Onboarding")!
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
            action: #selector(xExitButtonPressed),
            for: .touchDown
        )
        
        return button
    }()
    
    lazy var subInfoLabel: UILabel = {
        let font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 14.0)!
        let text = "Note: When using Ellipse, enter your PIN code, then press the center button.".localized()
        let utility = SLUtilities()
        let size: CGSize = utility.sizeForLabel(
            font: font,
            text:text,
            maxWidth:self.view.bounds.size.width - 2*self.xPadding,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        
        let frame = CGRect(
            x: self.xPadding,
            y: self.underlineView.frame.maxY + 15.0,
            width: size.width,
            height: size.height
        )
        
        let label: UILabel = UILabel(frame: frame)
        label.text = text
        label.textColor = .slRobinsEgg
        label.font = font
        label.numberOfLines = 0
        label.textAlignment = .center
        
        return label
    }()
    
    lazy var pinEntryView:UIView = {
        let width = CGFloat(self.maximunCodeNumber) * (self.arrowButtonSize.width + self.arrowViewSpacer)
        let frame = CGRect(
            x: 0.5*(self.view.bounds.size.width - width),
            y: SLUtilities().statusBarAndNavControllerHeight(viewController: self) + 40.0,
            width: width,
            height: 38.0
        )
        
        let view: UIView = UIView(frame: frame)
        
        return view
    }()
    
    lazy var deleteButton:UIButton = {
        let image:UIImage = UIImage(named: "button_backspace_Onboarding")!
        let frame = CGRect(
            x: self.pinEntryView.frame.maxX + 5.0,
            y: self.pinEntryView.frame.midY - 0.5*image.size.height,
            width: image.size.width,
            height: image.size.height
        )
        
        let button:UIButton = UIButton(frame: frame)
        button.addTarget(self, action: #selector(deleteButtonPressed), for: .touchDown)
        button.setImage(image, for: .normal)
        button.isHidden = true
        
        return button
    }()
    
    lazy var underlineView:UIView = {
        let frame = CGRect(
            x: self.pinEntryView.frame.minX,
            y: self.pinEntryView.frame.maxY + 3.0,
            width: self.pinEntryView.bounds.size.width,
            height: 1
        )
        
        let view:UIView = UIView(frame: frame)
        view.backgroundColor = UIColor(red: 151, green: 151, blue: 151)
        
        return view
    }()
    
    lazy var savePinButton: UIButton = {
        let padding:CGFloat = 15.0
        let height:CGFloat = 44.0
        let frame = CGRect(
            x: padding,
            y: self.view.bounds.size.height - height - 10.0,
            width: self.view.bounds.size.width - 2.0*padding,
            height: height
        )
        
        let button: UIButton = UIButton(type: .system)
        button.frame = frame
        button.addTarget(self, action: #selector(savePinButtonPressed), for: UIControlEvents.touchDown)
        button.setTitle(NSLocalizedString("SAVE PIN", comment: ""), for: .normal)
        button.setTitleColor(UIColor.white, for: .normal)
        button.setTitleColor(UIColor.color(188, green: 187, blue: 187), for: .disabled)
        button.backgroundColor = UIColor(red: 231, green: 231, blue: 233)
        button.isEnabled = false
        button.layer.cornerRadius = 3
        return button
    }()
    
    lazy var touchPadView: SLTouchPadView = {
        let padView = SLTouchPadView()
        padView.delegate = self
        return padView
    }()
    
    private let macId: String
    private let isOnboarding: Bool
    
    init(macId: String, isOnboarding: Bool) {
        self.macId = macId
        self.isOnboarding = isOnboarding
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
        
        view.backgroundColor = .white
        title = "ENTER PIN CODE".localized()
        if isOnboarding {
            navigationItem.leftBarButtonItem = nil
        } else {
            addBackButton()
        }
        
        
        let arrowImage = UIImage(named: "pin_arrow_blue_up")!
        arrowButtonSize = arrowImage.size
        
        view.addSubview(xExitButton)
        view.addSubview(deleteButton)
        view.addSubview(subInfoLabel)
        view.addSubview(pinEntryView)
        view.addSubview(underlineView)
        view.addSubview(savePinButton)
        
        view.addSubview(ovalImage)
        view.addSubview(touchPadView)
        
        ovalImage.frame = {
            var frame = ovalImage.frame
            frame.origin.y = subInfoLabel.frame.maxY + 30
            frame.origin.x = view.bounds.midX - frame.width*0.5
            return frame
        }()
        
        touchPadView.frame = {
            var frame = ovalImage.frame.insetBy(dx: 15, dy: 15)
            frame.size.height = frame.size.width
            return frame
        }()
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(lockCodeWritten),
            name: NSNotification.Name(rawValue: kSLNotificationLockSequenceWritten),
            object: nil
        )
        
        checkFirmWare()
    }
    
    func savePinButtonPressed() {
        locksService.save(pinCode: pinCode, forLockWithMacId: macId)
    }
    
    func xExitButtonPressed() {
        if self.onCanelExit != nil {
            self.onCanelExit!()
        }
    }
    
    func deleteButtonPressed() {
        if self.arrowInputViews.isEmpty {
            return
        }
        
        let lastView = self.arrowInputViews.popLast()!
        lastView.removeFromSuperview()
        
        pinCode.removeLast()
        
        if pinCode.count == 0 {
            self.deleteButton.isHidden = true
        }
        
        if self.savePinButton.isEnabled && pinCode.count < self.minimumCodeNumber {
            self.savePinButton.isEnabled = false
            self.savePinButton.backgroundColor = UIColor(red: 231, green: 231, blue: 233)
        }
    }
    
    func lockCodeWritten() {
        Answers.logCustomEvent(withName: "Pincode changed", customAttributes: nil)
        forceUpdate()
    }
    
    // TODO: - remove this logic when we don't need initial force update anymore
    func forceUpdate() {
        guard let current = currentVersion,
            let latest = latestVersion,
            current < latest else {
            onSaveExit?()
            return
        }
        
        let fwController = SLFirmwareUpdateViewController(firmwareVersionString: current)
        fwController.isForceUpdate = true
        navigationController?.pushViewController(fwController, animated: true)
    }
    
    func checkFirmWare() {
        NotificationCenter.default.addObserver(self, selector: #selector(firmwareRead(notification:)), name: NSNotification.Name(rawValue: kSLNotificationLockManagerReadFirmwareVersion), object: nil)
        SLLockManager.sharedManager.readFirmwareDataForCurrentLock()
        Oval.locks.firmvareVersions(success: { [weak self] (versions) in
            let trimming = CharacterSet(charactersIn: "0")
            let ver = versions.map({ $0.trimmingCharacters(in: trimming) }).sorted(by: <)
            self?.latestVersion = ver.last
            }, fail: { error in
                
        })
    }
    
    func firmwareRead(notification: Notification) {
        guard let firmware = notification.object as? String else {
            return
        }
        currentVersion = firmware
    }
    
    func addImageToPinEntryView(imageName: String) {
        guard let image:UIImage = UIImage(named: imageName) else {
            return
        }
        
        let x0 = self.arrowInputViews.isEmpty ? 0.0 :
            CGFloat(self.arrowInputViews.count) * (arrowButtonSize.width + self.arrowViewSpacer)
        let frame = CGRect(
            x: x0,
            y: 0.5*(self.pinEntryView.bounds.size.height - self.arrowButtonSize.height),
            width: arrowButtonSize.width,
            height: arrowButtonSize.height
        )
        
        let view:UIView = UIView(frame: frame)
        
        let imageFrame = CGRect(
            x: 0.5*(view.bounds.size.width - image.size.width),
            y: 0.5*(view.bounds.size.height - image.size.height),
            width: image.size.width,
            height: image.size.height
        )
        
        let imageView:UIImageView = UIImageView(frame: imageFrame)
        imageView.image = image
        
        view.addSubview(imageView)
        self.pinEntryView.addSubview(view)
        
        self.arrowInputViews.append(view)
    }
    
    //MARK: SLTouchPadViewDelegate methods
    func touchPadViewLocationSelected(_ touchPadViewController: SLTouchPadView, location: SLTouchPadLocation) {
        guard pinCode.count < maximunCodeNumber else { return }
        
        self.addImageToPinEntryView(imageName: location.imageName)
        pinCode.append(location.pinValue)
        self.deleteButton.isHidden = false
        if !self.savePinButton.isEnabled  && self.arrowInputViews.count >= self.minimumCodeNumber {
            self.savePinButton.isEnabled = true
            self.savePinButton.backgroundColor = .slLightBlueGreyTwo
        }
    }
}

extension SLTouchPadLocation {
    var imageName: String {
        switch self {
        case .top:
            return "pin_arrow_blue_up"
        case .right:
            return "pin_arrow_blue_right"
        case .bottom:
            return "pin_arrow_blue_down"
        case .left:
            return "pin_arrow_blue_left"
        }
    }
    
    var pinValue: Oval.Locks.Pin {
        switch self {
        case .top:
            return .up
        case .right:
            return .right
        case .bottom:
            return .down
        case .left:
            return .left
        }
    }
}
