//
//  SLOnboardingMapBackgroundViewController.swift
//  Skylock
//
//  Created by Andre Green on 7/29/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

class SLOnboardingMapBackgroundViewController: SLOnboardingViewController {
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.titleLabel.textColor = UIColor(red: 130, green: 156, blue: 178)
        self.textLabel.textColor = UIColor(red: 188, green: 187, blue: 187)
        
        self.pictureView.frame = self.view.bounds
    }
}
