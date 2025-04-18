//
//  ProfileProfileInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 02/04/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Foundation
import Oval

class ProfileInteractor {
    weak var view: ProfileInteractorOutput!
    var router: ProfileRouter!
    var action: (ProfileInteractor) -> () = {_ in}
    fileprivate let storage: UserStorage
    fileprivate let network: UserNetwork
    
    init(storage: UserStorage = CoreDataStack.shared, network: UserNetwork = Session.shared) {
        self.storage = storage
        self.network = network
    }
    
    func addPrivateNetwork() {
        let model = ProfileInfoModel(type: .privateNetworks, keyboard: .emailAddress, value: nil, action: update(value: for: ))
        edit(info: model)
    }
    
    func addPhoneNumber() {
        let model = ProfileInfoModel(type: .phone, keyboard: .phonePad, value: nil, action: update(value: for: ))
        edit(info: model)
    }
}

extension ProfileInteractor: ProfileInteractorInput {
    func edit(info: ProfileInfoModel) {
        router.edit(info: info)
    }
    
    func changePassword() {
        router.changePassword()
    }
    
    func deleteAccount() {
        view.startLoading(with: "delete_account_loading".localized())
        network.deleteUser { [weak self] (result) in
            switch result {
            case .success:
                self?.view.stopLoading(completion: nil)
                if let userId = Session.shared.storage.userId {
                    FileManager.default.removeUserDirectory(for: userId)
                }
                AppDelegate.shared.logout()
            case .failure(let error):
                self?.view.show(error: error, file: #file, line: #line)
                
                Analytics.report(error)
            }
        }
    }
    
    func viewLoaded() {
        guard let user = storage.user(with: nil) else {
            return view.show(error: ProfileError.emptyUser, file: #file, line: #line)
        }
        view.show(user)
        action(self)
    }
    
    func update(value: String, for infoType: ProfileInfoType) {
        guard var user = storage.user(with: nil) else {
            return view.show(error: ProfileError.emptyUser, file: #file, line: #line)
        }
        
        func save() {
            storage.save(user)
            view.show(user)
        }
        
        let restore = user
        switch infoType {
        case .name:
            user.firstName = value
            update(user, restore: restore)
        case .lastName:
            user.lastName = value
            update(user, restore: restore)
        case .email:
            user.email = value
            return update(email: value, restore: restore, completion: save)
        case .phone:
            user.phoneNumber = value
            return update(phone: value, restore: restore, completion: save)
        case .privateNetworks:
            return addPrivateNetvork(with: value, completion: refreshUser)
        default:
            break
        }
        save()
        router.pop()
    }
    
    func save(image: UIImage) {
        var user = storage.user(with: nil)
        user?.image = image
    }
}


private extension ProfileInteractor {
    func update(_ user: User, restore: User) {
        func result(responce: User?) {
            var usr = restore
            if let resp = responce {
                usr = resp
            }
            storage.save(usr)
            view.show(usr)
        }
        
        network.update(user: User.Request(user)) { [weak self] (res) in
            switch res {
            case .success(let responce):
                guard self != nil else { return }
                result(responce: responce)
            case .failure(let error):
                result(responce: nil)
            }
        }
    }
    
    func update(email: String, restore: User, completion: @escaping () -> ()) {
        let submit: (String, @escaping (Error?) -> ()) -> () = { [unowned self] code, block in
            self.network.update(email: User.Email(confirmationCode: code, email: email)) { [weak self] (result) in
                switch result {
                case .success:
                    block(nil)
                    completion()
                    self?.router.pop(root: true)
                case .failure(let error):
                    self?.storage.save(restore)
                    self?.view.show(restore)
                    block(error)
                }
            }
        }
        
        let resend: (@escaping (Error?) -> ()) -> () = { [unowned self] completion in
            self.network.getUpdateCode(email: email) { (result) in
                switch result {
                case .success:
                    completion(nil)
                case .failure(let error):
                    completion(error)
                }
            }
        }
        
        let success: () -> () = { [weak self] in
            self?.view.stopLoading(completion: nil)
            self?.router.openVerification(verificationType: ProfileVerificationType(title: String(format: "profile_edit_email_title".localized(), email), submit: submit, resend: resend, infoType: .email))
        }
        
        view.startLoading(with: "".localized())
        network.getUpdateCode(email: email) { [weak self] (result) in
            switch result {
            case .success:
                success()
            case .failure(let error):
                self?.view.show(error: error, file: #file, line: #line)
            }
        }
    }
    
    func update(phone: String, restore: User, completion: @escaping () -> ()) {
        let submit: (String, @escaping (Error?) -> ()) -> () = { [unowned self] code, block in
            self.network.update(phone: .init(phoneNumber: phone, confirmationCode: code)) { [weak self] (result) in
                switch result {
                case .success:
                    completion()
                    block(nil)
                    self?.router.pop(root: true)
                case .failure(let error):
                    self?.storage.save(restore)
                    self?.view.show(restore)
                    block(error)
                }
            }
        }
        
        let resend: (@escaping (Error?) -> ()) -> () = { [unowned self] completion in
            self.network.getUpdateCode(phoneNumber: phone) { [weak self] (result) in
                switch result {
                case .success:
                    completion(nil)
                case .failure(let e):
                    print(e)
                }
            }
        }
        
        let success: () -> () = { [weak self] in
            self?.view.stopLoading(completion: nil)
            self?.router.openVerification(verificationType: ProfileVerificationType(title: String(format: "profile_edit_phone_title".localized(), phone), submit: submit, resend: resend, infoType: .phoneNumber))
        }
        
        view.startLoading(with: "".localized())
        network.getUpdateCode(phoneNumber: phone) { [weak self] (result) in
            switch result {
            case .success:
                success()
            case .failure(let error):
                if let e = error as? SessionError, e.code == .unauthorized {
                    self?.view.warning(with: "general_error_title".localized(), subtitle: "duplicated_phone_number")
                } else {
                    self?.view.show(error: error, file: #file, line: #line)
                }
            }
        }
    }
    
    func addPrivateNetvork(with email: String, completion: @escaping () -> ()) {
        let submit: (String, @escaping (Error?) -> ()) -> () = { [unowned self] code, block in
            self.network.confirm(email: email, code: code, accountType: "private_account") { [weak self] (result) in
                switch result {
                case .success:
                    completion()
                    block(nil)
                    self?.router.pop(root: true)
                case .failure(let error):
                    block(error)
                }
            }
        }
        
        let resend: (@escaping (Error?) -> ()) -> () = { [unowned self] completion in
            self.network.addPrivateNetwork(email: email) { [weak self] (result) in
                switch result {
                case .success(let hasFleetForAccount):
                    if hasFleetForAccount {
                        completion(nil)
                    } else {
                        completion(ProfileError.noFleetsForAccount)
                    }
                case .failure(let error):
                    print(error)
                }
            }
        }
        
        let success: (Bool) -> () = { [weak self] hasFleetForAccount in
            if hasFleetForAccount {
                self?.view.stopLoading(completion: nil)
                self?.router.openVerification(verificationType: ProfileVerificationType(title: String(format: "profile_edit_private_network_title".localized(), email), submit: submit, resend: resend, infoType: .privateNetwork))
            } else if let s = self {
                let content = s.view.hasFleets ? "private_network_content_has_fleets" : "private_network_content"
                self?.view.warning(with: "private_network".localized(), subtitle: content.localized())
            }
        }
        
        view.startLoading(with: "".localized())
        network.addPrivateNetwork(email: email) { [weak self] (result) in
            switch result {
            case .success(let added):
                success(added)
            case .failure(let error):
                if let e = error as? SessionError, case .resourceNotFound = e.code, let s = self {
                    let content = s.view.hasFleets ? "private_network_content_has_fleets" : "private_network_content"
                    s.view.warning(with: "private_network".localized(), subtitle: content.localized())
                } else {
                    self?.view.show(error: error, file: #file, line: #line)
                }
            }
        }
    }
    
    func refreshUser() {
        network.getUser { [weak self] (result) in
            switch result {
            case .success(let user):
                self?.storage.save(user)
                self?.view.show(user)
            case .failure(let error):
                print(error)
            }
        }
    }
}

enum ProfileError: Error {
    case emptyUser
    case noFleetsForAccount
}
