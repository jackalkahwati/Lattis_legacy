//
//  LogInLogInInteractor.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 08/03/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//
import UIKit
import Oval

protocol LogInInteractorDelegate: class {
    func loginSucceded(for userId: Int, verification: ((UIViewController) -> ())?, errorHandler: @escaping (Error?) -> ())
    func loginSucceded(userId: Int)
}

final class LogInInteractor {
    var router: LogInRouter!
    weak var view: LogInInteractorOutput!
    weak var delegate: LogInInteractorDelegate?
    let network: OperatorNetwork = Session.shared
    
    func handle(error: Error) {
        if let err = error as? SessionError, err.code == .unauthorized {
            view.showAlert(title: "incorrect_password_title".localized(), subtitle: "incorrect_password_message".localized())
        } else {
            view.show(error: error)
        }
    }
}


extension LogInInteractor: LogInInteractorInput {
    func login(with email: String, password: String) {
        view.startLoading(title: nil)
        network.login(with: .init(username: email, password: password)) { [weak self] result in
            switch result {
            case .success(let userId):
                self?.view.stopLoading {}
                self?.delegate?.loginSucceded(userId: userId)
                AppDelegate.shared.isLoggedIn = true
            case .failure(let error):
                self?.handle(error: error)
            }
        }
    }
}

private extension LogInInteractor {
    func getTokens(for userId: Int, password: String) {
        network.getTokens(userId: userId, password: password) { [weak self] result in
            switch result {
            case .success:
                self?.delegate?.loginSucceded(for: userId, verification: nil, errorHandler: {_ in})
            case .failure(let error):
                self?.handle(error: error)
            }
        }
    }
}
