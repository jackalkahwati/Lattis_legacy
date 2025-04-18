//
//  SLShareInviteViewcontroller.swift
//  Ellipse
//
//  Created by Ranjitha on 10/21/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit
import Localize_Swift
import Contacts
import PhoneNumberKit
import Crashlytics
import RestService

class SLShareInviteViewController: SLBaseViewController {
    private let userImageView = UIImageView(image: UIImage(named:"contacts_icon"))
    private let userNameLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 16)
        label.textAlignment = .center
        label.textColor = .slDenim
        label.lineBreakMode = .byWordWrapping
        return label
    }()
    
    private let phoneLabel: UILabel = {
        let label = UILabel()
        label.textAlignment = .center
        label.textColor = .slPinkishGreyThree
        label.lineBreakMode = .byWordWrapping
        label.font = .systemFont(ofSize: 14)
        return label
    }()
    
    private let descriptionDetail: UILabel = {
        let label = UILabel()
        label.textAlignment = .center
        label.font = .systemFont(ofSize: 14)
        label.textColor = .slWarmGrey
        label.text = "We’ll send your contact an invitation by SMS,so make sure you’ve selected their mobile number.You can revoke access or share with another user at any time.".localized()
        label.numberOfLines = 0
        return label
    }()
    
    private let inviteButton: UIButton = {
        let button = UIButton(type: .custom)
        button.backgroundColor = .slRobinsEgg
        button.setTitle("SEND SHARE INVITATION".localized(), for: .normal)
        button.layer.cornerRadius = 3
        return button
    }()
    
    private let phoneNumberKit = PhoneNumberKit()
    
    private let lockId: Int32
    private let contact: EllipseContact
    
    init(lockId: Int32, contact: EllipseContact) {
        self.lockId = lockId
        self.contact = contact
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white
        
        title = "SHARE WITH ".localized() + (contact.firstName ?? contact.name).uppercased()
        
        addBackButton()
        
        userImageView.frame = {
            var frame = CGRect(x: 0, y: 0, width: 71, height: 71)
            frame.origin.y = 130
            frame.origin.x = view.bounds.midX - frame.size.width*0.5
            return frame
        }()
        userImageView.contentMode = .scaleAspectFit
        view.addSubview(userImageView)
        if let image = contact.image {
            userImageView.image = image
        }
        
        userNameLabel.frame = {
            var frame = view.bounds.insetBy(dx: 20, dy: 0)
            frame.origin.y = userImageView.frame.maxY + 14
            frame.size.height = userNameLabel.font.lineHeight
            return frame
        }()
        userNameLabel.text = contact.name
        view.addSubview(userNameLabel)
        
        phoneLabel.frame = {
            var frame = userNameLabel.frame
            frame.origin.y = userNameLabel.frame.maxY + 10
            frame.size.height = phoneLabel.font.lineHeight
            return frame
        }()
        phoneLabel.text = contact.phoneNumber
        view.addSubview(phoneLabel)
        
        inviteButton.frame = {
            var frame = view.bounds.insetBy(dx: 23, dy: 0)
            frame.size.height = 50
            frame.origin.y = view.bounds.height - 23 - frame.height
            return frame
        }()
        inviteButton.addTarget(self, action:#selector(pressedShare), for: UIControlEvents.touchUpInside)
        view.addSubview(inviteButton)
        
        descriptionDetail.frame = {
            var frame = inviteButton.frame
            frame.size.height = descriptionDetail.sizeThatFits(CGSize(width: frame.width, height: .greatestFiniteMagnitude)).height
            frame.origin.y = inviteButton.frame.minY - frame.height - 30
            return frame
        }()
        view.addSubview(descriptionDetail)
    }

    func pressedShare(sender: UIButton) {
        func showAlert(text: String) {
            self.presentWarningViewControllerWithTexts(texts: [.Info: text, .Header: "SHARING ERROR".localized(), .CancelButton: "OK".localized()], cancelClosure: nil)
        }
        
        guard let phone = try? phoneNumberKit.parse(contact.phoneNumber) else { return showAlert(text: "Wrong country code!") }
        presentLoadingViewWithMessage(message: "Sending invitation...".localized())
        let recipient = phoneNumberKit.format(phone, toType: .e164)
        Oval.locks.share(lock: lockId, to: Oval.Locks.Contact(phoneNumber: recipient, countryCode: contact.countryCode), success: { [weak self] in
            self?.dismissLoadingViewWithCompletion(completion: {
                let alvc = SLSharingInviteFormViewController()
                self?.navigationController?.pushViewController(alvc, animated: true)
            })
        }, fail: { [weak self] error in
            self?.dismissLoadingViewWithCompletion(completion: {
                if let `self` = self {
                    showAlert(text: String(format: "There was a problem attempting to share your lock with %@. Please try again.".localized(), self.contact.name))
                }
            })
            Answers.logShare(withMethod: "Sharing Failed", contentName: nil, contentType: nil, contentId: nil, customAttributes: ["error": "\(error)"])
        })
    }
}

extension PhoneNumberKit {
    func convert(phoneNumber: CNPhoneNumber, toType type: PhoneNumberFormat) -> String? {
        do {
            let phone = try parse(phoneNumber)
            return format(phone, toType: type)
        } catch {
            return nil
        }
    }
    
    func parse(_ phoneNumber: CNPhoneNumber) throws -> PhoneNumber  {
        guard let countryCode = phoneNumber.countryCodeString else { throw PhoneNumberError.invalidCountryCode }
        return try parse(phoneNumber.stringValue, withRegion: countryCode.uppercased(), ignoreType: false)
    }
}
