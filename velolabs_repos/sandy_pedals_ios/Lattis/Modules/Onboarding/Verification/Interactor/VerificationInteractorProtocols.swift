//
//  VerificationVerificationInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 31/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Foundation

protocol VerificationInteractorInput {
    func verify(with code: String)
}

protocol VerificationInteractorOutput: BaseInteractorOutput {
}
