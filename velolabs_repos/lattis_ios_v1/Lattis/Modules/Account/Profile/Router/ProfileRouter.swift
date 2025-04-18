//
//  ProfileProfileRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 02/04/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

final class ProfileRouter: BaseRouter {
    static func instantiate() -> (ProfileViewController, ProfileInteractor) {
        let controllet = UIStoryboard.account.instantiateViewController(withIdentifier: "profile") as! ProfileViewController
        let interactor = inject(controller: controllet)
        return (controllet, interactor)
    }
    
    func pop(root: Bool = false) {
        if root {
            controller.navigationController?.popToRootViewController(animated: true)
        } else {
            _ = controller.navigationController?.popViewController(animated: true)
        }
    }
    
    func edit(info: ProfileInfoModel) {
        let edit = ProfileEditRouter.instantiate(with: info)
        controller.navigationController?.pushViewController(edit, animated: true)
    }
    
    func openVerification(verificationType: ProfileVerificationType) {
        let verification = ProfileVerificationRouter.instantiate(verificationType: verificationType)
        controller.navigationController?.pushViewController(verification, animated: true)
    }
    
    func changePassword() {
         let pass = ProfilePasswordRouter.instantiate()
        controller.navigationController?.pushViewController(pass, animated: true)
    }
    
    func deleteAccount() {
        let delete = UIStoryboard.account.instantiateViewController(withIdentifier: "delete") as! ProfileDeleteViewController
        let interactor = ProfileInteractor()
        delete.interactor = interactor
        interactor.view = delete
        interactor.router = ProfileRouter(controller)
        controller.navigationController?.pushViewController(delete, animated: true)
    }
}

private func inject(controller: ProfileViewController) -> ProfileInteractor {
    let interactor = ProfileInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = ProfileRouter(controller)
    return interactor
}
