//
//  LogInLogInInteractorProtocols.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 08/03/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

protocol LogInInteractorInput {
    func login(with email: String, password: String)
}

protocol LogInInteractorOutput: LoaderPresentable, ErrorPresentable {
    
}
