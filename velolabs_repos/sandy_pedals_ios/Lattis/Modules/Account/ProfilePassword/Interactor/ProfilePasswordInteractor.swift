//
//  ProfilePasswordProfilePasswordInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 03/04/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Foundation
import Oval

class ProfilePasswordInteractor {
    weak var view: ProfilePasswordInteractorOutput!
    var router: ProfilePasswordRouter!
    
    fileprivate let network: UserNetwork
    
    init(network: UserNetwork = Session.shared) {
        self.network = network
    }
}

extension ProfilePasswordInteractor: ProfilePasswordInteractorInput {
    func submit(password: String, newPass: String) {
        view.startLoading(with:"profile_change_password_loader".localized())
        network.change(password: .init(password: password, newPassword: newPass)) { [weak self] (result) in
            switch result {
            case .success:
                var user = User.current
                user?.password = newPass
                self?.view.stopLoading(completion: nil)
                self?.view.success()
            case .failure(let error):
                self?.view.show(error: error, file: #file, line: #line)
            }
        }
    }
}
