//
//  LockOnboardingLockOnboardingInteractorProtocols.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 16/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

enum PageType: String {
    case choose, touch, list, rename, pin, share
}

protocol LockOnboardingDelegate: class {
    func didFinishLockOnboarding()
}

protocol LockOnboardingInteractorInput: OnboardingPinPageDelegate, OnboardingChoosePageDelegate, OnboardingLocksPageDelegate, OnboardingRenamePageDelegate, OnboardingTouchPageDelegate, OnboardingSharePageDelegate {
    func didShowPage(at index: Int)
    func push()
    func page(for index: Int) -> LockOnboardingPage
    var numberOfPages: Int {get}
    var delegate: LockOnboardingDelegate? { get set }
}

protocol LockOnboardingInteractorOutput: InteractorOutput {
    func show(title: String?)
    func pushPage()
    func reload()
    func hideCloseButton()
}

extension PageType {
    var title: String {
        switch self {
        case .list:
            return "title_activity_add_lock".localized()
        case .rename:
            return "lock_name".localized()
        case .share:
            return "enter_your_invitation_code".localized()
        default:
            return "addlock_home_description_1".localized()
        }
    }
}
