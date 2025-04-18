//
//  LockOnboardingLockOnboardingRouter.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 16/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

final class LockOnboardingRouter: Router {
    class func instantiate(delegate: LockOnboardingDelegate?) -> LockOnboardingViewController {
        let controller = LockOnboardingViewController()
        let interactor = inject(controller: controller)
        interactor.delegate = delegate
        return controller
    }
    
    func dismiss(_ openDashboard: Bool = false) {
        controller.dismiss(animated: true) {
            guard openDashboard else { return }
            AppDelegate.shared.navigateHome()
        }
    }
}

private func inject(controller: LockOnboardingViewController) -> LockOnboardingInteractor {
    let interactor = LockOnboardingInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = LockOnboardingRouter(controller)
    return interactor
}

extension PageType {
    func controller(with delegate: Any?) -> LockOnboardingPage {
        let page: LockOnboardingPage
        switch self {
        case .choose:
            page = OnboardingChoosePage()
        case .list:
            page = OnboardingLocksPage()
        case .share:
            page = OnboardingSharePage()
        case .pin:
            page = UIStoryboard.lockOnboarding.instantiateViewController(withIdentifier: "pin") as! OnboardingPinPage
        case .rename:
            page = OnboardingRenamePage()
        case .touch:
            page = OnboardingTouchPage()
        }
        page.set(delegate: delegate)
        return page
    }
}
