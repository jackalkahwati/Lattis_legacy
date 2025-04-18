//
//  ProfileProfileInteractor.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 30/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import ImagePicker
import Oval

class ProfileInteractor: NSObject {
    weak var view: ProfileInteractorOutput! {
        didSet {
            errorHandler = ErrorHandler(view)
        }
    }
    var router: ProfileRouter!
    
    fileprivate var items: [Profile.Item] = []
    fileprivate var storageHandler: StorageHandler?
    fileprivate var errorHandler: ErrorHandler!
    fileprivate let storage: UserStorage = CoreDataStack.shared
    fileprivate var network: UserNetwork = Session.shared
    fileprivate var user: User! {
        didSet {
            calculate()
        }
    }
}

extension ProfileInteractor: ProfileInteractorInput {
    func viewDidLoad() {
        storageHandler = storage.current() { [unowned self] (user) in
            self.user = user
        }
    }
    
    var numberOfSections: Int {
        return 1
    }
    
    func numberOfRows(in section: Int) -> Int {
        return items.count
    }
    
    func item(for indexPath: IndexPath) -> Profile.Item {
        return items[indexPath.row]
    }
    
    func save(lastName: String) {
        var user = self.user!
        user.lastName = lastName
        view.startLoading(text: "saving".localized())
        network.update(user: user) { [weak self] result in
            switch result {
            case .success(let user):
                self?.storage.save(user)
                self?.view.saved()
            case .failure(let error):
                self?.errorHandler.handle(error: error)
            }
        }
    }
    
    func save(firstName: String) {
        var user = self.user!
        user.firstName = firstName
        view.startLoading(text: "saving".localized())
        network.update(user: user) { [weak self] result in
            switch result {
            case .success(let user):
                self?.storage.save(user)
                self?.view.saved()
            case .failure(let error):
                self?.errorHandler.handle(error: error)
            }
        }
    }
    
    func update(phoneNumber: String, code: String?) {
        if let c = code {
            var user = self.user!
            user.phone = phoneNumber
            view.startLoading(text: "saving".localized())
            network.update(phoneNumber: phoneNumber.trimmedPhoneNumber, with: c) { [weak self] result in
                switch result {
                case .success:
                    self?.storage.save(user)
                    self?.view.saved()
                case .failure(let error):
                    self?.errorHandler.handle(error: error)
                }
            }
        } else {
            view.startLoading(text: "sending_confirmation_code".localized())
            network.getUpdateCode(for: phoneNumber.trimmedPhoneNumber) { [weak self] result in
                switch result {
                case .success:
                    self?.view.enterCode(phoneNumber: phoneNumber.phoneNumberFormat)
                case .failure(let error):
                    guard let e = error as? SessionError, e.code == .conflict else {
                        self?.errorHandler.handle(error: error)
                        return
                    }
                    self?.view.show(warning: "alert.signup.duplicate.error.message".localized(), title: nil)
                }
            }
        }
    }
    
    func update(email: String, code: String?) {
        if let c = code {
            var user = self.user!
            user.email = email
            view.startLoading(text: "saving".localized())
            network.update(email: email.trimmingCharacters(in: .whitespacesAndNewlines), with: c) { [weak self] result in
                switch result {
                case .success:
                    self?.storage.save(user)
                    self?.view.saved()
                case .failure(let error):
                    self?.errorHandler.handle(error: error)
                }
            }
        } else {
            view.startLoading(text: "sending_confirmation_code".localized())
            network.getUpdateCode(email: email.trimmingCharacters(in: .whitespacesAndNewlines)) { [weak self] result in
                switch result {
                case .success:
                    self?.view.enterCode(email: email)
                case .failure(let error):
                    self?.errorHandler.handle(error: error)
                }
            }
        }
    }
    
    func update(password: String?, code: String?) {
        if let p = password, let c = code {
            view.startLoading(text: "saving".localized())
            network.update(password: p, with: c) { [weak self] result in
                switch result {
                case .success:
                    self?.view.saved()
                case .failure(let error):
                    self?.errorHandler.handle(error: error)
                }
            }
        } else if let phone = user.phone {
            view.startLoading(text: "sending_confirmation_code".localized())
            network.getUpdatePasswordCode { [weak self] result in
                switch result {
                case .success:
                    self?.view.enterPasswordCode(phoneNumber: phone.phoneNumberFormat)
                case .failure(let error):
                     self?.errorHandler.handle(error: error)
                }
            }
        } else {
            // TODO: Show pop up for no phone number case
        }
    }
    
    func isDifferent(phoneNumber: String) -> Bool {
        guard let current = user.phone else { return true }
        return current != phoneNumber.trimmedPhoneNumber
    }
    
    func isDifferent(email: String) -> Bool {
        guard let current = user.email else { return true }
        return current != email.trimmingCharacters(in: .whitespacesAndNewlines)
    }
    
    func isFBUser() -> Bool {
        return user.userType == .facebook
    }
}

extension ProfileInteractor: ProfilePhoneDelegate {
    func sendCode(to phoneNumber: String, completion: @escaping (Error?) -> ()) {
        network.getUpdateCode(for: phoneNumber) { result in
            switch result {
            case .success:
                completion(nil)
            case .failure(let error):
                completion(error)
            }
        }
    }
    
    func confirm(phoneNumber: String, with code: String, completion: @escaping (Error?) -> ()) {
        network.update(phoneNumber: phoneNumber, with: code) { result in
            switch result {
            case .success:
                completion(nil)
            case .failure(let error):
                completion(error)
            }
        }
    }
    
    func save(phoneNumber: String) {
        user.phone = phoneNumber
        storage.save(user)
        router.pop()
        network.user(nil) { [weak self] result in
            switch result {
            case .success(let usr):
                self?.storage.save(usr)
            case .failure(let error):
                self?.errorHandler.handle(error: error)
            }
        }
    }
}

private extension ProfileInteractor {
    func calculate() {
        items = [
            .firstName(user.firstName),
            .lastName(user.lastName),
            .phoneNmber(user.phone),
            .email(user.email)
        ]
        if user.userType == .ellipse {
            items.append(.password)
        }
        view.refresh()
    }
}
