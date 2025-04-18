//
//  SignUpSignUpInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Oval

class SignUpInteractor {
    weak var view: SignUpInteractorOutput!
    weak var delegate: WelcomeInteractorDelegate!
    var router: SignUpRouter!
    var network: UserNetwork!
    
    init(network: UserNetwork = Session.shared) {
        self.network = network
    }
}

extension SignUpInteractor: SignUpInteractorInput {
    func openLogIn() {
        router.openLogIn(with: delegate)
    }
    
    func signUp(user: User.Request) {
        view.startLoading(with: "signup_loading".localized())
        network.registration(user: user) { [weak self] (result) in
            switch result {
            case .success(let userId, let isVerified):
                if isVerified {
                    self?.view.stopLoading(completion: nil)
                    self?.delegate.loginSucceded(for: userId, password: user.password!)
                } else {
                    self?.verify(user: user)
                }
            case .failure(let error):
                self?.view.stopLoading(completion: nil)
                self?.handle(error: error)
            }
        }
    }
}

private extension SignUpInteractor {
    func handle(error: Error?) {
        let title = "general_error_title".localized()
        let text = "general_error_text".localized()
        view.warning(with: title, subtitle: text)
    }
    
    func verify(user: User.Request) {
        network.getEmailVerificationCode(email: user.email, accoutnType: nil) { [weak self] (result) in
            switch result {
            case .success:
                self?.router.openVerification(with: self!.delegate)
                self?.view.stopLoading(completion: nil)
            case .failure:
                self?.view.stopLoading(completion: nil)
            }
        }
    }
}


extension Set where Element: Equatable {
    func containsArray<T : Sequence> (array:T) -> Bool where T.Iterator.Element == Element {
        for item in array {
            if !self.contains(item) {
                return false
            }
        }
        return true
    }
}
