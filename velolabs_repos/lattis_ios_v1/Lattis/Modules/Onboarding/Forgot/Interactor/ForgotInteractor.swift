//
//  ForgotForgotInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Oval

class ForgotInteractor {
    weak var emailView: ForgotInteractorOutput!
    weak var passwordView: ForgotInteractorOutput!
    weak var delegate: ForgotInteractorDelegate!
    var router: ForgotRouter!
    var email: String?
    var network: UserNetwork!
    
    init(network: UserNetwork = Session.shared) {
        self.network = network
    }
}

extension ForgotInteractor: ForgotInteractorInput {
    func viewLoaded() {
        guard let email = email else { return }
        passwordView.show(email: email)
    }
    
    func submit(email: String) {
        self.email = email
        emailView.startLoading(with: "login_forgot_loading_email".localized())
        network.getConfirmationCode(email: email) { [weak self] (result) in
            switch result {
            case .success:
                self?.emailView.stopLoading(completion: nil)
                self?.router.openPassword(interactor: self!)
            case .failure(let error):
                self?.emailView.show(error: error, file: #file, line: #line)
            }
        }
    }
    
    func submit(password: String, code: String) {
        passwordView.startLoading(with: "login_forgot_loading_password".localized())
        network.confirm(forgot: .init(confirmationCode: code, email: email!, password: password)) { [weak self] (result) in
            switch result {
            case .success:
                self?.delegate.passwordChanged(for: self!.email!)
                self?.passwordView.showSuccess()
            case .failure(let error):
                self?.passwordView.show(error: error, file: #file, line: #line)
                
                Analytics.report(error)
            }
        }
    }
}
