//
//  ShareShareRouter.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 07/11/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Contacts
import ContactsUI

final class ShareRouter: Router {
    class func instantiate() -> ShareViewController {
        let share = ShareViewController()
        inject(controller: share)
        return share
    }
    
    func openContacts(delegate: ContactsInteractorDelegate) {
        func show() {
            let contacts = ContactsRouter.instantiate(delegate: delegate)
            contacts.title = "sharing".localized()
            let navigation = NavigationController(rootViewController: contacts)
            largeTitleWhiteStyle(navigation.navigationBar)
            controller.present(navigation, animated: true, completion: nil)
        }
        
        let authorizationStatus = CNContactStore.authorizationStatus(for: .contacts)
        
        switch authorizationStatus {
        case .authorized:
            show()
        case .denied, .notDetermined:
            CNContactStore().requestAccess(for: .contacts, completionHandler: { (access, accessError) -> Void in
                if access {
                    show()
                }
                else {
                    if authorizationStatus == .denied {
                        // TODO: alert
                    }
                }
            })
        default:
            break
        }
        
    }
    
    func dismiss(completion: @escaping () -> ()) {
        controller.dismiss(animated: true, completion: completion)
    }
    
    func chooseNumber(contact: Contact, completion: @escaping (String) -> ()) {
        let action = UIAlertController(title: contact.fullName, message: "choose_number_for_sharing".localized(), preferredStyle: .actionSheet)
        for number in contact.phoneNumbers {
            action.addAction(UIAlertAction(title: number, style: .default, handler: { (_) in
                completion(number)
            }))
        }
        action.addAction(UIAlertAction(title: "cancel".localized(), style: .cancel, handler: nil))
        controller.present(action, animated: true, completion: nil)
    }
    
    func openOnboarding() {
        let onboard = LockOnboardingRouter.instantiate(delegate: nil)
        let navigation = NavigationController(rootViewController: onboard)
        largeTitleWhiteStyle(navigation.navigationBar)
        controller.present(navigation, animated: true, completion: nil)
    }
}

private func inject(controller: ShareViewController) {
    let interactor = ShareInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = ShareRouter(controller)
}
