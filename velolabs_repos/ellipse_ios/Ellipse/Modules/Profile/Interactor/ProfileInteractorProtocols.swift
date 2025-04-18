//
//  ProfileProfileInteractorProtocols.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 30/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

struct Profile {
    enum Item {
        case firstName(String?), lastName(String?), phoneNmber(String?), email(String?), password
    }
}

protocol ProfileInteractorInput: TableViewPresentable {
    func viewDidLoad()
    func item(for indexPath: IndexPath) -> Profile.Item
    func save(firstName: String)
    func save(lastName: String)
    func update(phoneNumber: String, code: String?)
    func update(email: String, code: String?)
    func update(password: String?, code: String?)
    func isDifferent(phoneNumber: String) -> Bool
    func isDifferent(email: String) -> Bool
    func isFBUser() -> Bool
}

protocol ProfileInteractorOutput: InteractorOutput {
    func refresh()
    func saved()
    func enterCode(phoneNumber: String)
    func enterCode(email: String)
    func enterPasswordCode(phoneNumber: String)
}
