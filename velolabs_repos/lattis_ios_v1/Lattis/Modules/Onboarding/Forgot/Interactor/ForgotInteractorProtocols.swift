//
//  ForgotForgotInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Foundation

protocol ForgotInteractorDelegate: class {
    func passwordChanged(for email: String)
}

protocol ForgotInteractorInput {
    func viewLoaded()
    func submit(email: String)
    func submit(password: String, code: String)
}

protocol ForgotInteractorOutput: BaseInteractorOutput {
    func show(email: String)
    func showSuccess()
}
