//
//  WelcomeWelcomeInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

protocol WelcomeInteractorDelegate: class {
    func loginSucceded(for userId: Int, password: String?)
}

protocol WelcomeInteractorInput {
    func openLogIn()
    func openSignUp()
}

protocol WelcomeInteractorOutput: class {

}
