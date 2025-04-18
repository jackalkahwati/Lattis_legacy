//
//  HelpViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 5/11/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

class HelpViewController: UIViewController {
    
    static var navigation: UINavigationController {
        let controller = HelpViewController(nibName: "HelpViewController", bundle: nil)
        return UINavigationController(rootViewController: controller, style: .blue)
    }
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
    
    @IBOutlet weak var textView: UITextView!

    override func viewDidLoad() {
        super.viewDidLoad()
        title = "help_title".localized()
        AppRouter.shared.getSupportPhone { [weak self] (phone) in
            self?.textView.text = String(format: "help_text".localized(), phone)
        }
        
        navigationItem.leftBarButtonItem = .close(target: self, action: #selector(close))
    }
    
    override var prefersStatusBarHidden: Bool {
        return false
    }
    
    @objc func close() {
        dismiss(animated: true, completion: nil)
    }
}
