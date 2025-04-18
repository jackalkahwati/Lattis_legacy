//
//  ProfileEditProfileEditInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 03/04/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Foundation

protocol ProfileEditInteractorInput {
    func viewLoaded()
    func submit(value: String)
}

protocol ProfileEditInteractorOutput: class {
    func show(info: ProfileInfoModel)
}
