//
//  ViewController+Extensions.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/26/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import LGSideMenuController

extension ViewController {
    func addMenuButton() {
        navigationItem.leftBarButtonItem = .menu(target: self, action: #selector(menu))
    }
    
    @objc func menu() {
        showLeftViewAnimated(self)
    }
    
    func addBackButton() {
        navigationItem.leftBarButtonItem = .back(target: self, action: #selector(back))
    }
    
    @objc func back() {
        navigationController?.popViewController(animated: true)
    }
    
    func addCloseButton() {
        navigationItem.leftBarButtonItem = .close(target: self, action: #selector(close))
    }
    
    @objc func close() {
        dismiss(animated: true, completion: nil)
    }
}
