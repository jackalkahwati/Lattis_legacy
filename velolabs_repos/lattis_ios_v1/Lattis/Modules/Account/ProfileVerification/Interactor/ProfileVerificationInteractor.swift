//
//  ProfileVerificationProfileVerificationInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 03/04/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Oval

class ProfileVerificationInteractor {
    weak var view: ProfileVerificationInteractorOutput!
    var router: ProfileVerificationRouter!
    var verificationType: ProfileVerificationType!
}

extension ProfileVerificationInteractor: ProfileVerificationInteractorInput {
    func submit(code: String) {
        view.startLoading(with: "".localized())
        verificationType.submit(code) { [weak self] (error) in
            guard let `self` = self else { return }
            if let error = error {
                if self.verificationType.infoType == .privateNetwork,
                    let e = error as? SessionError,
                    case .resourceNotFound = e.code {
                    self.view.warning(with: "private_network".localized(), subtitle: "private_network_content".localized())
                } else {
                    self.view.show(error: error, file: #file, line: #line)
                }
            } else {
                self.view.stopLoading(completion: nil)
            }
        }
    }
    
    func viewLoaded() {
        view.show(title: verificationType.title)
    }
    
    func resendCode() {
        view.startLoading(with: "".localized())
        verificationType.resend() { [weak self] error in
            if let error = error {
                if let e = error as? ProfileError, e == .noFleetsForAccount {
                    self?.view.warning(with: "private_network", subtitle: "private_network_content".localized())
                } else {
                    self?.view.show(error: error, file: #file, line: #line)
                }
            } else {
                self?.view.stopLoading(completion: nil)
                self?.view.focusOnCode()
            }
        }
    }
}
