//
//  SLAcceptNotificationsViewController.swift
//  Skylock
//
//  Created by Andre Green on 6/4/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

protocol SLAcceptNotificationsViewControllerDelegate:class {
    func userWantsToAcceptLocationUse(acceptNotificationsVC: SLAcceptNotificationsViewController)
    func userWantsToAcceptsNotifications(acceptNotificationsVC: SLAcceptNotificationsViewController)
    func acceptsNotificationsControllerWantsExit(
        acceptNotiticationViewController: SLAcceptNotificationsViewController,
        animated: Bool
    )
}

class SLAcceptNotificationsViewController: UIViewController {
    enum NotificationStep {
        case Location
        case Notifications
        case Done
    }
    
    enum UIElement {
        case BackgroundImageName
        case MainText
        case DetailText
    }

    let xPadding:CGFloat = 35.0
    weak var delegate:SLAcceptNotificationsViewControllerDelegate?
    var currentNotificationStep:NotificationStep = .Location
    
    let elements:[NotificationStep:[UIElement:String]] = [
        .Location: [
            .BackgroundImageName: "login_use_location_background",
            .DetailText: NSLocalizedString(
                "We use geo-tracking to locate your Ellipse and any shared bikes " +
                "you have access to. When we know where you are, we can show you nearby " +
                "bikes, and help you locate them with precise directions.",
                comment: ""
            ),
            .MainText: NSLocalizedString("Ellipse would like to use your location", comment: "")
        ],
        .Notifications: [
            .BackgroundImageName: "notifications_background",
            .DetailText: NSLocalizedString(
                "To help you get the most out of your Ellipse, we need your permission " +
                "to send you notifications, such as in the event of a theft or crash, or if " +
                "someone wants to share your bike.",
                comment: ""
            ),
            .MainText: NSLocalizedString(
                "Ellipse would like to send you notifications including theft and crash alerts.",
                comment: ""
            )
        ]
    ]
    
    lazy var backgroundView:UIImageView = {
        let image:UIImage = UIImage(named: self.elements[.Location]![.BackgroundImageName]!)!
        
        let imageView:UIImageView = UIImageView(frame: self.view.bounds)
        imageView.image = image

        return imageView
    }()
    
    lazy var okButton:UIButton = {
        let image:UIImage = UIImage(named: "button_ok_onboarding")!
        let frame = CGRect(
            x: 0.5*(self.view.bounds.size.width - image.size.width),
            y: self.view.bounds.size.height - image.size.height - 20.0,
            width: image.size.width,
            height: image.size.height
        )
        
        let button:UIButton = UIButton(frame: frame)
        button.addTarget(
            self,
            action: #selector(okButtonPressed),
            for: .touchDown
        )
        button.setImage(image, for: .normal)
        
        return button
    }()
    
    lazy var detailLabel:UILabel = {
        let labelWidth = self.view.bounds.size.width - 2*self.xPadding
        let utility = SLUtilities()
        let font = UIFont.systemFont(ofSize: 12)
        let text = self.longestTextLength(element: .DetailText)
        let labelSize:CGSize = utility.sizeForLabel(
            font: font,
            text: text,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        
        let frame = CGRect(
            x: 0.5*(self.view.bounds.size.width - labelSize.width),
            y: self.okButton.frame.minY - labelSize.height - 50.0,
            width: labelSize.width,
            height: labelSize.height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = UIColor(white: 155.0/255.0, alpha: 1.0)
        label.text = self.elements[self.currentNotificationStep]![.DetailText]
        label.textAlignment = .center
        label.font = font
        label.numberOfLines = 0
        
        return label
    }()
 
    lazy var mainLabel:UILabel = {
        let labelWidth = self.view.bounds.size.width - 2*self.xPadding
        let utility = SLUtilities()
        let font = UIFont.systemFont(ofSize: 17)
        let text = self.longestTextLength(element: .MainText)
        let labelSize:CGSize = utility.sizeForLabel(
            font: font,
            text: text,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        
        let frame = CGRect(
            x: 0.5*(self.view.bounds.size.width - labelSize.width),
            y: self.detailLabel.frame.minY - labelSize.height - 25.0,
            width: labelSize.width,
            height: labelSize.height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = UIColor(red: 102, green: 177, blue: 227)
        label.text = self.elements[self.currentNotificationStep]![.MainText]
        label.textAlignment = .center
        label.font = font
        label.numberOfLines = 0
        
        return label
    }()
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = UIColor.white
        
        self.view.addSubview(self.backgroundView)
        self.view.addSubview(self.okButton)
        self.view.addSubview(self.detailLabel)
        self.view.addSubview(self.mainLabel)
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(userAccpetedNotifications),
            name: NSNotification.Name(rawValue: kSLNotificationUserAcceptedNotifications),
            object: nil
        )
    }
    
    func okButtonPressed() {
        switch self.currentNotificationStep {
        case .Location:
            self.currentNotificationStep = .Notifications
            self.delegate?.userWantsToAcceptLocationUse(acceptNotificationsVC: self)
        case .Notifications:
            self.currentNotificationStep = .Done
            self.delegate?.userWantsToAcceptsNotifications(acceptNotificationsVC: self)
        case .Done:
            self.delegate?.acceptsNotificationsControllerWantsExit(
                acceptNotiticationViewController: self,
                animated: false
            )
        }
    }
    
    func setBackgroundImageForCurrentStep() {
        if self.currentNotificationStep != .Location && self.currentNotificationStep != .Notifications {
            return
        }
        
        let image:UIImage = UIImage(
            named: self.elements[self.currentNotificationStep]![.BackgroundImageName]!
        )!
        
        let frame = CGRect(
            x: 0.5*(self.view.bounds.size.width - image.size.width),
            y: 0.5*(self.view.bounds.size.height - image.size.height),
            width: image.size.width,
            height: image.size.height
        )
        
        self.backgroundView.frame = frame
        self.backgroundView.image = image
        
        self.mainLabel.text = self.elements[self.currentNotificationStep]![.MainText]
        self.detailLabel.text = self.elements[self.currentNotificationStep]![.DetailText]
    }
    
    func longestTextLength(element: UIElement) -> String {
        var longestText:String = ""
        for elementsDict in self.elements.values {
            if let text = elementsDict[element], longestText.characters.count < text.characters.count {
                longestText = text
            }
        }
        
        return longestText
    }
    
    func userAccpetedNotifications() {
        self.delegate?.acceptsNotificationsControllerWantsExit(acceptNotiticationViewController: self, animated: true)
    }
}
