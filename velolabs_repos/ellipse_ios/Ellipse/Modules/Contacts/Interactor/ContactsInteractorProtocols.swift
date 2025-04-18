//
//  ContactsContactsInteractorProtocols.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 08/11/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

enum ContactsSelection {
    case single
    case multiple(Int)
    
    var identifire: String {
        switch self {
        case .single:
            return "single"
        case .multiple(_):
            return "multiple"
        }
    }
}

protocol ContactsInteractorDelegate: class {
    func didSelect(contact: Contact)
    func didSelect(contacts: [Contact])
}

protocol ContactsInteractorInput: TableViewPresentable {
    func start()
    func item(for indexPath: IndexPath) -> Contact
    func title(for section: Int) -> String
    func didSelect(contact: Contact, isSearch: Bool)
    func isSelected(contact: Contact) -> Bool
    func saveSelected()
    var sectionTitles: [String] {get}
    var selection: ContactsSelection {get}
    func search(_ pattern: String)
    var reloadSearch: () -> () {get set}
    var searchContacts: [Contact] {get}
}

protocol ContactsInteractorOutput: InteractorOutput {
    func refresh()
    func reloadRows(at indexPaths: [IndexPath])
    func reloadHint(left: Int)
    func remindLimit()
}
