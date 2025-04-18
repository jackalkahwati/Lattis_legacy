//
//  ProfileProfileRouter.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 30/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

final class ProfileRouter: Router {
    class func instantiate() -> ProfileViewController {
        let controller = ProfileViewController()
        inject(controller: controller)
        return controller
    }
    
    func pop() {
        controller.navigationController?.popViewController(animated: true)
    }
}

private func inject(controller: ProfileViewController) {
    let interactor = ProfileInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = ProfileRouter(controller)
}
