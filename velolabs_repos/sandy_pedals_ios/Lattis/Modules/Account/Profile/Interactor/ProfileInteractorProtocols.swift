//
//  ProfileProfileInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 02/04/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

protocol ProfileInteractorInput {
    func edit(info: ProfileInfoModel)
    func changePassword()
    func deleteAccount()
    func viewLoaded()
    func update(value: String, for infoType: ProfileInfoType)
    func save(image: UIImage)
}

protocol ProfileInteractorOutput: BaseInteractorOutput {
    var hasFleets: Bool {get}
    func show(_ user: User)
}
