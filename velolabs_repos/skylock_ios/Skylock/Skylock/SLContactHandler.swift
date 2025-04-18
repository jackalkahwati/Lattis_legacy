//
//  SLContactHandler.swift
//  Skylock
//
//  Created by Andre Green on 4/7/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import Foundation
import Contacts
import RestService

enum SLUserDefaultsEmergencyContactId: String {
    case One = "SLUserDefaultsEmergencyContactIdOne"
    case Two = "SLUserDefaultsEmergencyContactIdTwo"
    case Three = "SLUserDefaultsEmergencyContactIdThree"
}

@objc class SLContactHandler:NSObject {
    private enum PredicateType {
        case Ids
        case Name
    }
    
    private let keysToFetch: [CNKeyDescriptor] = [
        CNContactFormatter.descriptorForRequiredKeys(for: .fullName),
        CNContactGivenNameKey as CNKeyDescriptor,
        CNContactFamilyNameKey as CNKeyDescriptor,
        CNContactImageDataKey as CNKeyDescriptor,
        CNContactEmailAddressesKey as CNKeyDescriptor,
        CNContactPhoneNumbersKey as CNKeyDescriptor,
        CNContactThumbnailImageDataKey as CNKeyDescriptor
    ]
    
    func getContactsWithIds(contactIds: [String]) throws -> [CNContact] {
        return try self.getContacts(predicateType: PredicateType.Ids, predicateArgument: contactIds)
    }
    
    func getContactsWithName(name: String) throws -> [CNContact] {
        return try self.getContacts(predicateType: PredicateType.Name, predicateArgument: [name])
    }
    
    func allContacts(completion: ([CNContact]) -> Void) throws {
        let fetchRequest = CNContactFetchRequest(keysToFetch: self.keysToFetch)
        var contacts: [CNContact] = []
        let store = CNContactStore()
        try store.enumerateContacts(with: fetchRequest) { (contact, _) in
            contacts.append(contact)
        }
        
        completion(contacts)
    }
    
    func dbEmegencyContacts() -> [SLEmergencyContact]? {
        let contacts:[SLEmergencyContact]? = SLDatabaseManager.shared().emergencyContacts() as? [SLEmergencyContact]
        
        return contacts
    }
    
    func emergencyContactFromCNContact(contact: CNContact) -> SLEmergencyContact {
        print("contact: \(contact)")
        let dbManager = SLDatabaseManager.shared()
        if let dbContact = dbManager.getContactWithContactId(contact.identifier) {
            return dbContact
        }
        
        let newContact = dbManager.newEmergencyContact()
        newContact.firstName = contact.givenName
        newContact.lastName = contact.familyName
        newContact.contactId = contact.identifier
        newContact.isCurrentContact = false
        
        if !contact.phoneNumbers.isEmpty {
            let phoneNumber = contact.phoneNumbers[0].value
            newContact.phoneNumber = phoneNumber.value(forKey: "digits") as? String
            newContact.countyCode = phoneNumber.value(forKey: "countryCode") as? String
        }
        
        if !contact.emailAddresses.isEmpty {
            newContact.email = (contact.emailAddresses[0]).value(forKey: "value") as? String
        }
        
        return newContact
    }
    
    func getImageForContact(identifier: String, completion:((_: NSData?) -> ())?) {
        DispatchQueue.global().async {
            var contact:CNContact?
            do {
                contact = try CNContactStore().unifiedContact(
                    withIdentifier: identifier,
                    keysToFetch: [CNContactImageDataKey as CNKeyDescriptor]
                )
            } catch {
                if let exit = completion {
                    exit(nil)
                }
            }
            
            if let exit = completion {
                exit(contact?.imageData as NSData?)
            }
        }
    }
    
    func getActiveEmergencyContacts() -> [SLEmergencyContact]? {
        guard let contacts = self.dbEmegencyContacts() else {
            return nil
        }
        
        var activeContacts:[SLEmergencyContact] = [SLEmergencyContact]()
        for contact:SLEmergencyContact in contacts {
            if let isCurrent = contact.isCurrentContact, isCurrent.boolValue {
                activeContacts.append(contact)
            }
        }
        
        return activeContacts
    }
    
    func saveContactToUserDefaults(
        contact: CNContact,
        contactId: SLUserDefaultsEmergencyContactId,
        shouldSaveToServer: Bool
        )
    {
        let ud = UserDefaults.standard
        ud.set(contact.identifier, forKey: contactId.rawValue)
        ud.synchronize()
        
        if shouldSaveToServer {
            self.saveContactToServer(
                contact: contact,
                index: self.indexFromUserDefaultsContactId(contactId: contactId)
            )
        }
    }
    
    func emergencyContactIdFromUserDefaults(contactId: SLUserDefaultsEmergencyContactId) -> String? {
        let ud = UserDefaults.standard
        return ud.object(forKey: contactId.rawValue) as? String
    }
    
