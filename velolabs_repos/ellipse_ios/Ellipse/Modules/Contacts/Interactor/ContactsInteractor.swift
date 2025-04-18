//
//  ContactsContactsInteractor.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 08/11/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import Contacts
import PhoneNumberKit

class ContactsInteractor {
    weak var view: ContactsInteractorOutput!
    var router: ContactsRouter!
    weak var delegate: ContactsInteractorDelegate?
    var selection: ContactsSelection = .single
    fileprivate(set) var sectionTitles: [String] = []
    fileprivate(set) var searchContacts: [Contact] = []
    fileprivate var allContacts: [Contact] = []
    var selectedContacts: [Contact] = []
    fileprivate var sections: [Contact.Section] = []
    var reloadSearch: () -> () = {}
}

extension ContactsInteractor: ContactsInteractorInput {
    func isSelected(contact: Contact) -> Bool {
        return selectedContacts.contains(contact)
    }
    
    func didSelect(contact: Contact, isSearch: Bool) {
        if let idx = selectedContacts.index(of: contact) {
            selectedContacts.remove(at: idx)
            updateHint()
        } else if case let .multiple(count) = selection, selectedContacts.count == count  {
            view.remindLimit()
            return
        } else {
            selectedContacts.append(contact)
            delegate?.didSelect(contact: contact)
            updateHint()
        }
        if isSearch {
            view.refresh()
        }
    }
    
    func title(for section: Int) -> String {
        return sections[section].title
    }
    
    func start() {
        updateHint()
        DispatchQueue.global().async {
            let keys: [CNKeyDescriptor] = [
                CNContactFormatter.descriptorForRequiredKeys(for: .fullName),
                CNContactGivenNameKey as CNKeyDescriptor,
                CNContactFamilyNameKey as CNKeyDescriptor,
                CNContactImageDataKey as CNKeyDescriptor,
                CNContactEmailAddressesKey as CNKeyDescriptor,
                CNContactPhoneNumbersKey as CNKeyDescriptor,
                CNContactThumbnailImageDataKey as CNKeyDescriptor
            ]
            let fetchRequest = CNContactFetchRequest(keysToFetch: keys)
            var contacts: [CNContact] = []
            let store = CNContactStore()
            do {
                try store.enumerateContacts(with: fetchRequest) { (contact, _) in
                    contacts.append(contact)
                }
                self.allContacts = contacts.filter({ $0.phoneNumbers.isEmpty == false }).map(Contact.init).sorted(by: { $0.fullName < $1.fullName })
                self.searchContacts = self.allContacts
                self.calculate(contacts: self.allContacts)
            } catch {
                DispatchQueue.main.async {
                    self.view.show(error: error)
                }
            }
        }
    }
    
    func item(for indexPath: IndexPath) -> Contact {
        return sections[indexPath.section].contacts[indexPath.row]
    }
    
    var numberOfSections: Int {
        return sections.count
    }
    
    func numberOfRows(in section: Int) -> Int {
        return sections[section].contacts.count
    }
    
    func saveSelected() {
        delegate?.didSelect(contacts: selectedContacts)
    }
    
    func search(_ pattern: String) {
        DispatchQueue.global().async {
            self.searchContacts = self.allContacts.filter({ (contact) -> Bool in
                let phones = contact.phoneNumbers.map({$0.trimmedPhoneNumber}).joined(separator: ",")
                return contact.fullName.lowercased().contains(pattern.lowercased()) || phones.contains(pattern)
            })
            DispatchQueue.main.async(execute: self.reloadSearch)
        }
    }
}

private extension ContactsInteractor {
    func calculate(contacts: [Contact]) {
        for contact in contacts {
            guard let headerChar = contact.fullName.first else { continue }
            let title = String(headerChar).uppercased()
            var section = sections.last
            
            if section?.title == title {
                section?.contacts.append(contact)
                _ = sections.removeLast()
                sections.append(section!)
            } else {
                sections.append(Contact.Section(title: title, contacts: [contact]))
            }
        }
        sectionTitles = sections.map{ $0.title }
        DispatchQueue.main.async(execute: view.refresh)
    }
    
    func updateHint() {
        if case let .multiple(count) = selection {
            view.reloadHint(left: count - selectedContacts.count)
        }
    }
}

extension Contact {
    struct Section {
        let title: String
        var contacts: [Contact]
    }
    
    init(_ contact: CNContact) {
        self.firstName = contact.givenName
        self.lastName = contact.familyName
        self.identifier = contact.identifier
        self.phoneNumbers = contact.phoneNumbers.map{ $0.value.stringValue }
        self.primaryNumber = self.phoneNumbers.first
        self.countryCode = Locale.current.regionCode?.lowercased() ?? "us"
    }
    
    mutating func update(primary: String) {
        let kit = PhoneNumberKit()
        do {
            let phone = try kit.parse(primary, withRegion: countryCode, ignoreType: true)
            self.primaryNumber = phone.numberString
            if let code = kit.countries(withCode: phone.countryCode)?.first {
                self.countryCode = code
            }
        } catch {
            
        }
    }
}

