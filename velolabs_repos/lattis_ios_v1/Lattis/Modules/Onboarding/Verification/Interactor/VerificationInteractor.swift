//
//  VerificationVerificationInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit
import Oval

class VerificationInteractor {
    weak var view: VerificationInteractorOutput!
    var router: VerificationRouter!
    weak var delegate: WelcomeInteractorDelegate!
    var network: UserNetwork!
    var loadingText: String!
    
    init(network: UserNetwork = Session.shared) {
        self.network = network
    }
}

extension VerificationInteractor: VerificationInteractorInput {
    func verify(with code: String) {
        view.startLoading(with: loadingText)
        network.confirmVerification(code: code) { [weak self] (result) in
            switch result {
            case .success(let userId):
                self?.view.stopLoading(completion: nil)
                self?.delegate.loginSucceded(for: userId, password: nil)
            case .failure(let error):
                self?.view.show(error: error, file: #file, line: #line)
                Analytics.report(error)
            }
        }
    }
}
