//
//  ProfileEditProfileEditRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 03/04/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

final class ProfileEditRouter: BaseRouter {
    class func instantiate(with info: ProfileInfoModel) -> ProfileEditViewController {
        let controller = UIStoryboard.account.instantiateViewController(withIdentifier: "edit") as! ProfileEditViewController
        let interactor = inject(controller: controller)
        interactor.info = info
        return controller
    }
}

private func inject(controller: ProfileEditViewController) -> ProfileEditInteractor {
    let interactor = ProfileEditInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = ProfileEditRouter(controller)
    return interactor
}
