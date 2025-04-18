//
//  WelcomeWelcomeViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 10/03/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class WelcomeViewController: ViewController {
    var interactor: WelcomeInteractorInput!

    override func viewDidLoad() {
        super.viewDidLoad()
        
        navigationController?.isNavigationBarHidden = true
    }
    
    @IBAction func signIn(_ sender: Any) {
        interactor.signIn()
    }
}

extension WelcomeViewController: WelcomeInteractorOutput {
	
}
