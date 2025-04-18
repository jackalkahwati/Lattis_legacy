//
//  ProfileDeleteViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 03/04/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

class ProfileDeleteViewController: ViewController {
    var hasFleets: Bool = false
    var interactor: ProfileInteractorInput!
    override func viewDidLoad() {
        super.viewDidLoad()

        title = "profile_delete_account_title".localized()
        navigationItem.leftBarButtonItem = .back(target: self, action: #selector(back))
    }
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
    
    @objc private func back() {
        _ = navigationController?.popViewController(animated: true)
    }
}

extension ProfileDeleteViewController: ProfileInteractorOutput {
    func show(_ user: User) {}
}
