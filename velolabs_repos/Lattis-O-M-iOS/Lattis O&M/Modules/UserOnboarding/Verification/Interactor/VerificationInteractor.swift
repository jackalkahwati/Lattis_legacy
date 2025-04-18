//
//  VerificationVerificationInteractor.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 10/03/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//


class VerificationInteractor {
    var router: VerificationRouter!
    weak var view: VerificationInteractorOutput!
    var action:(String, @escaping (Error) -> ()) -> () = {_,_  in}
}

extension VerificationInteractor: VerificationInteractorInput {
    func submit(code: String) {
        action(code) { [unowned self] error in self.handle(error: error) }
    }
}

private extension VerificationInteractor {
    func handle(error: Error) {
        
    }
}
