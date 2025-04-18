//
//  ProfileVerificationProfileVerificationInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 03/04/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Foundation

struct ProfileVerificationType {
    let title: String
    let submit: (String, @escaping (Error?) -> ()) -> ()
    let resend: (@escaping (Error?) -> ()) -> ()
    let infoType: InfoType
}

extension ProfileVerificationType {
    enum InfoType {
        case privateNetwork
        case phoneNumber
        case email
    }
}

protocol ProfileVerificationInteractorInput {
    func submit(code: String)
    func viewLoaded()
    func resendCode()
}

protocol ProfileVerificationInteractorOutput: BaseInteractorOutput {
    func show(title: String)
    func focusOnCode()
}
