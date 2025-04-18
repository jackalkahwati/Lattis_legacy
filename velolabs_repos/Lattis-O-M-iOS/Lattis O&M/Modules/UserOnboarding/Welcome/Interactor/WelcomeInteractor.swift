//
//  WelcomeWelcomeInteractor.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 10/03/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Oval

class WelcomeInteractor {
    var router: WelcomeRouter!
    weak var view: WelcomeInteractorOutput!
    let network: OperatorNetwork = Session.shared
}

extension WelcomeInteractor: WelcomeInteractorInput {
    func signIn() {
        router.signIn() { $0.delegate = self }
    }
}

extension WelcomeInteractor: LogInInteractorDelegate {
    func loginSucceded(for userId: Int, verification: ((UIViewController) -> ())?, errorHandler:@escaping (Error?) -> ()) {
        if let ver = verification {
            verify(userId, open: ver, errorHandler: errorHandler)
        } else {
            openDashboard()
        }
    }
    
    func loginSucceded(userId: Int) {
        CoreDataStack.shared.setup(userId: userId, completion: openDashboard)
    }
}

private extension WelcomeInteractor {
    func verify(_ userId: Int, open: @escaping (UIViewController) -> (), errorHandler:@escaping (Error?) -> ()) {
        network.signInCode { [weak self] result in
            switch result {
            case .success:
                self?.verification(open: open)
                errorHandler(nil)
            case .failure(let error):
                errorHandler(error)
            }
        }
    }
    
    func verification(open: (UIViewController) -> ()) {
        let controller = router.verifiCation() { $0.action = self.signInVerification }
        open(controller)
    }
    
    func signInVerification(code: String, errorHandler: @escaping (Error) -> ()) {
        network.confirm(signIn: code) { [weak self] result in
            switch result {
            case .success:
                self?.openDashboard()
            case .failure(let error):
                errorHandler(error)
            }
        }
    }
    
    func openDashboard() {
        router.openDashboard()
    }
}
