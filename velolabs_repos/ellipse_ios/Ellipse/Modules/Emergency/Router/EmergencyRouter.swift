//
//  EmergencyEmergencyRouter.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 14/11/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

final class EmergencyRouter: Router {
    class func instantiate() -> EmergencyViewController {
        let emergency = EmergencyViewController()
        inject(controller: emergency)
        return emergency
    }
    
    func contacts(delegate: ContactsInteractorDelegate, contacts: [Contact]) {
        let contacts = ContactsRouter.instantiate(delegate: delegate, selection: .multiple(3), contacts: contacts)
        contacts.title = "action_emergency_contacts".localized()
        let navigation = NavigationController(rootViewController: contacts)
        largeTitleWhiteStyle(navigation.navigationBar)
        controller.present(navigation, animated: true, completion: nil)
    }
    
    func dismiss(completion: (() -> ())? = nil) {
        controller.dismiss(animated: true, completion: completion)
    }
}

private func inject(controller: EmergencyViewController) {
    let interactor = EmergencyInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = EmergencyRouter(controller)
}
