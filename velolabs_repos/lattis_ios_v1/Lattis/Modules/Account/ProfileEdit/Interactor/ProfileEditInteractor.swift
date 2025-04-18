//
//  ProfileEditProfileEditInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 03/04/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Foundation

class ProfileEditInteractor {
    weak var view: ProfileEditInteractorOutput!
    var router: ProfileEditRouter!
    var info: ProfileInfoModel!
}

extension ProfileEditInteractor: ProfileEditInteractorInput {
    func viewLoaded() {
        view.show(info: info)
    }
    
    func submit(value: String) {
        info.action(value, info.type)
    }
}


