//
//  ProfilePasswordProfilePasswordInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 03/04/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Foundation

protocol ProfilePasswordInteractorInput {
    func submit(password: String, newPass: String)
}

protocol ProfilePasswordInteractorOutput: BaseInteractorOutput {
    func success()
}