    @objc func emergencyContactsCommaSeperatedFirstNames() -> String {
        var names = ""
        let udContactIds: [SLUserDefaultsEmergencyContactId] = [.One, .Two, .Three]
        for (index, udContactId) in udContactIds.enumerated() {
            if let contactId = self.emergencyContactIdFromUserDefaults(contactId: udContactId) {
                do {
                    let contacts = try self.getContactsWithIds(contactIds: [contactId])
                    if let contact = contacts.first {
                        names += contact.givenName + (index == udContactIds.count - 1 ? "" : ", ")
                    }
                } catch {
                    print("Error: failed to get contact with ID: \(contactId)")
                }
            }
        }
        
        return names
    }
    
    func phoneNumberForUserDefualtContactId(contactId: String) -> String? {
        let contacts: [CNContact]
        do {
            contacts = try self.getContactsWithIds(contactIds: [contactId])
        } catch {
            print("Error: could not get CNContact with Id: \(contactId)")
            return nil
        }
        
        
        if let contact = contacts.first {
            return self.phoneNumberForContact(contact: contact)
        }
        
        return nil
    }
    
    func countryCodeFromCNContact(contact: CNContact) -> String {
        if contact.isKeyAvailable(CNContactPhoneNumbersKey) {
            if let phoneNumber = contact.phoneNumbers.last {
                print()
                return ((phoneNumber.value.value(forKey: "countryCode") as? String)?.uppercased())!
            }
        }
        return ""
    }
    
    func phoneNumberForContact(contact: CNContact) -> String? {
        if contact.isKeyAvailable(CNContactPhoneNumbersKey) {
            if let phoneNumber = contact.phoneNumbers.first {
                return phoneNumber.value.stringValue
            }
        }
        
        return nil
    }
    
    func fullNameForContact(contact: CNContact) -> String {
        var fullName = ""
        if contact.isKeyAvailable(CNContactGivenNameKey) {
            fullName += contact.givenName
        }
        
        if contact.isKeyAvailable(CNContactFamilyNameKey) {
            if fullName != "" {
                fullName += " "
            }
            
            fullName += contact.familyName
        }
        
        return fullName
    }
    
    func authorizedToAccessContacts() -> Bool {
        let status:CNAuthorizationStatus = CNContactStore.authorizationStatus(for: .contacts)
        return status == .authorized
    }
    
    func requestAuthorization(completion: ((_:Bool) -> Void)?) {
        CNContactStore().requestAccess(for: .contacts) { (didAllowAccess:Bool, error:Error?) -> Void in
            guard let completionBlock = completion else {
                return
            }
            
            if error == nil {
                completionBlock(didAllowAccess)
            } else {
                completionBlock(false)
            }

        }
    }
    
    private func getContacts(predicateType: PredicateType, predicateArgument: [String]) throws -> [CNContact]  {
        let predicate:NSPredicate
        
        switch predicateType {
        case .Ids:
            predicate = CNContact.predicateForContacts(withIdentifiers: predicateArgument)
        case .Name:
            predicate = CNContact.predicateForContacts(matchingName: predicateArgument.first!)
        }

        let contacts = try CNContactStore().unifiedContacts(
            matching: predicate,
            keysToFetch: self.keysToFetch
        )
        
        print("There were \(contacts.count) contacts fetched.")
        return contacts
    }
    
    private func saveContactToServer(contact: CNContact, index: Int) {
//        DispatchQueue.global().async {
//            let dataBaseManager = SLDatabaseManager.shared()
//            let ud = UserDefaults.standard
//            guard let user = dataBaseManager.getCurrentUser() else {
//                print("Error could not retrieve current user to post contact to server with Id: \(contact.identifier)")
//                return
//            }
//            
//            guard let phoneNumber = self.phoneNumberForContact(contact: contact) else {
//                print("Error: failed to retrieve contact phone with Id: \(contact.identifier) to server")
//                return
//            }
//            
//            guard let token = ud.value(forKey: SLUserDefaultsUserToken) as? String else {
//                print("Error: no user token found when trying to save contact \(contact.identifier) to server")
//                return
//            }
//            
//            
//            let body: [String : Any] = [
//                "emergency_contact": phoneNumber,
//                "emergency_contact_name": self.fullNameForContact(contact: contact)
//            ]
//            
//            let saveContact = Resource<String>(path: "users/\(user.usersId!)/mobiles/\(index)/",
//                jsonData: body,
//                parse: { $0.string }
//            )
//            
//            RestService.main.post(saveContact.auth(username: token, password: ""), success: { (string) in
//                print("Got payload from saving contact with Id: \(contact.identifier)")
//                print("\n\(string)")
//            }, fail: { (error) in
//                print("Contact saving error: \(error)")
//            })
//        }
    }
    
    private func indexFromUserDefaultsContactId(contactId: SLUserDefaultsEmergencyContactId) -> Int {
        let index: Int
        switch contactId {
        case .One:
            index = 1
        case .Two:
            index = 2
        case .Three:
            index = 3
        }
        
        return index
    }
}
