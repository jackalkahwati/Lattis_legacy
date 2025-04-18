//
//  ContactsContactsRouter.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 08/11/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

final class ContactsRouter: Router {
    class func instantiate(delegate: ContactsInteractorDelegate, selection: ContactsSelection = .single, contacts: [Contact] = []) -> ContactsViewController {
        let cont = UIStoryboard.contacts.instantiateInitialViewController() as! ContactsViewController
        let interactor = inject(controller: cont)
        interactor.delegate = delegate
        interactor.selection = selection
        interactor.selectedContacts = contacts
        return cont
    }
}

private func inject(controller: ContactsViewController) -> ContactsInteractor {
    let interactor = ContactsInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = ContactsRouter(controller)
    return interactor
}
