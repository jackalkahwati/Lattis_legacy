//
//  VerificationVerificationViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 10/03/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class VerificationViewController: ViewController {
    @IBOutlet weak var codeField: FloatLabelTextField!
    var interactor: VerificationInteractorInput!

    override func viewDidLoad() {
        super.viewDidLoad()

    }
    
    @IBAction func verify(_ sender: Any) {
        interactor.submit(code: codeField.text!)
    }
    
    @IBAction func skip(_ sender: Any) {
        
    }
}

extension VerificationViewController: VerificationInteractorOutput {
	
}
