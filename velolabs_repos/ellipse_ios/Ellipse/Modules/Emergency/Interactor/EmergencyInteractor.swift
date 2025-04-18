//
//  EmergencyEmergencyInteractor.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 14/11/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import Contacts

class EmergencyInteractor {
    weak var view: EmergencyInteractorOutput!
    var router: EmergencyRouter!
    
    fileprivate var contacts: [Contact] = []
    fileprivate let storage: ContactStorage = CoreDataStack.shared
    fileprivate var storageHandler: StorageHandler?
    fileprivate let contactsStore = CNContactStore()
}

extension EmergencyInteractor: EmergencyInteractorInput {
    func start() {
        storageHandler = storage.emergency { [unowned self] (contacts) in
            self.contacts = contacts
            self.view.refresh()
        }
        if CNContactStore.authorizationStatus(for: .contacts) != .authorized {
            self.view.showHint()
        }
    }
    
    func selectContacts() {
        router.contacts(delegate: self, contacts: contacts)
    }
    
    func item(for indexPath: IndexPath) -> Contact {
        return contacts[indexPath.row]
    }
    
    var numberOfSections: Int {
        return 1
    }
    
    func numberOfRows(in section: Int) -> Int {
        return contacts.count
    }
    
    func requestAccess() {
        contactsStore.requestAccess(for: .contacts) { [unowned self] (isGranted, error) in
            if isGranted {
                DispatchQueue.main.async(execute: self.view.hideHint)
            }
        }
    }
}

extension EmergencyInteractor: EmergencyCellDelegate {
    func remove(contact: Contact) {
        storage.delete(contact)
    }
}

extension EmergencyInteractor: ContactsInteractorDelegate {
    func didSelect(contact: Contact) {}
    
    func didSelect(contacts: [Contact]) {
        router.dismiss()
        storage.update(contacts)
    }
}
