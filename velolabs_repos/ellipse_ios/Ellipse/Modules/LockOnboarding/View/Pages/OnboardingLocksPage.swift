//
//  OnboardingLocksPage.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/16/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography

protocol OnboardingLocksPageDelegate: class {
    func blink(device: Ellipse.Device)
    func connect(device: Ellipse.Device)
}

class OnboardingLocksPage: ViewController, LockOnboardingPage {
    let viewModel = LocksOnboardingViewModel()
    weak var delegate: OnboardingLocksPageDelegate?
    
    fileprivate let titleLabel = UILabel()
    fileprivate let tableView = UITableView()
    
    func set(delegate: Any?) {
        self.delegate = delegate as? OnboardingLocksPageDelegate
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.addSubview(titleLabel)
        view.addSubview(tableView)

        configureLabel(titleLabel)

        constrain(titleLabel, tableView, view) { title, table, view in
            title.top == view.safeAreaLayoutGuide.top + .margin
            title.left == view.left + .margin
            title.right == view.right - .margin
            
            table.top == title.bottom + .margin
            table.left == view.left
            table.right == view.right
            table.bottom == view.safeAreaLayoutGuide.bottom
        }
        
        tableView.delegate = self
        tableView.dataSource = self
        tableView.tableFooterView = UIView()
        tableView.separatorInset = .init(top: 0, left: .margin, bottom: 0, right: .margin)
        tableView.separatorColor = .elVeryLightPinkTwo
        tableView.rowHeight = 120
        tableView.register(OnboardingEllipseCell.self, forCellReuseIdentifier: "ellipse")
        
        viewModel.reload = { [unowned self] title in
            self.titleLabel.text = title
            self.tableView.reloadData()
        }
    }
}

extension OnboardingLocksPage: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return viewModel.locks.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "ellipse", for: indexPath) as! OnboardingEllipseCell
        cell.device = viewModel.locks[indexPath.row]
        cell.delegate = self
        return cell
    }
}

extension OnboardingLocksPage: OnboardingEllipseCellDelegate {
    func blink(device: Ellipse.Device) {
        delegate?.blink(device: device)
    }
    
    func connect(device: Ellipse.Device) {
        delegate?.connect(device: device)
    }
}

fileprivate let configureLabel: (UILabel) -> () = { label in
    label.font = .elTitleLight
    label.textAlignment = .center
    label.textColor = .black
    label.numberOfLines = 0
}
