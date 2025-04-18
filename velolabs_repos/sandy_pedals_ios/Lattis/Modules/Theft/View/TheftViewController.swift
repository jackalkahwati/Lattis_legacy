//
//  TheftTheftViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 04/04/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

class TheftViewController: ViewController {
    var interactor: TheftInteractorInput!

    override func viewDidLoad() {
        super.viewDidLoad()

        title = "theft_report_title".localized()
        navigationItem.leftBarButtonItem = .close(target: self, action: #selector(closeAction))
    }
    
    @objc private func closeAction() {
        dismiss(animated: true, completion: nil)
    }
    
    @IBAction func reportAction(_ sender: Any) {
        interactor.submit()
    }
}

extension TheftViewController: TheftInteractorOutput {
    func showSuccess() {
        let actionTitle = "theft_report_success_button".localized()
        let alert = ErrorAlertView.alert(title: "theft_report_success_title".localized(), subtitle: "theft_report_success_subtitle".localized(), hint: "theft_report_success_hint".localized(), button: actionTitle)
        alert.action = { self.dismiss(animated: true, completion: {
                AppRouter.shared.endTripOrCancelBooking(theft: true)
            })
        }
        stopLoading {
            alert.show()
        }
    }
}
