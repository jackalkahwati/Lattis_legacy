//
//  CoreDataStack+Contact.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/14/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

extension CoreDataStack: ContactStorage {
    fileprivate var check: (String) -> Bool {
        return { string in
            return String(describing: CDContact.self) == string
        }
    }
    
    func update(_ contacts: [Contact]) {
        let identifiers = contacts.compactMap({$0.identifier})
        guard identifiers.isEmpty == false else { return }
        write(completion: { (context) in
            do {
                let exist: [CDContact] = try CDContact.all(in: context, with: NSPredicate(format: "identifier IN %@", identifiers))
                let delete: [CDContact] = try CDContact.all(in: context, with: NSPredicate(format: "NOT(identifier IN %@)", identifiers))
                delete.forEach({context.delete($0)})
                for contact in contacts {
                    if let idx = exist.index(where: {$0.identifier == contact.identifier}) {
                        let ct = exist[idx]
                        ct.fill(contact)
                    } else {
                        let ct = CDContact.create(in: context)
                        ct.fill(contact)
                    }
                }
            } catch {
                
            }
        }, fail: {
            print($0)
        })
    }
    
    func save(_ contact: Contact) {
        
    }
    
    func delete(_ contact: Contact) {
        guard let identifire = contact.identifier else { return }
        write(completion: { (context) in
            do {
                guard let con = try CDContact.find(in: context, with: NSPredicate(format: "identifier = %@", identifire)) else { return }
                context.delete(con)
            } catch {
                
            }
        }, fail: {
            print($0)
        })
    }
    
    func emergency(completion: @escaping ([Contact]) -> ()) -> StorageHandler {
        let handler = StorageHandler(check: check) { [unowned self] in
            do {
                let contacts: [CDContact] = try self.read(with: NSPredicate(format: "NOT(identifier = nil)"))
                completion(contacts.map(Contact.init))
            } catch {
                print(error)
            }
        }

        subscribe(handler: handler)
        return handler
    }
    
    func getEmergency() -> [Contact] {
        guard let contacts: [CDContact] = try? self.read(with: NSPredicate(format: "NOT(identifier = nil)")) else { return [] }
        return contacts.map(Contact.init)
    }
}

extension Contact {
    init(_ contact: CDContact) {
        self.identifier = contact.identifier
        self.firstName = contact.firstName
        self.lastName = contact.lastName
        self.countryCode = contact.countryCode ?? (Locale.current.regionCode ?? "us")
        self.primaryNumber = contact.primaryNumber
        self.phoneNumbers = []
    }
}

extension CDContact {
    func fill(_ contact: Contact) {
        self.identifier = contact.identifier
        self.firstName = contact.firstName
        self.lastName = contact.lastName
        self.countryCode = contact.countryCode
        self.primaryNumber = contact.primaryNumber
    }
}

