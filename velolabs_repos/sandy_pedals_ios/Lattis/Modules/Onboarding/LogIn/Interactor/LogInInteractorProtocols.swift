//
//  LogInLogInInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

protocol LogInInteractorInput {
    func openSignUp()
    func forgotPassword()
    func logIn(user: User.Request)
}

protocol LogInInteractorOutput: BaseInteractorOutput {
    func show(email: String)
}
