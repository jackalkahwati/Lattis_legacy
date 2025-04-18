//
//  SignUpSignUpInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Foundation

protocol SignUpInteractorInput {
    func openLogIn()
    func signUp(user: User.Request)
}

protocol SignUpInteractorOutput: BaseInteractorOutput {}
