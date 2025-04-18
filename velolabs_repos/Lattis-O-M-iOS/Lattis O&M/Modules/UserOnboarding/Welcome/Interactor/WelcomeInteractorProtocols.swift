//
//  WelcomeWelcomeInteractorProtocols.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 10/03/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

protocol WelcomeInteractorInput {
    func signIn()
}

protocol WelcomeInteractorOutput: LoaderPresentable, ErrorPresentable {

}
