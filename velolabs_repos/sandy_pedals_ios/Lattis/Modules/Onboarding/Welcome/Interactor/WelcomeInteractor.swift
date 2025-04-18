//
//  WelcomeWelcomeInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit
import Oval

final class WelcomeInteractor {
    weak var view: WelcomeInteractorOutput!
    var router: WelcomeRouter!
    var network: UserNetwork!
    
    init(network: UserNetwork = Session.shared) {
        self.network = network
    }
}

extension WelcomeInteractor: WelcomeInteractorInput {
    func openLogIn() {
        router.openLogIn(with: self)
    }
    
    func openSignUp() {
        router.openSignUp(with: self)
    }
}

extension WelcomeInteractor: WelcomeInteractorDelegate {
    func loginSucceded(for userId: Int, password: String?) {
        if let pass = password {
            network.getTokens(userId: userId, password: pass) { [weak self] (result) in
                switch result {
                case .success:
                    self?.router.openDashboard()
                case .failure:
                    break
                }
            }
        } else {
            router.openDashboard()
        }
    }
}
