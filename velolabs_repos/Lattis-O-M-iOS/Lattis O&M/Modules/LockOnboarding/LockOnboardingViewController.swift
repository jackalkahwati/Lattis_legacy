//
//  LockOnboardingViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 06.01.2020.
//  Copyright © 2020 Lattis. All rights reserved.
//

import UIKit
import Cartography

class LockOnboardingViewController: ViewController {

    fileprivate let tableView = UITableView()
    fileprivate let emptyLabel = UILabel()
    fileprivate let viewModel: LockOnboardingViewModel
    
    init(_ viewModel: LockOnboardingViewModel) {
        self.viewModel = viewModel
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "Onboard lock"
        view.backgroundColor = .white
        
        emptyLabel.text = "No locks to onboard"
        view.addSubview(emptyLabel)
        view.addSubview(tableView)
        tableView.isHidden = true
        constrain(tableView, emptyLabel, view) { table, empty, view in
            table.edges == view.edges
            empty.center == view.center
        }
        tableView.register(UITableViewCell.self, forCellReuseIdentifier: "cell")
        tableView.tableFooterView = UIView()
        tableView.dataSource = self
        tableView.delegate = self
        
        viewModel.insert = { [unowned self] indexPaths in
            self.tableView.isHidden = indexPaths.count == 0
            self.emptyLabel.isHidden = !self.tableView.isHidden
            self.tableView.beginUpdates()
            self.tableView.insertRows(at: indexPaths, with: .automatic)
            self.tableView.endUpdates()
        }
        
        viewModel.delete = { [unowned self] insexPaths in
            self.tableView.isHidden = self.viewModel.locksCount == 0
            self.emptyLabel.isHidden = !self.tableView.isHidden
            self.tableView.beginUpdates()
            self.tableView.deleteRows(at: insexPaths, with: .automatic)
            self.tableView.endUpdates()
        }
        viewModel.scan()
        navigationItem.leftBarButtonItem = .close(target: self, action: #selector(close))
    }
    
    @objc func close() {
        dismiss(animated: true, completion: nil)
    }
    
    fileprivate func onboardLock(at index: Int) {
        startLoading(title: "Onboarding Lock")
        viewModel.onboardLock(at: index) { [weak self] (isLockFree) in
            self?.stopLoading {
                if isLockFree {
                    self?.dismiss(animated: true, completion: {
                        self?.viewModel.finish(with: index)
                    })
                } else {
                    self?.showAlert(title: "You can't use that lock", subtitle: "It belongs to different fleet")
                    self?.viewModel.deleteLock(at: index)
                }
            }
        }
    }
    
    fileprivate func flashLED(at index: Int) {
        startLoading(title: "Blinking LED")
        viewModel.blinkLock(at: index) { [weak self] in
            self?.stopLoading()
        }
    }
}

extension LockOnboardingViewController: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return viewModel.locksCount
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath)
        cell.textLabel?.text = viewModel.title(for: indexPath.row)
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let alert = AlertController(title: "Choose action", message: viewModel.title(for: indexPath.row))
        alert.actions = [
            .plain(title: "Onboard", handler: { [unowned self] in
                self.onboardLock(at: indexPath.row)
            }),
            .plain(title: "locks_action_blink".localized(), handler: { [unowned self] in
                self.flashLED(at: indexPath.row)
            }),
            .cancel
        ]
        present(alert, animated: true, completion: nil)
        tableView.deselectRow(at: indexPath, animated: true)
    }
}
